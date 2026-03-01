package com.kazvoeten.omadketonics.feature.plan

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.Fastfood
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.MacroAverages
import com.kazvoeten.omadketonics.ui.components.RecipeDetailsDialog
import com.kazvoeten.omadketonics.ui.components.StarRow
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val BgColor = Color(0xFF0F172A)
private val CardBg = Color(0xFF1E293B)
private val CardBorder = Color(0xFF334155)
private val Primary = Color(0xFF2DD4BF)
private val TextMain = Color(0xFFF8FAFC)
private val TextMuted = Color(0xFF94A3B8)
private val CarbsColor = Color(0xFF7CE0BB)
private val ProteinColor = Color(0xFFFFE285)
private val FatColor = Color(0xFFFF91A4)
private val SuccessColor = Color(0xFF22C55E)

@Composable
fun PlanRoute(
    viewModel: PlanViewModel = hiltViewModel(),
    onEditRecipe: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showCheatDialog by remember { mutableStateOf(false) }
    var selectedMealId by remember { mutableStateOf<String?>(null) }
    var pendingMealReview by remember { mutableStateOf<PlanMealItemUi?>(null) }
    var pendingMealPhotoUri by remember { mutableStateOf<String?>(null) }
    var showPhotoPrompt by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    val selectedMeal = state.meals.firstOrNull { it.recipeId == selectedMealId }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        val recipeId = pendingMealReview?.recipeId
        pendingMealPhotoUri = if (bitmap != null && recipeId != null) {
            saveMealPhoto(context, recipeId, bitmap)
        } else {
            null
        }
        showRatingDialog = pendingMealReview != null
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        val recipeId = pendingMealReview?.recipeId
        pendingMealPhotoUri = if (uri != null && recipeId != null) {
            saveMealPhotoFromUri(context, recipeId, uri)
        } else {
            null
        }
        showRatingDialog = pendingMealReview != null
    }

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

    if (selectedMeal != null) {
        RecipeDetailsDialog(
            title = selectedMeal.name,
            calories = selectedMeal.calories,
            protein = selectedMeal.protein,
            carbs = selectedMeal.carbs,
            fat = selectedMeal.fat,
            ingredients = selectedMeal.ingredients,
            instructions = selectedMeal.instructions,
            recipeImageUri = selectedMeal.recipeImageUri,
            recipeIcon = selectedMeal.recipeIcon,
            weekAverageCalories = state.averages.calories,
            inCurrentPlan = true,
            canAddToWeek = false,
            onAddToWeek = null,
            onEdit = {
                selectedMealId = null
                onEditRecipe(selectedMeal.recipeId)
            },
            onDismiss = { selectedMealId = null },
        )
    }

    if (showPhotoPrompt && pendingMealReview != null) {
        PhotoPromptDialog(
            mealName = pendingMealReview?.name.orEmpty(),
            onTakePhoto = {
                showPhotoPrompt = false
                cameraLauncher.launch(null)
            },
            onPickGallery = {
                showPhotoPrompt = false
                galleryLauncher.launch("image/*")
            },
            onSkip = {
                showPhotoPrompt = false
                showRatingDialog = true
            },
            onCancel = {
                showPhotoPrompt = false
                pendingMealPhotoUri = null
                pendingMealReview = null
            },
        )
    }

    if (showRatingDialog && pendingMealReview != null) {
        MealRatingDialog(
            mealName = pendingMealReview?.name.orEmpty(),
            initialRating = pendingMealReview?.rating?.coerceIn(1, 5) ?: 3,
            onConfirm = { rating ->
                val meal = pendingMealReview ?: return@MealRatingDialog
                viewModel.onEvent(
                    PlanUiEvent.SetMealEaten(
                        recipeId = meal.recipeId,
                        eaten = true,
                        capturedImageUri = pendingMealPhotoUri,
                        rating = rating,
                    ),
                )
                showRatingDialog = false
                pendingMealPhotoUri = null
                pendingMealReview = null
            },
            onCancel = {
                showRatingDialog = false
                pendingMealPhotoUri = null
                pendingMealReview = null
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
                        text = "THIS WEEK'S OMAD",
                        color = TextMain,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
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

            itemsIndexed(state.meals, key = { _, meal -> meal.recipeId }) { _, meal ->
                MealCard(
                    meal = meal,
                    enabled = state.isViewingCurrentWeek,
                    onOpenRecipe = { selectedMealId = meal.recipeId },
                    onToggleEaten = {
                        if (meal.isEaten) {
                            viewModel.onEvent(
                                PlanUiEvent.SetMealEaten(
                                    recipeId = meal.recipeId,
                                    eaten = false,
                                ),
                            )
                        } else {
                            pendingMealReview = meal
                            pendingMealPhotoUri = null
                            showPhotoPrompt = true
                        }
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
                text = "CURRENT ENERGY",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = selectedMood?.let { "Energy: ${it.label}" } ?: "How are you?",
                    color = Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (selectedMood != null) {
                    Text(
                        text = "Clear",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onClearMood),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DailyMood.entries.forEach { mood ->
                val isActive = selectedMood == mood
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = 2.dp,
                            color = if (isActive) Primary else Color.Transparent,
                            shape = RoundedCornerShape(18.dp),
                        )
                        .background(if (isActive) Primary.copy(alpha = 0.12f) else Color.Transparent)
                        .clickable { onSelectMood(mood) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .alpha(if (isActive) 1f else 0.5f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = moodEmoji(mood),
                        fontSize = 30.sp,
                    )
                }
            }
        }
    }
}

private fun moodEmoji(mood: DailyMood): String = when (mood) {
    DailyMood.Terrible -> "\uD83D\uDE2B"
    DailyMood.Low -> "\uD83D\uDE15"
    DailyMood.Okay -> "\uD83D\uDE10"
    DailyMood.Good -> "\uD83D\uDE0A"
    DailyMood.Great -> "\uD83E\uDD29"
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
    meal: PlanMealItemUi,
    enabled: Boolean,
    onOpenRecipe: () -> Unit,
    onToggleEaten: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (meal.isEaten) CardBg.copy(alpha = 0.6f) else CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onOpenRecipe)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = meal.recipeIcon,
                    fontSize = 24.sp,
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
                    text = "${meal.calories} kcal | ${meal.protein}g Protein | ${meal.fat}g Fat",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                MealMacroBar(
                    protein = meal.protein,
                    carbs = meal.carbs,
                    fat = meal.fat,
                    modifier = Modifier.padding(top = 10.dp, end = 4.dp),
                )
            }

            Surface(
                color = if (meal.isEaten) SuccessColor else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = if (meal.isEaten) SuccessColor else CardBorder,
                ),
                modifier = Modifier
                    .size(44.dp)
                    .alpha(if (enabled) 1f else 0.45f)
                    .then(if (enabled) Modifier.clickable { onToggleEaten() } else Modifier),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = if (meal.isEaten) "Completed" else "Mark meal complete",
                        tint = if (meal.isEaten) Color.White else TextMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MealMacroBar(
    protein: Int,
    carbs: Int,
    fat: Int,
    modifier: Modifier = Modifier,
) {
    val total = max(1, protein + carbs + fat).toFloat()
    val carbsWeight = max(0.001f, carbs / total)
    val proteinWeight = max(0.001f, protein / total)
    val fatWeight = max(0.001f, fat / total)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(CardBorder.copy(alpha = 0.85f)),
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

@Composable
private fun PhotoPromptDialog(
    mealName: String,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Log Meal") },
        text = {
            Text(
                text = "Take a meal photo for \"$mealName\"? You can skip and keep the current recipe image.",
            )
        },
        confirmButton = {
            TextButton(onClick = onTakePhoto) {
                Text("Take Photo")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onPickGallery) {
                    Text("Gallery")
                }
                TextButton(onClick = onSkip) {
                    Text("Skip")
                }
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        },
    )
}

@Composable
private fun MealRatingDialog(
    mealName: String,
    initialRating: Int,
    onConfirm: (Int) -> Unit,
    onCancel: () -> Unit,
) {
    var rating by remember(initialRating) { mutableIntStateOf(initialRating.coerceIn(1, 5)) }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Rate \"$mealName\"") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("This updates recipe rankings for future plans.")
                StarRow(
                    rating = rating,
                    size = 30.dp,
                    onRate = { selected -> rating = selected },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(rating) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
    )
}

private fun saveMealPhoto(
    context: Context,
    recipeId: String,
    bitmap: Bitmap,
): String? {
    val safeId = recipeId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    val imageDir = File(context.filesDir, "recipe_images").apply { mkdirs() }
    val outFile = File(imageDir, "${safeId}.jpg")

    return runCatching {
        FileOutputStream(outFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.flush()
        }
        outFile.absolutePath
    }.getOrNull()
}

private fun saveMealPhotoFromUri(
    context: Context,
    recipeId: String,
    uri: Uri,
): String? {
    val safeId = recipeId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    val imageDir = File(context.filesDir, "recipe_images").apply { mkdirs() }
    val outFile = File(imageDir, "${safeId}.jpg")

    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
                output.flush()
            }
        } ?: return null
        outFile.absolutePath
    }.getOrNull()
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


