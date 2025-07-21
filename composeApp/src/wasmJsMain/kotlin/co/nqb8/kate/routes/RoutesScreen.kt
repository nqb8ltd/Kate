package co.nqb8.kate.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.nqb8.kate.FullScreenLoading
import co.nqb8.kate.data.KateRoutes
import co.nqb8.kate.data.Route
import co.nqb8.kate.data.Service


@Composable
fun RoutesScreen(paddingValues: PaddingValues) {
    val viewModel = remember { RouteViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedRouteForDialog by remember { mutableStateOf<KateRoutes?>(null) }
    var showAddRouteDialog by remember { mutableStateOf(false) }
    var isEdit by remember { mutableStateOf(false) }
    var isCreate by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error){
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }


    if (state.isLoading) {
        FullScreenLoading()
    }else{
        RoutesContent(
            paddingValues = paddingValues,
            state = state,
            onRouteClick = {
                selectedRouteForDialog = it
                showAddRouteDialog = true
            },
            onAddRouteClick = {
                isCreate = true
                isEdit = true
                showAddRouteDialog = true
            },
            onEditRoute = {
                selectedRouteForDialog = it
                isEdit = true
                showAddRouteDialog = true
            }
        )
    }


    if (showAddRouteDialog) {
        RouteDialog(
            isEdit = isEdit,
            services = state.services,
            route = selectedRouteForDialog,
            onDismiss = { showAddRouteDialog = false; selectedRouteForDialog = null },
            onSave = { service, newRoute, authEnabled ->
                showAddRouteDialog = false
                if (isEdit && isCreate){
                    viewModel.addRoute(service, newRoute, authEnabled)
                    return@RouteDialog
                }
                if (isEdit){
                    viewModel.updateRoute(service, newRoute.route!!, authEnabled)
                    return@RouteDialog
                }
            }
        )
    }

    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(top=16.dp))
}

@Composable
private fun RoutesContent(
    paddingValues: PaddingValues,
    state: RouteState,
    onRouteClick: (KateRoutes) -> Unit,
    onEditRoute: (KateRoutes) -> Unit,
    onAddRouteClick: () -> Unit
){

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.padding(paddingValues).fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            RoutesHeader { onAddRouteClick() }
            Spacer(modifier = Modifier.height(24.dp))
            RoutesTable(
                services = state.services,
                routes = state.data,
                onRouteClick = onRouteClick,
                onEditRoute = onEditRoute
            )
        }
    }
}

// Header section with title and action buttons
@Composable
fun RoutesHeader(onAddRouteClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Routes",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* TODO: Search logic */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search Routes")
            }
            IconButton(onClick = { /* TODO: Filter logic */ }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter Routes")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onAddRouteClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Icon", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Route")
            }
        }
    }
}

// Table displaying the list of routes
@Composable
fun RoutesTable(
    services: List<Service>,
    routes: List<KateRoutes>,
    onRouteClick: (KateRoutes) -> Unit,
    onEditRoute: (KateRoutes) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn {
            // Table Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(text = "Name", weight = 2f, isHeader = true)
                    TableCell(text = "Method", weight = 0.3f, isHeader = true)
                    TableCell(text = "Path", weight = 2f, isHeader = true)
                    //TableCell(text = "Version", weight = 1f, isHeader = true)
                    TableCell(text = "Service", weight = 0.5f, isHeader = true)
                    TableCell(text = "Status", weight = 0.5f, isHeader = true)
                    //TableCell(text = "Last Updated", weight = 1.5f, isHeader = true)
                    TableCell(text = "Actions", weight = 0.5f, isHeader = true)
                }
                Divider(color = MaterialTheme.colorScheme.outline)
            }

            // Table Rows
            items(routes) { route ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRouteClick(route) }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(text = transformPath(route.path), weight = 2f)
                    TableCell(text = route.method, weight = 0.3f)
                    TableCell(text = route.path, weight = 2f)
                    TableCell(text = getServiceByRoute(services, route)?.name.orEmpty(), weight = 0.5f)
                    TableCell(weight = 0.5f) { StatusBadge(status = "Active") }
                    TableCell(weight = 0.5f) {
                        Text(
                            text = "Edit",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onEditRoute(route) }.padding(5.dp)
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
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

// Badge for displaying route status
@Composable
fun StatusBadge(status: String) {
    val (color, textColor) = when (status.lowercase()) {
        "active" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) to MaterialTheme.colorScheme.primary
        else -> Color.Gray.copy(alpha = 0.2f) to Color.White
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = status, color = textColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

// Dialog for adding or editing a route
@Composable
private fun RouteDialog(
    isEdit: Boolean = false,
    services: List<Service> = listOf(),
    route: KateRoutes? = null,
    onDismiss: () -> Unit = {},
    onSave: (Service, KateRoutes, Boolean) -> Unit
) {
    var path by remember { mutableStateOf(route?.path.orEmpty()) }
    val name = remember(path) { transformPath(path)  }
    var method by remember { mutableStateOf(Route.Method(route?.method.orEmpty())) }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var authEnabled by remember { mutableStateOf(false) }


    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Route Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        DialogTextField(
                            isEdit = false,
                            label = "Name",
                            value = name,
                            onValueChange = { }
                        )
                    }
                    item {
                        DialogTextField(
                            isEdit = isEdit,
                            label = "Path",
                            value = path,
                            onValueChange = { path = it }
                        )
                    }
                    item {
                        MethodDropdown(
                            isEdit = isEdit,
                            selectedMethod = method,
                            onMethodSelected = { method = it }
                        )
                    }
                    item {
                        ServiceDropdown(
                            isEdit = isEdit,
                            availableServices = services,
                            selectedService = selectedService,
                            onServiceSelected = { selectedService = it }
                        )
                    }
                    item {
                        Text("Authentication and Rate Limit policies would be edited here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        AuthDropdown(
                            isEdit = isEdit,
                            option = if (route?.route?.authenticationPolicy != null) "Yes" else "No",
                            onAuthEnabled = { authEnabled = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    if (isEdit) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                selectedService?.let {
                                    onSave(it, KateRoutes(path, method.method, route?.route), authEnabled)
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogTextField(
    isEdit: Boolean = false,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        readOnly = !isEdit,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDropdown(
    isEdit: Boolean = false,
    availableServices: List<Service>,
    selectedService: Service?,
    onServiceSelected: (Service) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedService?.name.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Service") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded && isEdit,
            onDismissRequest = { expanded = false }
        ) {
            availableServices.forEach { serviceName ->
                DropdownMenuItem(
                    text = { Text(serviceName.name) },
                    onClick = {
                        onServiceSelected(serviceName)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MethodDropdown(
    isEdit: Boolean = false,
    selectedMethod: Route.Method,
    onMethodSelected: (Route.Method) -> Unit
) {
    val methods = listOf(
        Route.Method(method = "GET"),
        Route.Method(method = "POST"),
        Route.Method(method = "PUT"),
        Route.Method(method = "DELETE"),
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (isEdit) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedMethod.method,
            onValueChange = {},
            readOnly = true,
            label = { Text("Method") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded && isEdit,
            onDismissRequest = { expanded = false }
        ) {
            methods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method.method) },
                    onClick = {
                        onMethodSelected(method)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDropdown(
    isEdit: Boolean = false,
    option: String,
    onAuthEnabled: (Boolean) -> Unit
) {
    val options = listOf("Yes", "No")
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(option) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Enable auth") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded && isEdit,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { auth ->
                DropdownMenuItem(
                    text = { Text(auth) },
                    onClick = {
                        selected = auth
                        onAuthEnabled(auth == "Yes")
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun getServiceByRoute(services: List<Service>, route: KateRoutes): Service?{
    return services.find { it.routes.contains(route.route) }
}

fun transformPath(path: String): String {
    val parts = path.trimStart('/').split('/')

    return buildString {
        parts.forEachIndexed { index, part ->
            when {
                part.startsWith("{") && part.endsWith("}") -> {
                    append("by ")
                    append(
                        part.trim('{', '}')
                            .replace('_', ' ')
                            .replaceFirstChar { it.uppercase() }
                    )
                }
                else -> {
                    if (index > 0) append(" ")
                    append(part.replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}
