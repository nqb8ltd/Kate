package co.nqb8.kate.traces

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.nqb8.kate.FullScreenLoading
import co.nqb8.kate.data.Trace
import io.ktor.http.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.ceil


@Composable
fun TracesScreen(paddingValues: PaddingValues) {
    val viewModel = remember { TraceViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTrace by remember { mutableStateOf<Trace?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error){
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }


    selectedTrace?.let { trace ->
        TraceDetailsDialog(
            trace = trace,
            onDismiss = { selectedTrace = null }
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.padding(paddingValues).fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            TracesHeader()
            Spacer(modifier = Modifier.height(24.dp))
            TracesTable(
                modifier = Modifier.weight(1f),
                isLoading = state.isLoading,
                traces = state.pagedResult?.items.orEmpty(),
                onTraceClick = { trace ->
                    selectedTrace = trace
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PaginationControls(
                currentPage = state.pagedResult?.page ?: 1,
                totalPages = state.pagedResult?.total?.let { ceil(it.toDouble() / 20).toInt() } ?: 0,
                hasNext = state.pagedResult?.hasNext ?: false,
                hasPrevious = state.pagedResult?.hasPrevious ?: false,
                onPageChange = { newPage ->
                    viewModel.fetchTraces(page = newPage)
                }
            )
        }
    }
    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(top=16.dp))
}

@Composable
private fun TracesHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Traces",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterButton(text = "Route")
            FilterButton(text = "Status")
            FilterButton(text = "Timeframe")
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Text") },
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
private fun FilterButton(text: String) {
    OutlinedButton(onClick = { /* TODO: Filter logic */ }) {
        Text(text)
        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
    }
}

// Table displaying the list of traces
@Composable
private fun TracesTable(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    traces: List<Trace>,
    onTraceClick: (Trace) -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(text = "Route", weight = 2f, isHeader = true)
            TableCell(text = "Status", weight = 1f, isHeader = true)
            TableCell(text = "Duration", weight = 1f, isHeader = true)
            TableCell(text = "Timestamp", weight = 1.5f, isHeader = true)
        }
        if (isLoading){
            FullScreenLoading()
        }else {
            LazyColumn {
                items(traces) { trace ->
                    Divider(color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTraceClick(trace) }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(text = trace.route, weight = 2f)
                        TableCell(weight = 1f) { StatusBadge(status = trace.status) }
                        TableCell(text = trace.duration.toString(), weight = 1f)
                        TableCell(text = trace.timeStamp.toString(), weight = 1.5f)
                    }
                    //Divider(color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

// Reusable composable for table cells
@Composable
private fun RowScope.TableCell(
    text: String? = null,
    weight: Float,
    isHeader: Boolean = false,
    content: @Composable () -> Unit = {
        if (text != null) {
            Text(
                text = text,
                style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                color = if (isHeader) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(end = 16.dp)
    ) {
        content()
    }
}

// Badge for displaying trace status
@Composable
private fun StatusBadge(status: Int) {
    val (color, textColor) = when (HttpStatusCode.fromValue(status).isSuccess()) {
        true -> Color(0xFF34C759).copy(alpha = 0.2f) to Color(0xFF34C759)
        false -> Color(0xFFFF453A).copy(alpha = 0.2f) to Color(0xFFFF453A)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = status.toString(), color = textColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

// Controls for navigating between pages
@Composable
private fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    hasNext: Boolean,
    hasPrevious: Boolean,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { onPageChange(currentPage - 1) }, enabled = hasPrevious) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous Page")
        }
        Text("Page $currentPage of $totalPages", modifier = Modifier.padding(horizontal = 16.dp))
        IconButton(onClick = { onPageChange(currentPage + 1) }, enabled = hasNext) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Next Page")
        }
    }
}

// Dialog to show the details of a selected trace
@Composable
private fun TraceDetailsDialog(trace: Trace, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Request Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                DetailRow("Route", trace.route)
                DetailRow("Status") { StatusBadge(status = trace.status) }
                DetailRow("Duration", "${trace.duration}ms")
                DetailRow("Timestamp", trace.timeStamp.format(LocalDateTime.Formats.ISO))
                DetailRow("Method", trace.method)
                DetailRow("Source IP", trace.sourceIp)
                DetailRow("Upstream duration", "${trace.upstreamDuration}ms")
                DetailRow("Request", trace.id)
                Text("Request Body", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                JsonBody(trace.requestBody)
                Text("Response Body", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                JsonBody(trace.responseBody)


                //Spacer(Modifier.weight(1f))

                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Close")
                }
            }
        }
    }
}
@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailRow(label: String, content: @Composable () -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun JsonBody(json: String) {
    val jzon = Json { prettyPrint = true }
    var data by remember { mutableStateOf("") }
    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(json){
        println("Json: $json")
        runCatching {
            val mapJson = jzon.decodeFromString<JsonElement>(json).jsonObject.toMap()
            jzon.encodeToString(mapJson)
        }.onSuccess {
            data = it
            isLoaded = true
        }.onFailure {
            isLoaded = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.99f)
            .fillMaxHeight(0.9f)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        if (!isLoaded){
            FullScreenLoading()
        }else{
            Text(
                data,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

    }
}