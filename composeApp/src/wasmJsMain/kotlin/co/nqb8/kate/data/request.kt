package co.nqb8.kate.data

import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    val message: String? = null,
    val data: T
)

@Serializable
data class Token(
    val token: String
)