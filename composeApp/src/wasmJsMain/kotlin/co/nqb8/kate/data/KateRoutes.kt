package co.nqb8.kate.data

import kotlinx.serialization.Serializable

@Serializable
data class KateRoutes(
    val path: String,
    val method: String,
    val route: Route?
)