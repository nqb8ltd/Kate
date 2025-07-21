package co.nqb8.kate.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.nqb8.kate.auth.TokenStorage
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val client = HttpClient {
    defaultRequest {
        url("https://api.nqb8.co")
        bearerAuth(TokenStorage.token)
    }
    install(ContentNegotiation){
        json(
            Json { ignoreUnknownKeys = true }
        )
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.NONE
    }
}