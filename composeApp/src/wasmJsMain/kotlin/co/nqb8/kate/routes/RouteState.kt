package co.nqb8.kate.routes

import co.nqb8.kate.data.KateRoutes
import co.nqb8.kate.data.Route
import co.nqb8.kate.data.Service

data class RouteState(
    val isLoading: Boolean = false,
    val original: List<KateRoutes> = emptyList(),
    val data: List<KateRoutes> = emptyList(),
    val services: List<Service> = listOf(),
    val error: String? = null
)


data class RouteEdit(
    val service: Service,
    val routes: KateRoutes,
    val route: Route,
    val isProtected: Boolean,
    val requestBodyType: Route.RequestBodyType = Route.RequestBodyType.JSON
)