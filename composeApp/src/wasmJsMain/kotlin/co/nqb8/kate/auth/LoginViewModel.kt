package co.nqb8.kate.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nqb8.kate.data.*
import co.nqb8.kate.routes.RouteState
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel: ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            kotlin.runCatching {
                val request = client.post("/_login"){
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("email" to email, "password" to password))
                }
                if (!request.status.isSuccess()) throw Exception(request.status.description)
                request.body<Response<Token>>()
            }.onSuccess { data ->
                TokenStorage.saveToken(data.data.token)
                _state.update { it.copy(isLoading = false, isAuthenticated = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
}