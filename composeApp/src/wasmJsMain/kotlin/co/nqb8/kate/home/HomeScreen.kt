package co.nqb8.kate.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.nqb8.kate.*
import co.nqb8.kate.data.DashboardHome
import co.nqb8.kate.data.FlowChart
import co.nqb8.kate.data.LastRequestIssue
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    paddingValues: PaddingValues
){
    val viewModel = remember { HomeViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error){
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    if (state.isLoading){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            CircularProgressIndicator()
        }
    }else{
        state.data?.let {
            HomeContent(
                paddingValues = paddingValues,
                dashboardHome = it
            )
        }
    }
    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(top=16.dp))

}

@Composable
private fun HomeContent(
    paddingValues: PaddingValues,
    dashboardHome: DashboardHome
){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { Header() }
        item { DashboardMetrics(dashboardHome) }
        item { TrafficHeatmap(dashboardHome) }
        item { RecentAlerts(dashboardHome.recentRequestsWithIssue) }
        item { ActionButtons() }
    }
}

@Composable
private fun Header() {
    Text(
        "Home",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

// Section for the four key metrics
@Composable
fun DashboardMetrics(
    dashboardHome: DashboardHome
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetricCard(title = "Total APIs", value = dashboardHome.totalApiCount.toString(), modifier = Modifier.weight(1f))
        MetricCard(title = "24h Request Volume", value = dashboardHome.requestVolume.toString(), modifier = Modifier.weight(1f))
        MetricCard(title = "Average Latency", value = "${dashboardHome.averageLatency}ms", modifier = Modifier.weight(1f))
        MetricCard(title = "Error Rate", value = "${dashboardHome.errorRatePercent.roundToInt()}%", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Section for the Traffic Heatmap graph
@Composable
private fun TrafficHeatmap(
    dashboardHome: DashboardHome
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Traffic Heatmap", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Request Volume", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Text(dashboardHome.requestVolume.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Last 24 Hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text("+${dashboardHome.last24HourIncreasePercent.roundToInt()}%", style = MaterialTheme.typography.bodySmall, color = Color(0xFF34C759)) // Green color for positive change
            }
            Spacer(modifier = Modifier.height(24.dp))
            LineChart(dashboardHome.last7HoursFlow)
        }
    }
}

// A simple Canvas-based line chart to represent the graph
@Composable
private fun LineChart(
    flowCharts: List<FlowChart>
) {
    val pointz = remember {
        listOf(0.2f, 0.8f, 0.5f, 0.9f, 0.4f, 0.7f, 0.3f, 1.0f, 0.6f, 0.2f, 0.8f, 0.4f)
    }
    val points = flowCharts.map { it.value }
    val labels = flowCharts.map { it.title }
    val lineColor = MaterialTheme.colorScheme.primary
    val maxPoint = remember(points) { points.maxOrNull()?.toFloat() ?: 1f }

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val path = Path()
            val stepX = size.width / (points.size - 1)
            val stepY = size.height

            points.forEachIndexed { index, point ->
                // Normalize the point value to be between 0 and 1
                val normalizedY = point.toFloat() / maxPoint

                val x = stepX * index
                val y = size.height - (normalizedY * stepY * 0.8f + (stepY * 0.1f)) // Scale and offset for visual appeal

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevNormalizedY = points[index - 1].toFloat() / maxPoint
                    val prevX = stepX * (index - 1)
                    val prevY = size.height - (prevNormalizedY * stepY * 0.8f + (stepY * 0.1f))
                    val controlX1 = prevX + stepX / 2f
                    val controlY1 = prevY
                    val controlX2 = prevX + stepX / 2f
                    val controlY2 = y
                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                }
            }

            drawPath(
                path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.cornerPathEffect(16.dp.toPx()))
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { label ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Section for the Recent Alerts table
@Composable
fun RecentAlerts(
    recentRequestsWithIssue: List<LastRequestIssue>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Recent Alerts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Timestamp", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Severity", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Path", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Divider(color = MaterialTheme.colorScheme.outline)
                // Alert Items
                recentRequestsWithIssue.forEach {
                    AlertItem(it.time.toString(), it.responseStatus.toString(), it.path, Color(0xFFFF453A))
                }
//                AlertItem("2024-01-26 14:30", "High", "API 'User Service' exceeded rate limit", Color(0xFFFF453A))
//                AlertItem("2024-01-26 12:15", "Medium", "Slow response time for API 'Order Service'", Color(0xFFFF9F0A))
//                AlertItem("2024-01-26 10:00", "Low", "API 'Product Service' experiencing increased traffic", Color(0xFF0A84FF))
            }
        }
    }
}

@Composable
fun AlertItem(timestamp: String, severity: String, message: String, severityColor: Color) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(timestamp, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Box(modifier = Modifier.weight(1f)) {
                SeverityBadge(text = severity, color = severityColor)
            }
            Text(message, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
        }
        Divider(color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun SeverityBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

// Section for the bottom action buttons
@Composable
fun ActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { /*TODO*/ }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text("New Route")
        }
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedButton(onClick = { /*TODO*/ }, border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(
            MaterialTheme.colorScheme.outline)
        )) {
            Text("Set Rate Limit")
        }
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedButton(onClick = { /*TODO*/ }, border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(
            MaterialTheme.colorScheme.outline)
        )) {
            Text("View Logs")
        }
    }
}
