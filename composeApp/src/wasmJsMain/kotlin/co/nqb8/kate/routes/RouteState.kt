package co.nqb8.kate.routes

import co.nqb8.kate.data.KateRoutes
import co.nqb8.kate.data.Service

data class RouteState(
    val isLoading: Boolean = false,
    val data: List<KateRoutes> = emptyList(),
    val services: List<Service> = listOf(),
    val error: String? = null
)
