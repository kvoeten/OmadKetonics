package com.kazvoeten.omadketonics.feature.recipes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazvoeten.omadketonics.model.Ingredient
import com.kazvoeten.omadketonics.model.OpenFoodFactsProduct
import com.kazvoeten.omadketonics.model.Recipe
import com.kazvoeten.omadketonics.ui.components.RecipeDetailsDialog
import com.kazvoeten.omadketonics.ui.components.RecipeImage
import kotlin.math.roundToInt

private val RecipeIconOptions = listOf("\uD83C\uDF7D\uFE0F", "\uD83E\uDD57", "\uD83E\uDD69", "\uD83C\uDF56", "\uD83C\uDF2F", "\uD83C\uDF72", "\uD83C\uDF73", "\uD83D\uDC1F", "\uD83E\uDD58", "\uD83C\uDF57", "\uD83E\uDD63", "\uD83E\uDD6A")

@Composable
fun RecipesRoute(
    viewModel: RecipesViewModel = hiltViewModel(),
    openRecipeId: String? = null,
    openEditorRecipeId: String? = null,
    onOpenRecipeConsumed: () -> Unit = {},
    onOpenEditorConsumed: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(openRecipeId) {
        if (!openRecipeId.isNullOrBlank()) {
            viewModel.onEvent(RecipesUiEvent.OpenRecipe(openRecipeId))
            onOpenRecipeConsumed()
        }
    }

    LaunchedEffect(openEditorRecipeId) {
        if (!openEditorRecipeId.isNullOrBlank()) {
            viewModel.onEvent(RecipesUiEvent.CloseRecipe)
            viewModel.onEvent(RecipesUiEvent.StartEdit(openEditorRecipeId))
            onOpenEditorConsumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RecipesEffect.Message -> Toast.makeText(context, effect.value, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val selectedRecipe = state.recipes.firstOrNull { it.id == state.selectedRecipeId }
    val editorSeed = viewModel.editingRecipe()

    if (selectedRecipe != null) {
        RecipeDetailsDialog(
            title = selectedRecipe.name,
            calories = selectedRecipe.calories,
            protein = selectedRecipe.protein,
            carbs = selectedRecipe.carbs,
            fat = selectedRecipe.fat,
            ingredients = selectedRecipe.ingredients,
            instructions = selectedRecipe.instructions,
            recipeImageUri = selectedRecipe.imageUri,
            recipeIcon = selectedRecipe.icon,
            weekAverageCalories = state.weekAverageCalories,
            inCurrentPlan = state.inPlanRecipeIds.contains(selectedRecipe.id),
            canAddToWeek = state.isViewingCurrentWeek,
            onDismiss = { viewModel.onEvent(RecipesUiEvent.CloseRecipe) },
            onAddToWeek = if (state.isViewingCurrentWeek && !state.inPlanRecipeIds.contains(selectedRecipe.id)) {
                { viewModel.onEvent(RecipesUiEvent.AddToCurrentWeek(selectedRecipe.id)) }
            } else {
                null
            },
            onEdit = {
                viewModel.onEvent(RecipesUiEvent.CloseRecipe)
                viewModel.onEvent(RecipesUiEvent.StartEdit(selectedRecipe.id))
            },
        )
    }

    if (editorSeed != null) {
        RecipeEditorDialog(
            seed = editorSeed,
            searchState = searchState,
            onDismiss = { viewModel.onEvent(RecipesUiEvent.CloseEditor) },
            onSearch = { query -> viewModel.onEvent(RecipesUiEvent.SearchIngredient(query)) },
            inferCategory = viewModel::inferCategory,
            cycleCategory = viewModel::cycleCategory,
            onSave = { existingId, name, icon, ingredients, instructions ->
                viewModel.onEvent(
                    RecipesUiEvent.SaveRecipe(
                        existingId = existingId,
                        name = name,
                        icon = icon,
                        ingredients = ingredients,
                        instructions = instructions,
                    ),
                )
            },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Recipe Book", fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text("${state.recipes.size} recipes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { viewModel.onEvent(RecipesUiEvent.StartCreate) },
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Create recipe",
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }

        if (state.recipes.isEmpty()) {
            item {
                Text("No recipes yet")
            }
        }

        items(state.recipes, key = { it.id }) { recipe ->
            Card(
                onClick = { viewModel.onEvent(RecipesUiEvent.OpenRecipe(recipe.id)) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    RecipeImage(
                        imageUri = recipe.imageUri,
                        fallbackIcon = recipe.icon,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(156.dp),
                        iconFontSize = 54.sp,
                    )

                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    recipe.name,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${recipe.calories} kcal | ${recipe.protein}g protein | ${recipe.fat}g fat",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                                Text(
                                    text = recipe.mainIngredientsPreview(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 3.dp),
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                if (state.inPlanRecipeIds.contains(recipe.id)) {
                                    Text(
                                        text = "IN PLAN",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                    )
                                }
                                TextButton(
                                    onClick = { viewModel.onEvent(RecipesUiEvent.OpenRecipe(recipe.id)) },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 8.dp,
                                        vertical = 2.dp,
                                    ),
                                ) {
                                    Text(
                                        text = "View",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Recipe.mainIngredientsPreview(limit: Int = 3): String {
    val names = ingredients
        .map { it.name.trim() }
        .filter { it.isNotBlank() }
    val top = names.take(limit)
    if (top.isEmpty()) return "Main ingredients: none listed"
    val extra = (names.size - top.size).coerceAtLeast(0)
    val suffix = if (extra > 0) " +$extra more" else ""
    return "Main ingredients: ${top.joinToString(" | ")}$suffix"
}

@Composable
private fun RecipeEditorDialog(
    seed: RecipeEditorSeed,
    searchState: RecipeEditorSearchState,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    inferCategory: (String) -> com.kazvoeten.omadketonics.model.IngredientCategory,
    cycleCategory: (com.kazvoeten.omadketonics.model.IngredientCategory) -> com.kazvoeten.omadketonics.model.IngredientCategory,
    onSave: (existingId: String?, name: String, icon: String, ingredients: List<Ingredient>, instructions: String) -> Unit,
) {
    var name by rememberSaveable(seed.existingId) { mutableStateOf(seed.name) }
    var icon by rememberSaveable(seed.existingId) { mutableStateOf(seed.icon) }
    var searchText by rememberSaveable(seed.existingId) { mutableStateOf("") }
    var instructions by rememberSaveable(seed.existingId) { mutableStateOf(seed.instructions) }
    val draftIngredients = remember(seed.existingId) {
        mutableStateListOf<Ingredient>().apply { addAll(seed.ingredients) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (seed.existingId == null) "New Recipe" else "Edit Recipe") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Recipe name") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Meal Icon", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(RecipeIconOptions) { candidate ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (icon == candidate) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = CardDefaults.outlinedCardBorder(),
                                    modifier = Modifier.clickable { icon = candidate },
                                ) {
                                    Text(
                                        text = candidate,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Search OpenFoodFacts") },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onSearch(searchText) }, enabled = searchText.isNotBlank() && !searchState.loading) {
                            Text("Search")
                        }
                    }
                }

                if (searchState.loading) {
                    item { Text("Searching...") }
                }
                if (!searchState.error.isNullOrBlank()) {
                    item { Text(searchState.error, color = MaterialTheme.colorScheme.error) }
                }

                items(searchState.results.take(8), key = { it.code }) { result ->
                    SearchResultRow(
                        result = result,
                        onAdd = {
                            val duplicate = draftIngredients.any { it.apiProductCode == result.code }
                            if (!duplicate) {
                                draftIngredients.add(
                                    Ingredient(
                                        name = result.name,
                                        category = inferCategory(result.name),
                                        amountGrams = 100f,
                                        apiProductCode = result.code,
                                        apiProductName = result.name,
                                        apiBrand = result.brand,
                                        apiNutriScore = result.nutriScoreGrade,
                                        nutrientsPer100g = result.nutritionPer100g,
                                    ),
                                )
                            }
                        },
                    )
                }

                item {
                    Text("Ingredients", fontWeight = FontWeight.Bold)
                }
                items(draftIngredients, key = { "${it.apiProductCode}-${it.name}-${it.amountGrams}" }) { ingredient ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = CardDefaults.outlinedCardBorder(),
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ingredient.apiProductName ?: ingredient.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    ingredient.apiBrand?.takeIf { it.isNotBlank() }?.let { brand ->
                                        Text(brand, fontSize = 11.sp)
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable {
                                        draftIngredients.remove(ingredient)
                                    },
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.padding(4.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = {
                                    val index = draftIngredients.indexOf(ingredient)
                                    if (index >= 0) draftIngredients[index] = ingredient.copy(category = cycleCategory(ingredient.category))
                                }) {
                                    Text(ingredient.category.name)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = {
                                        val index = draftIngredients.indexOf(ingredient)
                                        if (index >= 0) {
                                            draftIngredients[index] = ingredient.copy(amountGrams = (ingredient.amountGrams - 10f).coerceAtLeast(1f))
                                        }
                                    }) { Text("-") }
                                    Text("${ingredient.amountGrams.roundToInt()} g")
                                    TextButton(onClick = {
                                        val index = draftIngredients.indexOf(ingredient)
                                        if (index >= 0) {
                                            draftIngredients[index] = ingredient.copy(amountGrams = ingredient.amountGrams + 10f)
                                        }
                                    }) { Text("+") }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        label = { Text("Prep notes (one step per line)") },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(seed.existingId, name, icon, draftIngredients.toList(), instructions) }) {
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

@Composable
private fun SearchResultRow(result: OpenFoodFactsProduct, onAdd: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAdd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(result.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                result.brand?.takeIf { it.isNotBlank() }?.let { brand ->
                    Text(brand, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("${result.nutritionPer100g.calories.roundToInt()} kcal /100g", fontSize = 10.sp)
            }
            Text("ADD", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}


