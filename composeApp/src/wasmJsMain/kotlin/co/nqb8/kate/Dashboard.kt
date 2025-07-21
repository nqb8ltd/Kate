package co.nqb8.kate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.nqb8.kate.home.HomeScreen
import co.nqb8.kate.routes.RoutesScreen
import co.nqb8.kate.traces.TracesScreen

//import co.nqb8.kate.traces.TracesScreen


enum class Dashboard {
    HOME, ROUTES, TRACES, ANALYTICS
}

@Composable
fun DashboardScreen() {
    var currentDashboard by remember { mutableStateOf(Dashboard.HOME) }
    var currentQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAdminBar(
                currentDashboard = currentDashboard,
                currentQuery = currentQuery,
                onTextChanged = { newQuery -> currentQuery = newQuery },
                onClick = { currentDashboard = it }
            )
        },
        content = { paddingValues ->
            when(currentDashboard){
                Dashboard.HOME -> HomeScreen(paddingValues)
                Dashboard.ROUTES -> RoutesScreen(currentQuery, paddingValues)
                Dashboard.TRACES -> TracesScreen(paddingValues)
                Dashboard.ANALYTICS -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary))
            }
        }
    )
}

// Top App Bar with Navigation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAdminBar(
    currentDashboard: Dashboard,
    currentQuery: String,
    onTextChanged: (String) -> Unit,
    onClick: (Dashboard) -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Api,
                    contentDescription = "Gateway Icon",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Kate Admin",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            NavigationItems(
                currentDashboard = currentDashboard,
                onClick = onClick
            )
            Spacer(modifier = Modifier.weight(1f))
            SearchBar(currentQuery = currentQuery, onTextChanged = onTextChanged)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(16.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun NavigationItems(
    currentDashboard: Dashboard = Dashboard.HOME,
    onClick: (Dashboard) -> Unit
) {
    Row(
        modifier = Modifier.padding(start = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        NavItem(
            text = "Home",
            Icons.Default.Home,
            selected = currentDashboard == Dashboard.HOME,
            onClick = { onClick(Dashboard.HOME) }
        )
        NavItem(
            text = "Routes",
            Icons.Default.Route,
            selected = currentDashboard == Dashboard.ROUTES,
            onClick = { onClick(Dashboard.ROUTES) }
        )
        NavItem(
            text = "Trace",
            Icons.Default.Notes,
            selected = currentDashboard == Dashboard.TRACES,
            onClick = { onClick(Dashboard.TRACES) }
        )
        NavItem(
            text = "Analytics",
            Icons.Default.Analytics,
            selected = currentDashboard == Dashboard.ANALYTICS,
            onClick = { onClick(Dashboard.ANALYTICS) }
        )
//        NavItem(
//            text = "Limits",
//            Icons.Default.Speed,
//            selected = currentDashboard == Dashboard.
//        )

    }
}

@Composable
private fun NavItem(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = contentColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = contentColor, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun SearchBar(
    currentQuery: String,
    onTextChanged: (String) -> Unit
) {

    TextField(
        value = currentQuery,
        onValueChange = {  onTextChanged(it) },
        placeholder = { Text("Search") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedContainerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.width(250.dp)
    )
}

