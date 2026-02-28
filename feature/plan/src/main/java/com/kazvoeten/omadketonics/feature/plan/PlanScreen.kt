package com.kazvoeten.omadketonics.feature.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.SentimentSatisfied
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material.icons.rounded.SentimentVerySatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.MacroAverages
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val BgColor = Color(0xFF121214)
private val CardBg = Color(0xFF1C1C20)
private val CardBorder = Color(0xFF2A2A30)
private val Primary = Color(0xFFC4D0FF)
private val TextMain = Color.White
private val TextMuted = Color(0xFF8E8E99)
private val CarbsColor = Color(0xFF7CE0BB)
private val ProteinColor = Color(0xFFFFE285)
private val FatColor = Color(0xFFFF91A4)
private val SuccessColor = Color(0xFF4ADE80)

@Composable
fun PlanRoute(
    viewModel: PlanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showCheatDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PlanEffect.Message -> snackbarHostState.showSnackbar(effect.value)
            }
        }
    }

    if (showCheatDialog) {
        CheatMealDialog(
            onDismiss = { showCheatDialog = false },
            onSave = { name, calories, protein, carbs, fat ->
                viewModel.onEvent(
                    PlanUiEvent.LogCheatMeal(
                        name = name,
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fat = fat,
                    ),
                )
                showCheatDialog = false
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "THIS WEEK'S OMAD",
                    color = TextMain,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
            }

            item {
                AveragesCard(averages = state.averages)
            }

            item {
                MoodCard(
                    selectedMood = state.todayMood,
                    onSelectMood = { mood ->
                        viewModel.onEvent(PlanUiEvent.SetMood(mood))
                        scope.launch {
                            snackbarHostState.showSnackbar("Mood: ${mood.label}")
                        }
                    },
                    onClearMood = {
                        viewModel.onEvent(PlanUiEvent.SetMood(null))
                        scope.launch {
                            snackbarHostState.showSnackbar("Mood cleared")
                        }
                    },
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionsRow(
                        enabled = state.isViewingCurrentWeek,
                        onLogCheat = { showCheatDialog = true },
                        onRegenerate = { viewModel.onEvent(PlanUiEvent.GenerateWeek) },
                    )
                    if (!state.isViewingCurrentWeek) {
                        Text(
                            text = "Historical weeks are read-only.",
                            color = TextMuted,
                            fontSize = 11.sp,
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Meals",
                    color = TextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            itemsIndexed(state.meals, key = { _, meal -> meal.recipeId }) { index, meal ->
                MealCard(
                    mealIndex = index + 1,
                    meal = meal,
                    enabled = state.isViewingCurrentWeek,
                    onToggleEaten = {
                        viewModel.onEvent(
                            PlanUiEvent.SetMealEaten(
                                recipeId = meal.recipeId,
                                eaten = !meal.isEaten,
                            ),
                        )
                    },
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 98.dp),
            snackbar = { data ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 8.dp,
                ) {
                    Text(
                        text = data.visuals.message,
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                }
            },
        )
    }
}

@Composable
private fun AveragesCard(averages: MacroAverages) {
    val total = max(1, averages.protein + averages.carbs + averages.fat).toFloat()
    val carbsPct = ((averages.carbs / total) * 100f).roundToInt()
    val proteinPct = ((averages.protein / total) * 100f).roundToInt()
    val fatPct = ((averages.fat / total) * 100f).roundToInt()

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PLAN DAILY AVERAGES",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
                Text(
                    text = "${averages.calories} kcal",
                    color = TextMain,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroLegendRow(label = "Carbs", value = "$carbsPct%", color = CarbsColor)
                    MacroLegendRow(label = "Protein", value = "$proteinPct%", color = ProteinColor)
                    MacroLegendRow(label = "Fat", value = "$fatPct%", color = FatColor)
                }
            }

            MacroRings(
                protein = averages.protein,
                carbs = averages.carbs,
                fat = averages.fat,
            )
        }
    }
}

@Composable
private fun MacroLegendRow(
    label: String,
    value: String,
    color: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = "$label $value",
            color = TextMuted,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun MacroRings(
    protein: Int,
    carbs: Int,
    fat: Int,
) {
    val total = max(1, protein + carbs + fat).toFloat()
    val fatSweep = (fat / total) * 360f
    val proteinSweep = (protein / total) * 360f
    val carbsSweep = (carbs / total) * 360f

    Canvas(
        modifier = Modifier.size(110.dp),
    ) {
        drawRingArc(
            color = FatColor,
            startAngle = -45f,
            sweep = fatSweep,
            scale = 1f,
            stroke = 12.dp.toPx(),
        )
        drawRingArc(
            color = ProteinColor,
            startAngle = 20f,
            sweep = proteinSweep,
            scale = 0.74f,
            stroke = 12.dp.toPx(),
        )
        drawRingArc(
            color = CarbsColor,
            startAngle = 45f,
            sweep = carbsSweep,
            scale = 0.48f,
            stroke = 12.dp.toPx(),
        )
    }
}

private fun DrawScope.drawRingArc(
    color: Color,
    startAngle: Float,
    sweep: Float,
    scale: Float,
    stroke: Float,
) {
    val ringSize = size.minDimension * scale
    val topLeft = Offset(
        x = (size.width - ringSize) / 2f,
        y = (size.height - ringSize) / 2f,
    )
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweep.coerceAtLeast(12f),
        useCenter = false,
        topLeft = topLeft,
        size = Size(ringSize, ringSize),
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
}

@Composable
private fun MoodCard(
    selectedMood: DailyMood?,
    onSelectMood: (DailyMood) -> Unit,
    onClearMood: () -> Unit,
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "TODAY'S MOOD",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
            TextButton(
                onClick = onClearMood,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(30.dp)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                contentPadding = PaddingValues(horizontal = 10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Clear",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DailyMood.entries.forEach { mood ->
                val isActive = selectedMood == mood
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 2.dp,
                            color = if (isActive) CarbsColor else Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                        )
                        .background(if (isActive) CarbsColor.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onSelectMood(mood) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = moodIcon(mood),
                        contentDescription = mood.label,
                        tint = if (isActive) TextMain else TextMuted,
                        modifier = Modifier
                            .size(30.dp)
                            .alpha(if (isActive) 1f else 0.45f),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = DailyMood.Terrible.label,
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp),
            )
            Text(
                text = DailyMood.Low.label,
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp),
            )
            Text(
                text = selectedMood?.label ?: "Select Mood",
                color = if (selectedMood == null) TextMuted else TextMain,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = DailyMood.Good.label,
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp),
            )
            Text(
                text = DailyMood.Great.label,
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp),
            )
        }
    }
}

@Composable
private fun ActionsRow(
    enabled: Boolean,
    onLogCheat: () -> Unit,
    onRegenerate: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onLogCheat,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = BgColor,
                disabledContainerColor = Primary.copy(alpha = 0.35f),
                disabledContentColor = BgColor.copy(alpha = 0.65f),
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Fastfood,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Cheat", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onRegenerate,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = BgColor,
                disabledContainerColor = Primary.copy(alpha = 0.35f),
                disabledContentColor = BgColor.copy(alpha = 0.65f),
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Autorenew,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Regenerate", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MealCard(
    mealIndex: Int,
    meal: PlanMealItemUi,
    enabled: Boolean,
    onToggleEaten: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (meal.isEaten) CardBg.copy(alpha = 0.6f) else CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CardBorder),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mealIndex.toString(),
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name,
                    color = TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${meal.calories} kcal",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
                )
                MealMacroBar(
                    protein = meal.protein,
                    carbs = meal.carbs,
                    fat = meal.fat,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${meal.carbs}g Carbs",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${meal.fat}g Fat",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            MealVisual(
                index = mealIndex,
                isCompleted = meal.isEaten,
                enabled = enabled,
                onToggle = onToggleEaten,
            )
        }
    }
}

@Composable
private fun MealMacroBar(
    protein: Int,
    carbs: Int,
    fat: Int,
) {
    val total = max(1, protein + carbs + fat).toFloat()
    val carbsWeight = max(0.001f, carbs / total)
    val proteinWeight = max(0.001f, protein / total)
    val fatWeight = max(0.001f, fat / total)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(CardBorder),
    ) {
        Box(
            modifier = Modifier
                .weight(carbsWeight)
                .fillMaxHeight()
                .background(CarbsColor),
        )
        Box(
            modifier = Modifier
                .weight(proteinWeight)
                .fillMaxHeight()
                .background(ProteinColor),
        )
        Box(
            modifier = Modifier
                .weight(fatWeight)
                .fillMaxHeight()
                .background(FatColor),
        )
    }
}

@Composable
private fun MealVisual(
    index: Int,
    isCompleted: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Box(
        modifier = Modifier.size(width = 74.dp, height = 76.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CardBorder)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = mealIcon(index),
                contentDescription = null,
                tint = if (isCompleted) TextMuted else Primary,
                modifier = Modifier.size(34.dp),
            )
        }

        Surface(
            color = if (isCompleted) SuccessColor else Primary,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .alpha(if (enabled) 1f else 0.5f)
                .clip(RoundedCornerShape(12.dp))
                .then(if (enabled) Modifier.clickable { onToggle() } else Modifier),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = if (isCompleted) "Completed" else "Mark Eaten",
                    tint = if (isCompleted) Color.White else Color.Black,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun AppCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }
}

private fun moodIcon(mood: DailyMood): ImageVector = when (mood) {
    DailyMood.Terrible -> Icons.Rounded.SentimentVeryDissatisfied
    DailyMood.Low -> Icons.Rounded.SentimentDissatisfied
    DailyMood.Okay -> Icons.Rounded.SentimentNeutral
    DailyMood.Good -> Icons.Rounded.SentimentSatisfied
    DailyMood.Great -> Icons.Rounded.SentimentVerySatisfied
}

private fun mealIcon(index: Int): ImageVector = when ((index - 1) % 3) {
    0 -> Icons.Rounded.Restaurant
    1 -> Icons.Rounded.LocalDining
    else -> Icons.Rounded.Fastfood
}

@Composable
private fun CheatMealDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, calories: Int, protein: Int, carbs: Int, fat: Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Cheat Meal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Meal name") })
                OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Calories") })
                OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("Protein") })
                OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Carbs") })
                OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Fat") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        name,
                        calories.toIntOrNull() ?: 0,
                        protein.toIntOrNull() ?: 0,
                        carbs.toIntOrNull() ?: 0,
                        fat.toIntOrNull() ?: 0,
                    )
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
