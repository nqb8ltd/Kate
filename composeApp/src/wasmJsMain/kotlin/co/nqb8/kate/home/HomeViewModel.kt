package co.nqb8.kate.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nqb8.kate.auth.TokenStorage
import co.nqb8.kate.data.DashboardHome
import co.nqb8.kate.data.Response
import co.nqb8.kate.data.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        fetchDashboardData()
    }

    private fun fetchDashboardData() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        val request = runCatching {
            val request = client.get("/_dashboard/home")
            if (request.status == HttpStatusCode.Unauthorized){
                TokenStorage.clearToken()
                throw Exception(request.status.description)
            }
            request.body<Response<DashboardHome>>()
        }
        request.onFailure {
            _state.update { it.copy(isLoading = false, error = it.error) }
        }
        request.onSuccess {
            println("Success: ${it.data}")
            _state.update { state -> state.copy(isLoading = false, data = it.data) }
        }
    }
}