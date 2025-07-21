package co.nqb8.kate.home

import co.nqb8.kate.data.DashboardHome

data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: DashboardHome? = null
)
