package com.example.colorpalette.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.colorpalette.monitoring.AppMonitor
import com.example.colorpalette.monitoring.HealthReport
import kotlinx.coroutines.launch

/**
 * Analytics Dashboard Screen
 * Displays comprehensive app health metrics and analytics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    appMonitor: AppMonitor,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var healthReport by remember { mutableStateOf<HealthReport?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    
    // Load analytics data
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                healthReport = appMonitor.getHealthReport()
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Clear all data button
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Clear All Data"
                        )
                    }
                    // Refresh button
                    IconButton(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                try {
                                    healthReport = appMonitor.getHealthReport()
                                    isLoading = false
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingView()
                }
                errorMessage != null -> {
                    ErrorView(errorMessage!!)
                }
                healthReport != null -> {
                    AnalyticsContent(healthReport!!)
                }
            }
        }
        
        // Clear data confirmation dialog
        if (showClearDialog) {
            ClearDataDialog(
                onDismiss = { showClearDialog = false },
                onConfirm = {
                    scope.launch {
                        try {
                            appMonitor.clearAllData()
                            healthReport = appMonitor.getHealthReport()
                            showClearDialog = false
                        } catch (e: Exception) {
                            errorMessage = e.message
                            showClearDialog = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading analytics...")
        }
    }
}

@Composable
private fun ErrorView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error loading analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnalyticsContent(report: HealthReport) {
    // Check if data is empty
    val isEmpty = report.totalSessions == 0 && report.totalFeedback == 0
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            HeaderCard()
        }
        
        // Empty state info card
        if (isEmpty) {
            item {
                EmptyDataInfoCard()
            }
        }
        
        // Primary Metrics
        item {
            SectionTitle("Primary Metrics", Icons.Default.MonitorHeart)
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Crash Rate",
                    value = String.format("%.2f%%", report.crashRate),
                    icon = Icons.Default.Warning,
                    color = getHealthColor(report.crashRate, inverse = true),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Error Rate",
                    value = String.format("%.2f%%", report.errorRate),
                    icon = Icons.Default.ErrorOutline,
                    color = getHealthColor(report.errorRate, inverse = true),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Performance Metrics
        item {
            SectionTitle("Performance", Icons.Default.Speed)
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Avg Session",
                    value = formatDuration(report.avgSessionLength),
                    icon = Icons.Default.Timer,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "App Start",
                    value = String.format("%.0fms", report.avgAppStartTime),
                    icon = Icons.Default.RocketLaunch,
                    color = getPerformanceColor(report.avgAppStartTime),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // User Satisfaction
        item {
            SectionTitle("User Satisfaction", Icons.Default.Favorite)
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "CSI Score",
                    value = String.format("%.1f%%", report.csi),
                    icon = Icons.Default.Star,
                    color = getSatisfactionColor(report.csi),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "NPS",
                    value = String.format("%.0f", report.nps),
                    icon = Icons.Default.ThumbUp,
                    color = getNPSColor(report.nps),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            MetricCard(
                title = "Retention (7 days)",
                value = String.format("%.1f%%", report.retentionRate7Days),
                icon = Icons.Default.People,
                color = getHealthColor(report.retentionRate7Days),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Totals
        item {
            SectionTitle("Statistics", Icons.Default.BarChart)
        }
        
        item {
            StatisticsCard(report)
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "App Health Dashboard",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Last 7 days overview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EmptyDataInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "No Data Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Start using the app to collect real analytics!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "📊 How metrics are collected:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MetricInfoRow("🚀", "App starts & session duration tracked automatically")
                MetricInfoRow("🎨", "Each image color extraction is recorded")
                MetricInfoRow("⭐", "User feedback collected via feedback button")
                MetricInfoRow("❌", "Errors & crashes logged for quality monitoring")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "💡 All metrics update in real-time as you use the app. Close and reopen the app a few times, extract colors, and leave feedback to see the analytics!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun MetricInfoRow(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatisticsCard(report: HealthReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            StatItem("Total Sessions", report.totalSessions.toString(), Icons.Default.PlayArrow)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            StatItem("Total Crashes", report.totalCrashes.toString(), Icons.Default.BugReport)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            StatItem("Total Errors", report.totalErrors.toString(), Icons.Default.Error)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            StatItem("Total Feedback", report.totalFeedback.toString(), Icons.Default.Feedback)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Helper functions for color coding
private fun getHealthColor(value: Double, inverse: Boolean = false): Color {
    return when {
        inverse -> {
            when {
                value < 1.0 -> Color(0xFF4CAF50) // Green - good
                value < 5.0 -> Color(0xFFFFA726) // Orange - warning
                else -> Color(0xFFEF5350) // Red - bad
            }
        }
        else -> {
            when {
                value > 80.0 -> Color(0xFF4CAF50) // Green - good
                value > 50.0 -> Color(0xFFFFA726) // Orange - warning
                else -> Color(0xFFEF5350) // Red - bad
            }
        }
    }
}

private fun getPerformanceColor(milliseconds: Double): Color {
    return when {
        milliseconds < 1000 -> Color(0xFF4CAF50) // Green - fast
        milliseconds < 3000 -> Color(0xFFFFA726) // Orange - acceptable
        else -> Color(0xFFEF5350) // Red - slow
    }
}

private fun getSatisfactionColor(percentage: Double): Color {
    return when {
        percentage > 80.0 -> Color(0xFF4CAF50) // Green - excellent
        percentage > 60.0 -> Color(0xFFFFA726) // Orange - good
        else -> Color(0xFFEF5350) // Red - needs improvement
    }
}

private fun getNPSColor(score: Double): Color {
    return when {
        score > 50.0 -> Color(0xFF4CAF50) // Green - excellent
        score > 0.0 -> Color(0xFFFFA726) // Orange - good
        else -> Color(0xFFEF5350) // Red - needs improvement
    }
}

private fun formatDuration(milliseconds: Double): String {
    val seconds = (milliseconds / 1000).toInt()
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}

@Composable
private fun ClearDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Clear All Analytics Data?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "This will permanently delete all collected analytics data:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ClearItemRow("All sessions")
                    ClearItemRow("All crashes and errors")
                    ClearItemRow("All performance metrics")
                    ClearItemRow("All feature usage data")
                    ClearItemRow("All user feedback")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "⚠️ This action cannot be undone!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ClearItemRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

