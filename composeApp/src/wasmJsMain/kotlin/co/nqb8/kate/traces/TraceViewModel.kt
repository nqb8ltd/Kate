package co.nqb8.kate.traces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nqb8.kate.auth.TokenStorage
import co.nqb8.kate.data.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TraceViewModel: ViewModel() {
    private val _state = MutableStateFlow(TraceState())
    val state = _state.asStateFlow()

    init {
        fetchTraces()
    }

    fun fetchTraces(page: Int = 1, count: Int = 20) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        val request = runCatching {
            val request = client.get("/_dashboard/traces"){
                parameter("page", page)
                parameter("count", count)
            }
            if (request.status == HttpStatusCode.Unauthorized){
                TokenStorage.clearToken()
                throw Exception(request.status.description)
            }
            request.body<Response<PagedResult>>()
        }
        request.onFailure { error ->
            _state.update { it.copy(isLoading = false, error = error.message) }
        }
        request.onSuccess {
            _state.update { state -> state.copy(isLoading = false, pagedResult = it.data) }
        }
    }
}