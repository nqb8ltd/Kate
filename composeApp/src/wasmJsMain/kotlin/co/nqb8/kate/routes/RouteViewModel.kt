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

    fun addRoute(service: Service, routes: KateRoutes, isProtected: Boolean) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        val auth = state.value.services.flatMap { it.routes }.first { it.authenticationPolicy != null && it.authenticationPolicy is JwtPolicy }.authenticationPolicy
        val rate = state.value.services.flatMap { it.routes }.first { it.rateLimitPolicy != null }.rateLimitPolicy
        val body = service.copy(
            routes = listOf(
                Route(
                    uri = routes.path,
                    methods = listOf(Route.Method(routes.method)),
                    rateLimitPolicy = rate,
                    authenticationPolicy = if (isProtected) auth else null
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
            println("Error: ${error.message}")
            _state.update { it.copy(isLoading = false, error = error.message) }
        }
    }

    fun updateRoute(service: Service, routes: Route, isProtected: Boolean) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        val auth = state.value.services.flatMap { it.routes }.first { it.authenticationPolicy != null && it.authenticationPolicy is JwtPolicy }.authenticationPolicy
        val body = service.copy(
            routes = listOf(routes.copy(authenticationPolicy = if (isProtected) auth else null))
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
            println("Error: ${error.message}")
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
            _state.update { state -> state.copy(isLoading = false, data = it.first, services = it.second) }
        }
    }
}