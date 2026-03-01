package com.kazvoeten.omadketonics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kazvoeten.omadketonics.model.Ingredient
import kotlin.math.max
import kotlin.math.roundToInt

private val CarbsMacroColor = Color(0xFF7CE0BB)
private val ProteinMacroColor = Color(0xFFFFE285)
private val FatMacroColor = Color(0xFFFF91A4)
private val EggRegex = Regex("\\beggs?\\b", RegexOption.IGNORE_CASE)
private const val GramsPerEgg = 50f

@Composable
fun RecipeDetailsDialog(
    title: String,
    calories: Int,
    protein: Int,
    carbs: Int,
    fat: Int,
    ingredients: List<Ingredient>,
    instructions: List<String>,
    recipeImageUri: String? = null,
    recipeIcon: String = "\uD83C\uDF7D\uFE0F",
    weekAverageCalories: Int,
    inCurrentPlan: Boolean,
    canAddToWeek: Boolean,
    onAddToWeek: (() -> Unit)?,
    onEdit: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 920.dp)
                .heightIn(max = 760.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (inCurrentPlan) "In current week plan" else "Not in current week plan",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            RecipeImage(
                                imageUri = recipeImageUri,
                                fallbackIcon = recipeIcon,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp),
                                iconFontSize = 54.sp,
                            )
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = CardDefaults.outlinedCardBorder(),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Overview", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "$calories kcal",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                                Text(
                                    text = "${protein}g protein | ${carbs}g carbs | ${fat}g fat",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                                Text(
                                    text = "Compared to week average",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                                )
                                WeekCalorieBar(
                                    calories = calories,
                                    weekAverageCalories = weekAverageCalories,
                                )
                                Text(
                                    text = "Macros",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                                )
                                RecipeMacroOverview(
                                    protein = protein,
                                    carbs = carbs,
                                    fat = fat,
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = CardDefaults.outlinedCardBorder(),
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Ingredients", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (ingredients.isEmpty()) {
                                    Text("No ingredients listed.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                } else {
                                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                        val useTwoColumns = maxWidth >= 340.dp
                                        val rows = if (useTwoColumns) {
                                            ingredients.chunked(2)
                                        } else {
                                            ingredients.map { listOf(it) }
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            rows.forEach { rowItems ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.Top,
                                                ) {
                                                    rowItems.forEach { ingredient ->
                                                        IngredientCell(
                                                            ingredient = ingredient,
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                    }
                                                    if (useTwoColumns && rowItems.size == 1) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = CardDefaults.outlinedCardBorder(),
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Instructions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (instructions.isEmpty()) {
                                    Text("No instructions listed.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                } else {
                                    instructions.forEachIndexed { index, step ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                            Text(
                                                text = step,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(top = 1.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (canAddToWeek && !inCurrentPlan && onAddToWeek != null) {
                        TextButton(onClick = onAddToWeek) {
                            Text("Add To Week")
                        }
                    }
                    if (onEdit != null) {
                        TextButton(onClick = onEdit) {
                            Text("Edit")
                        }
                    }
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientCell(
    ingredient: Ingredient,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(6.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = "${ingredient.name} (${formatIngredientAmount(ingredient)})",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatIngredientAmount(ingredient: Ingredient): String {
    val grams = ingredient.amountGrams.roundToInt().coerceAtLeast(0)
    if (grams == 0) return "0g"
    if (!EggRegex.containsMatchIn(ingredient.name)) return "${grams}g"

    val eggCount = (ingredient.amountGrams / GramsPerEgg).roundToInt().coerceAtLeast(1)
    val eggLabel = if (eggCount == 1) "egg" else "eggs"
    return "${grams}g (~$eggCount $eggLabel)"
}

@Composable
private fun RecipeMacroOverview(
    protein: Int,
    carbs: Int,
    fat: Int,
) {
    val total = max(1, protein + carbs + fat).toFloat()
    val carbsSweep = (carbs / total) * 360f
    val proteinSweep = (protein / total) * 360f
    val fatSweep = (fat / total) * 360f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MacroGramRow(label = "Carbs", grams = carbs, color = CarbsMacroColor)
            MacroGramRow(label = "Protein", grams = protein, color = ProteinMacroColor)
            MacroGramRow(label = "Fat", grams = fat, color = FatMacroColor)
        }

        Box(modifier = Modifier.padding(end = 8.dp, bottom = 6.dp)) {
            Canvas(modifier = Modifier.size(124.dp)) {
                drawMacroRingArc(
                    color = FatMacroColor,
                    startAngle = -45f,
                    sweep = if (fat > 0) max(12f, fatSweep) else 0f,
                    scale = 1f,
                    stroke = 12.dp.toPx(),
                )
                drawMacroRingArc(
                    color = ProteinMacroColor,
                    startAngle = 20f,
                    sweep = if (protein > 0) max(12f, proteinSweep) else 0f,
                    scale = 0.74f,
                    stroke = 12.dp.toPx(),
                )
                drawMacroRingArc(
                    color = CarbsMacroColor,
                    startAngle = 45f,
                    sweep = if (carbs > 0) max(12f, carbsSweep) else 0f,
                    scale = 0.48f,
                    stroke = 12.dp.toPx(),
                )
            }
        }
    }
}

@Composable
private fun MacroGramRow(
    label: String,
    grams: Int,
    color: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(color),
        )
        Text(
            text = "$label ${grams}g",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun DrawScope.drawMacroRingArc(
    color: Color,
    startAngle: Float,
    sweep: Float,
    scale: Float,
    stroke: Float,
) {
    if (sweep <= 0f) return
    val ringSize = size.minDimension * scale
    val topLeft = Offset(
        x = (size.width - ringSize) / 2f,
        y = (size.height - ringSize) / 2f,
    )
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = topLeft,
        size = Size(ringSize, ringSize),
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
}

@Composable
private fun WeekCalorieBar(
    calories: Int,
    weekAverageCalories: Int,
) {
    val target = max(1, weekAverageCalories)
    val lowThreshold = (target * 0.9f).roundToInt()
    val highThreshold = (target * 1.1f).roundToInt()
    val maxDisplay = max(highThreshold + (target * 0.4f).roundToInt(), calories + (target * 0.1f).roundToInt())
    val clampedCalories = calories.coerceIn(0, maxDisplay)
    val markerFraction = clampedCalories.toFloat() / maxDisplay.toFloat()

    val lowWeight = lowThreshold.toFloat().coerceAtLeast(1f)
    val targetWeight = (highThreshold - lowThreshold).toFloat().coerceAtLeast(1f)
    val highWeight = (maxDisplay - highThreshold).toFloat().coerceAtLeast(1f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val markerOffset = (maxWidth * markerFraction).coerceAtLeast(0.dp)

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(MaterialTheme.shapes.extraLarge),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(lowWeight)
                            .fillMaxSize()
                            .background(Color(0xFF86EFAC)),
                    )
                    Box(
                        modifier = Modifier
                            .weight(targetWeight)
                            .fillMaxSize()
                            .background(Color(0xFFFDE68A)),
                    )
                    Box(
                        modifier = Modifier
                            .weight(highWeight)
                            .fillMaxSize()
                            .background(Color(0xFFFCA5A5)),
                    )
                }

                Box(
                    modifier = Modifier
                        .offset(x = markerOffset - 1.dp, y = (-2).dp)
                        .width(2.dp)
                        .height(18.dp)
                        .background(Color(0xFF0F172A)),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Low <$lowThreshold", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Week Avg $target", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("High >$highThreshold", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
