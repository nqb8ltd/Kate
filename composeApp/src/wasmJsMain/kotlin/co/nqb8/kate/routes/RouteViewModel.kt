package co.nqb8.kate.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nqb8.kate.data.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RouteViewModel: ViewModel() {
    private val _state = MutableStateFlow(RouteState())
    val state = _state.asStateFlow()

    init {
        fetchRoutes()
    }

    fun addRoute(routeEdit: RouteEdit) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        val auth = state.value.services.flatMap { it.routes }.first { it.authenticationPolicy != null && it.authenticationPolicy is JwtPolicy }.authenticationPolicy
        val rate = state.value.services.flatMap { it.routes }.first { it.rateLimitPolicy != null }.rateLimitPolicy
        val authPolicy = (auth as? JwtPolicy)?.copy(check = "", checkPath = "")
        val body = routeEdit.service.copy(
            routes = listOf(
                Route(
                    uri = routeEdit.routes.path,
                    methods = listOf(Route.Method(routeEdit.routes.method, routeEdit.requestBodyType)),
                    rateLimitPolicy = rate,
                    authenticationPolicy = if (routeEdit.isProtected) authPolicy else null
                )
            )
        )
        println("Add new")
        println(body)

        runCatching {
            client.post("/_dashboard/route"){
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.onSuccess {
            fetchRoutes()
        }.onFailure { error ->
            _state.update { it.copy(isLoading = false, error = error.message) }
        }
    }

    fun updateRoute(routeEdit: RouteEdit) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        val auth = state.value.services.flatMap { it.routes }.first { it.authenticationPolicy != null && it.authenticationPolicy is JwtPolicy }.authenticationPolicy
        val body = routeEdit.service.copy(
            routes = listOf(
                routeEdit.route.copy(
                    authenticationPolicy = if (routeEdit.isProtected) auth else null,
                    methods = listOf(
                        Route.Method(routeEdit.routes.method, routeEdit.requestBodyType)
                    ),
                )
            )
        )
        println("Update: $body")
        runCatching {
            client.put("/_dashboard/route"){
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.onSuccess {
            fetchRoutes()
        }.onFailure { error ->
            _state.update { it.copy(isLoading = false, error = error.message) }
        }
    }
    private fun fetchRoutes() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        runCatching {
            val routes = async { client.get("/_dashboard/route").body<Response<List<KateRoutes>>>() }
            val services = async { client.get("/_dashboard/service").body<Response<List<Service>>>() }
            val routeResult = routes.await()
            val serviceResult = services.await()
            routeResult.data to serviceResult.data
        }.onFailure { error ->
            _state.update { it.copy(isLoading = false, error = error.message) }
        }.onSuccess {
            _state.update { state -> state.copy(isLoading = false, data = it.first, original = it.first, services = it.second) }
        }
    }

    fun search(currentQuery: String) {
        viewModelScope.launch {
            val routes = state.value.original.filter { it.path.contains(currentQuery) }
            _state.update { it.copy(data = routes) }
        }
    }
}