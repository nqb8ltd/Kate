package co.nqb8.kate.traces

import co.nqb8.kate.data.PagedResult
import co.nqb8.kate.data.Trace

data class TraceState(
    val isLoading: Boolean = false,
    val pagedResult: PagedResult? = null,
    val error: String? = null
)
