package com.kazvoeten.omadketonics.feature.progress

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazvoeten.omadketonics.model.DailyHealthSummary
import com.kazvoeten.omadketonics.model.DayTrend
import com.kazvoeten.omadketonics.model.HealthAvailability
import com.kazvoeten.omadketonics.model.InsightRange
import com.kazvoeten.omadketonics.model.ProgressDeepLinkMetric
import com.kazvoeten.omadketonics.model.ProgressInsights
import java.time.format.DateTimeFormatter
import java.util.Locale

private val BgColor = Color(0xFF0F172A)
private val CardBg = Color(0xFF1E293B)
private val CardBorder = Color(0xFF334155)
private val Primary = Color(0xFF2DD4BF)
private val SleepBlue = Color(0xFF818CF8)
private val ExerciseRed = Color(0xFFF87171)
private val Success = Color(0xFF22C55E)
private val TextMain = Color(0xFFF8FAFC)
private val TextMuted = Color(0xFF94A3B8)
private const val HealthConnectProviderPackage = "com.google.android.apps.healthdata"

@Composable
fun ProgressRoute(
    viewModel: ProgressViewModel = hiltViewModel(),
    openMetric: ProgressDeepLinkMetric? = null,
    openActivityLogger: Boolean = false,
    onDeepLinkConsumed: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val requiredPermissions = viewModel.requiredPermissions()

    val permissionsLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        Log.d("HealthConnect", "Progress permission result: granted=${granted.size} required=${requiredPermissions.size}")
        viewModel.onEvent(ProgressUiEvent.HealthPermissionsResult(granted))
        if (!granted.containsAll(requiredPermissions)) {
            runCatching {
                context.startActivity(
                    HealthConnectClient.getHealthConnectManageDataIntent(
                        context,
                        HealthConnectProviderPackage,
                    ),
                )
            }.onFailure {
                Toast.makeText(context, "Open Health Connect settings to grant permissions", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val requestPermissions: (Set<String>) -> Unit = { permissions ->
        Log.d("HealthConnect", "Progress connect tapped")
        runCatching {
            permissionsLauncher.launch(permissions)
        }.onFailure {
            runCatching {
                context.startActivity(
                    HealthConnectClient.getHealthConnectManageDataIntent(
                        context,
                        HealthConnectProviderPackage,
                    ),
                )
            }.onFailure {
                Toast.makeText(context, "Unable to open Health Connect settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProgressUiEvent.TabOpened)
        viewModel.effects.collect { effect ->
            when (effect) {
                is ProgressEffect.Message -> Toast.makeText(context, effect.value, Toast.LENGTH_SHORT).show()
                is ProgressEffect.RequestHealthPermissions -> requestPermissions(effect.permissions)
            }
        }
    }

    LaunchedEffect(openMetric, openActivityLogger) {
        if (openMetric != null) {
            viewModel.onEvent(ProgressUiEvent.OpenMetric(openMetric.toUiMetric()))
            if (openActivityLogger) {
                viewModel.onEvent(ProgressUiEvent.ShowActivityLogger)
            }
            onDeepLinkConsumed()
        }
    }

    if (state.showActivityLogger) {
        ActivityLogDialog(
            state = state,
            onDismiss = { viewModel.onEvent(ProgressUiEvent.DismissActivityLogger) },
            onSave = { viewModel.onEvent(ProgressUiEvent.SaveActivityLog) },
            onTypeChanged = { viewModel.onEvent(ProgressUiEvent.UpdateActivityType(it)) },
            onDurationChanged = { viewModel.onEvent(ProgressUiEvent.UpdateActivityDuration(it)) },
            onExertionChanged = { viewModel.onEvent(ProgressUiEvent.UpdateActivityExertion(it)) },
            onCaloriesOverrideChanged = { viewModel.onEvent(ProgressUiEvent.UpdateActivityCaloriesOverride(it)) },
            onNotesChanged = { viewModel.onEvent(ProgressUiEvent.UpdateActivityNotes(it)) },
        )
    }

    if (state.selectedMetric != null) {
        MetricDetailDialog(
            state = state,
            onClose = { viewModel.onEvent(ProgressUiEvent.CloseMetric) },
            onRangeSelected = { viewModel.onEvent(ProgressUiEvent.SelectRange(it)) },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Primary),
                )
                Text(
                    text = "INSIGHTS",
                    color = TextMain,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        item {
            ConnectionCard(
                state = state,
                onConnect = { requestPermissions(viewModel.requiredPermissions()) },
                onRefresh = { viewModel.onEvent(ProgressUiEvent.RefreshSync) },
            )
        }

        item {
            InsightRingsCard(
                insights = state.weeklyInsights,
                onOpen = { viewModel.onEvent(ProgressUiEvent.OpenMetric(ProgressMetric.ActivityRings)) },
            )
        }

        item {
            ActivityLoadCard(
                summaries = state.weeklyHealthSummaries,
                insights = state.weeklyInsights,
                onOpen = { viewModel.onEvent(ProgressUiEvent.OpenMetric(ProgressMetric.ExerciseLoad)) },
            )
        }

        item {
            WeightTrendCard(
                trend = state.weeklyTrend,
                onOpen = { viewModel.onEvent(ProgressUiEvent.OpenMetric(ProgressMetric.WeightTrend)) },
            )
        }

        item {
            CalorieConsistencyCard(
                trend = state.weeklyTrend,
                insights = state.weeklyInsights,
                onOpen = { viewModel.onEvent(ProgressUiEvent.OpenMetric(ProgressMetric.CalorieConsistency)) },
            )
        }

        item {
            SleepEnergyCard(
                summaries = state.weeklyHealthSummaries,
                insights = state.weeklyInsights,
                onOpen = { viewModel.onEvent(ProgressUiEvent.OpenMetric(ProgressMetric.SleepQuality)) },
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Manual Activity", color = TextMain, fontWeight = FontWeight.Bold)
                        Text(
                            "${state.manualActivityLogs.size} logs",
                            color = TextMuted,
                            fontSize = 11.sp,
                        )
                    }
                    Button(
                        onClick = { viewModel.onEvent(ProgressUiEvent.ShowActivityLogger) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = BgColor,
                        ),
                    ) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Session", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (state.manualActivityLogs.isNotEmpty()) {
            items(state.manualActivityLogs.take(5), key = { it.id }) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(log.activityType, color = TextMain, fontWeight = FontWeight.SemiBold)
                            Text("${log.calories} kcal • RPE ${log.exertion}", color = TextMuted, fontSize = 12.sp)
                        }
                        Text(log.outboxStatus.name.uppercase(Locale.US), color = if (log.outboxStatus.name == "Synced") Success else TextMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionCard(
    state: ProgressUiState,
    onConnect: () -> Unit,
    onRefresh: () -> Unit,
) {
    val connection = state.connectionState
    val availabilityMessage = when (connection.availability) {
        HealthAvailability.Unavailable -> "Health Connect unavailable on this device"
        HealthAvailability.ProviderUpdateRequired -> "Update Health Connect to enable sync"
        HealthAvailability.Available -> if (connection.hasPermissions) "Health Connect linked" else "Permissions required"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Health Connect", color = TextMain, fontWeight = FontWeight.Bold)
                Text("Queue: ${connection.pendingOutboxCount}", color = TextMuted, fontSize = 11.sp)
            }
            Text(availabilityMessage, color = TextMuted, fontSize = 12.sp)
            val lastSynced = connection.lastSyncedAtEpochMillis
            if (lastSynced != null) {
                Text("Last sync: ${formatTime(lastSynced)}", color = TextMuted, fontSize = 11.sp)
            }
            val errorMessage = connection.lastError
            if (errorMessage != null) {
                Text(errorMessage, color = ExerciseRed, fontSize = 11.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!connection.hasPermissions) {
                    Button(
                        onClick = onConnect,
                        enabled = connection.availability == HealthAvailability.Available,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = BgColor,
                        ),
                    ) {
                        Text("Connect")
                    }
                }
                Button(
                    onClick = onRefresh,
                    enabled = connection.availability == HealthAvailability.Available && connection.hasPermissions,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardBorder,
                        contentColor = TextMain,
                    ),
                ) {
                    Icon(Icons.Rounded.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (connection.isSyncing) "Syncing" else "Refresh")
                }
            }
        }
    }
}

@Composable
private fun InsightRingsCard(
    insights: ProgressInsights?,
    onOpen: () -> Unit,
) {
    val completion = insights?.recoveryScore ?: 0
    val movePct = (insights?.weeklyActiveCalories ?: 0) / 600f / 7f
    val exercisePct = (insights?.weeklyExerciseMinutes ?: 0) / 45f / 7f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text("Streak Multiplier", color = TextMain, fontWeight = FontWeight.Bold)
                Text("Recovery score $completion%", color = TextMuted, fontSize = 12.sp)
                LinearProgressIndicator(
                    progress = { movePct.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = Primary,
                    trackColor = CardBorder,
                )
                LinearProgressIndicator(
                    progress = { exercisePct.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = ExerciseRed,
                    trackColor = CardBorder,
                )
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
private fun ActivityLoadCard(
    summaries: List<DailyHealthSummary>,
    insights: ProgressInsights?,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Activity Load", color = TextMain, fontWeight = FontWeight.Bold)
                Text("${insights?.activityIntensityDeltaPct ?: 0}%", color = ExerciseRed, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                summaries.takeLast(7).forEach { day ->
                    val intensity = intensityLevel(day)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (intensity) {
                                    2 -> ExerciseRed
                                    1 -> Primary
                                    else -> CardBorder
                                },
                            ),
                    )
                }
            }
            Text("Last 7 days metabolic effort", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun WeightTrendCard(
    trend: List<DayTrend>,
    onOpen: () -> Unit,
) {
    val currentWeight = trend.lastOrNull()?.weight
    val previousWeight = trend.dropLast(1).lastOrNull()?.weight
    val delta = if (currentWeight != null && previousWeight != null) currentWeight - previousWeight else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Current Weight", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                text = if (currentWeight != null) String.format(Locale.US, "%.1f kg", currentWeight) else "-",
                color = TextMain,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
            )
            Text(
                text = String.format(Locale.US, "%+.1f kg", delta),
                color = if (delta <= 0f) Success else ExerciseRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
            MiniBarChart(
                values = trend.map { it.weight ?: 0f },
                labels = trend.map { it.dayLabel.take(1) },
                color = Success,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun CalorieConsistencyCard(
    trend: List<DayTrend>,
    insights: ProgressInsights?,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Calorie Intake", color = TextMain, fontWeight = FontWeight.Bold)
            MiniBarChart(
                values = trend.map { it.calories.toFloat() },
                labels = trend.map { it.dayLabel.take(1) },
                color = Primary,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                "Weekly average: ${insights?.calorieAverage ?: 0} kcal",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun SleepEnergyCard(
    summaries: List<DailyHealthSummary>,
    insights: ProgressInsights?,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Sleep vs Energy Flow", color = TextMain, fontWeight = FontWeight.Bold)
            MiniBarChart(
                values = summaries.takeLast(7).map { it.sleep.totalSleepMinutes.toFloat() / 60f },
                labels = summaries.takeLast(7).map { it.date.dayOfWeek.name.take(1) },
                color = SleepBlue,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text("Recovery score: ${insights?.recoveryScore ?: 0}%", color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MiniBarChart(
    values: List<Float>,
    labels: List<String>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val max = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEachIndexed { index, value ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((72f * (value / max).coerceIn(0.08f, 1f)).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                        .background(color),
                )
                Text(
                    text = labels.getOrElse(index) { "" },
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun MetricDetailDialog(
    state: ProgressUiState,
    onClose: () -> Unit,
    onRangeSelected: (InsightRange) -> Unit,
) {
    val title = when (state.selectedMetric) {
        ProgressMetric.ActivityRings -> "Activity Rings"
        ProgressMetric.ExerciseLoad -> "Exercise Load"
        ProgressMetric.WeightTrend -> "Weight Trend"
        ProgressMetric.CalorieConsistency -> "Calorie Consistency"
        ProgressMetric.SleepQuality -> "Sleep Quality"
        null -> ""
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(title, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RangeChip(
                        text = "Day",
                        selected = state.selectedRange == InsightRange.Day,
                        onClick = { onRangeSelected(InsightRange.Day) },
                    )
                    RangeChip(
                        text = "Week",
                        selected = state.selectedRange == InsightRange.Week,
                        onClick = { onRangeSelected(InsightRange.Week) },
                    )
                    RangeChip(
                        text = "Month",
                        selected = state.selectedRange == InsightRange.Month,
                        onClick = { onRangeSelected(InsightRange.Month) },
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                when (state.selectedMetric) {
                    ProgressMetric.ActivityRings -> MetricTextBlock(
                        title = "Completion",
                        value = "${(state.weeklyInsights?.recoveryScore ?: 0)}%",
                        summary = state.weeklyInsights?.narrativeActivity.orEmpty(),
                    )

                    ProgressMetric.ExerciseLoad -> MetricTextBlock(
                        title = "Volume",
                        value = "${state.weeklyInsights?.weeklyExerciseMinutes ?: 0} min",
                        summary = state.weeklyInsights?.narrativeActivity.orEmpty(),
                    )

                    ProgressMetric.WeightTrend -> {
                        val trend = when (state.selectedRange) {
                            InsightRange.Day -> state.weeklyTrend.takeLast(1)
                            InsightRange.Week -> state.weeklyTrend
                            InsightRange.Month -> state.weeklyTrend
                        }
                        MetricTextBlock(
                            title = "Weight entries",
                            value = "${trend.count { it.weight != null }} days",
                            summary = "Keep daily measurements consistent for cleaner trend analysis.",
                        )
                    }

                    ProgressMetric.CalorieConsistency -> MetricTextBlock(
                        title = "Average intake",
                        value = "${state.weeklyInsights?.calorieAverage ?: 0} kcal",
                        summary = state.weeklyInsights?.narrativeCalories.orEmpty(),
                    )

                    ProgressMetric.SleepQuality -> MetricTextBlock(
                        title = "Recovery",
                        value = "${state.weeklyInsights?.recoveryScore ?: 0}%",
                        summary = state.weeklyInsights?.narrativeSleep.orEmpty(),
                    )

                    null -> Unit
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("Close")
            }
        },
    )
}

@Composable
private fun RangeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Primary.copy(alpha = 0.2f) else CardBorder.copy(alpha = 0.7f))
            .border(1.dp, if (selected) Primary else CardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text, color = if (selected) Primary else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetricTextBlock(
    title: String,
    value: String,
    summary: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Text(value, color = TextMain, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Primary.copy(alpha = 0.12f))
                .padding(12.dp),
        ) {
            Text(summary, color = TextMain, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ActivityLogDialog(
    state: ProgressUiState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onTypeChanged: (String) -> Unit,
    onDurationChanged: (String) -> Unit,
    onExertionChanged: (String) -> Unit,
    onCaloriesOverrideChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Activity") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.activityForm.activityType,
                    onValueChange = onTypeChanged,
                    label = { Text("Type") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.activityForm.durationMinutes,
                    onValueChange = onDurationChanged,
                    label = { Text("Duration (min)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.activityForm.exertion,
                    onValueChange = onExertionChanged,
                    label = { Text("Exertion (1-10)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.activityForm.caloriesOverride,
                    onValueChange = onCaloriesOverrideChanged,
                    label = { Text("Calories override (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.activityForm.notes,
                    onValueChange = onNotesChanged,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Log Activity")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun intensityLevel(summary: DailyHealthSummary): Int {
    return when {
        summary.activity.activeCalories >= 500 || summary.activity.exerciseMinutes >= 60 -> 2
        summary.activity.activeCalories >= 250 || summary.activity.exerciseMinutes >= 30 -> 1
        else -> 0
    }
}

private fun formatTime(epochMillis: Long): String {
    return DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.US)
        .format(java.time.Instant.ofEpochMilli(epochMillis).atZone(java.time.ZoneId.systemDefault()))
}

private fun ProgressDeepLinkMetric.toUiMetric(): ProgressMetric = when (this) {
    ProgressDeepLinkMetric.ActivityRings -> ProgressMetric.ActivityRings
    ProgressDeepLinkMetric.ExerciseLoad -> ProgressMetric.ExerciseLoad
    ProgressDeepLinkMetric.WeightTrend -> ProgressMetric.WeightTrend
    ProgressDeepLinkMetric.CalorieConsistency -> ProgressMetric.CalorieConsistency
    ProgressDeepLinkMetric.SleepQuality -> ProgressMetric.SleepQuality
}
