package com.kazvoeten.omadketonics.feature.progress

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazvoeten.omadketonics.model.ChartType
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProgressRoute(
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var weightInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ProgressEffect.Message -> Toast.makeText(context, effect.value, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("My Progress", fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("Track weight, meals, and trends", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Today's Weight", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("kg") },
                            modifier = Modifier.weight(1f),
                        )
                        Button(onClick = { viewModel.onEvent(ProgressUiEvent.SaveWeight(weightInput)) }) {
                            Text("Save")
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("7-Day Trend", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.onEvent(ProgressUiEvent.SetChartType(ChartType.Weight)) }) {
                            Text("Weight")
                        }
                        Button(onClick = { viewModel.onEvent(ProgressUiEvent.SetChartType(ChartType.Calories)) }) {
                            Text("Calories")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    state.trend.forEach { day ->
                        val value = if (state.chartType == ChartType.Weight) {
                            day.weight?.let { String.format(Locale.US, "%.1f kg", it) } ?: "-"
                        } else {
                            "${day.calories} kcal"
                        }
                        Text("${day.dayLabel}: $value", fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Text("Recent Meals", fontWeight = FontWeight.Bold)
        }

        if (state.mealHistory.isEmpty()) {
            item {
                Text("No meals logged yet")
            }
        } else {
            items(state.mealHistory, key = { it.date.toString() }) { entry ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(
                            entry.date.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(entry.name)
                        Text("${entry.calories} kcal")
                    }
                }
            }
        }
    }
}
