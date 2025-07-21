package co.nqb8.kate.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

object TokenStorage {
    var token by mutableStateOf(getToken())
        private set

    private const val TOKEN_KEY = "auth_token"

    fun saveToken(token: String) {
        this.token = token
        localStorage[TOKEN_KEY] = token
    }

    private fun getToken(): String {
        return localStorage[TOKEN_KEY].orEmpty()
    }

    fun clearToken() {
        token = ""
        localStorage.removeItem(TOKEN_KEY)
    }
}