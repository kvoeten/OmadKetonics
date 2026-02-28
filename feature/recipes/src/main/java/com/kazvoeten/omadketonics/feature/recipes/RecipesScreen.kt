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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
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
import com.kazvoeten.omadketonics.ui.components.MacroBar
import com.kazvoeten.omadketonics.ui.components.MacroVisualSize
import kotlin.math.roundToInt

@Composable
fun RecipesRoute(
    viewModel: RecipesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RecipesEffect.Message -> Toast.makeText(context, effect.value, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val selectedRecipe = viewModel.selectedRecipe()
    val editorSeed = viewModel.editingRecipe()

    if (selectedRecipe != null) {
        RecipeDetailsDialog(
            recipe = selectedRecipe,
            inCurrentPlan = state.inPlanRecipeIds.contains(selectedRecipe.id),
            isViewingCurrentWeek = state.isViewingCurrentWeek,
            onDismiss = { viewModel.onEvent(RecipesUiEvent.CloseRecipe) },
            onAddToWeek = { viewModel.onEvent(RecipesUiEvent.AddToCurrentWeek(selectedRecipe.id)) },
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
            onSave = { existingId, name, ingredients, instructions ->
                viewModel.onEvent(
                    RecipesUiEvent.SaveRecipe(
                        existingId = existingId,
                        name = name,
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.clickable { viewModel.onEvent(RecipesUiEvent.OpenRecipe(recipe.id)) },
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            recipe.name,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (state.inPlanRecipeIds.contains(recipe.id)) {
                            Text("IN PLAN", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    MacroBar(
                        protein = recipe.protein,
                        carbs = recipe.carbs,
                        fat = recipe.fat,
                        size = MacroVisualSize.Small,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeDetailsDialog(
    recipe: Recipe,
    inCurrentPlan: Boolean,
    isViewingCurrentWeek: Boolean,
    onDismiss: () -> Unit,
    onAddToWeek: () -> Unit,
    onEdit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(recipe.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${recipe.calories} kcal", fontWeight = FontWeight.Bold)
                MacroBar(
                    protein = recipe.protein,
                    carbs = recipe.carbs,
                    fat = recipe.fat,
                    showLabels = true,
                    size = MacroVisualSize.Medium,
                )
                Text("Ingredients", fontWeight = FontWeight.Bold)
                recipe.ingredients.forEach { ingredient ->
                    Text("- ${ingredient.name} (${ingredient.amountGrams.roundToInt()}g)")
                }
                Text("Instructions", fontWeight = FontWeight.Bold)
                recipe.instructions.forEachIndexed { index, step ->
                    Text("${index + 1}. $step")
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isViewingCurrentWeek && !inCurrentPlan) {
                    TextButton(onClick = onAddToWeek) {
                        Text("Add To Week")
                    }
                }
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
    )
}

@Composable
private fun RecipeEditorDialog(
    seed: RecipeEditorSeed,
    searchState: RecipeEditorSearchState,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    inferCategory: (String) -> com.kazvoeten.omadketonics.model.IngredientCategory,
    cycleCategory: (com.kazvoeten.omadketonics.model.IngredientCategory) -> com.kazvoeten.omadketonics.model.IngredientCategory,
    onSave: (existingId: String?, name: String, ingredients: List<Ingredient>, instructions: String) -> Unit,
) {
    var name by rememberSaveable(seed.existingId) { mutableStateOf(seed.name) }
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
            TextButton(onClick = { onSave(seed.existingId, name, draftIngredients.toList(), instructions) }) {
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
