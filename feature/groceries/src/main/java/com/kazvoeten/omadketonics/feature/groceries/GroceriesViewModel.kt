package com.kazvoeten.omadketonics.feature.groceries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazvoeten.omadketonics.core.common.AppResult
import com.kazvoeten.omadketonics.domain.model.DisplayedWeek
import com.kazvoeten.omadketonics.domain.policy.IngredientCategoryInferencePolicy
import com.kazvoeten.omadketonics.domain.repository.GroceryRepository
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.domain.usecase.GetDisplayedWeekUseCase
import com.kazvoeten.omadketonics.domain.usecase.SearchIngredientsUseCase
import com.kazvoeten.omadketonics.domain.usecase.ToggleGroceryItemUseCase
import com.kazvoeten.omadketonics.model.Ingredient
import com.kazvoeten.omadketonics.model.IngredientCategory
import com.kazvoeten.omadketonics.model.OpenFoodFactsProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@HiltViewModel
class GroceriesViewModel @Inject constructor(
    private val getDisplayedWeekUseCase: GetDisplayedWeekUseCase,
    private val recipeRepository: RecipeRepository,
    private val groceryRepository: GroceryRepository,
    private val toggleGroceryItemUseCase: ToggleGroceryItemUseCase,
    private val searchIngredientsUseCase: SearchIngredientsUseCase,
    private val ingredientCategoryInferencePolicy: IngredientCategoryInferencePolicy,
) : ViewModel() {
    private data class SearchState(
        val loading: Boolean = false,
        val error: String? = null,
        val results: List<OpenFoodFactsProduct> = emptyList(),
    )

    private data class GroceryAccumulator(
        val key: String,
        val category: IngredientCategory,
        var displayName: String,
        var totalGrams: Float,
        var count: Int,
    )

    private var latestDisplayedWeek: DisplayedWeek? = null
    private var searchJob: Job? = null

    private val query = MutableStateFlow("")
    private val searchState = MutableStateFlow(SearchState())

    private val effectEmitter = MutableSharedFlow<GroceriesEffect>(extraBufferCapacity = 8)
    val effects = effectEmitter

    val state = getDisplayedWeekUseCase().filterNotNull().flatMapLatest { displayedWeek ->
        latestDisplayedWeek = displayedWeek
        combine(
            recipeRepository.observeRecipes(),
            groceryRepository.observeChecklist(displayedWeek.snapshot.startDate),
            query,
            searchState,
        ) { recipes, checklist, queryValue, search ->
            val recipeById = recipes.associateBy { it.id }
            val displayedRecipes = displayedWeek.snapshot.mealIds.mapNotNull { recipeById[it] }

            val plannedMap = LinkedHashMap<String, GroceryAccumulator>()
            displayedRecipes.flatMap { it.ingredients }.forEach { ingredient ->
                val key = ingredient.name.trim()
                if (key.isBlank()) return@forEach
                val entry = plannedMap.getOrPut(key) {
                    GroceryAccumulator(
                        key = key,
                        category = ingredient.category,
                        displayName = displayNameFromIngredient(ingredient),
                        totalGrams = 0f,
                        count = 0,
                    )
                }
                entry.totalGrams += inferredAmountGrams(ingredient)
                entry.count += 1
            }

            val plannedItems = plannedMap.values.map { entry ->
                GroceryItemUi(
                    key = entry.key,
                    name = entry.displayName,
                    quantityLabel = formatQuantity(entry.totalGrams, entry.count),
                    checked = if (displayedWeek.isViewingCurrentWeek) checklist[entry.key] ?: false else false,
                    category = entry.category,
                    isCustom = false,
                )
            }

            val customItems = checklist.keys
                .filter { itemName -> itemName !in plannedMap.keys }
                .map { itemName ->
                    GroceryItemUi(
                        key = itemName,
                        name = itemName,
                        quantityLabel = "1 unit",
                        checked = if (displayedWeek.isViewingCurrentWeek) checklist[itemName] ?: false else false,
                        category = ingredientCategoryInferencePolicy.inferCategory(itemName),
                        isCustom = true,
                    )
                }

            val allItems = plannedItems + customItems
            val sectionOrder = listOf(
                GrocerySectionType.Produce,
                GrocerySectionType.ProteinDairy,
                GrocerySectionType.Pantry,
            )
            val sections = sectionOrder.mapNotNull { section ->
                val items = allItems.filter { sectionForCategory(it.category) == section }
                if (items.isEmpty()) {
                    null
                } else {
                    GrocerySectionUi(
                        type = section,
                        title = sectionTitle(section),
                        items = items,
                    )
                }
            }

            GroceriesUiState(
                isLoading = false,
                isViewingCurrentWeek = displayedWeek.isViewingCurrentWeek,
                sections = sections,
                query = queryValue,
                isSearching = search.loading,
                searchError = search.error,
                searchResults = search.results.take(6).map { result ->
                    GrocerySearchResultUi(
                        name = result.name,
                        brand = result.brand,
                        caloriesPer100g = result.nutritionPer100g.calories.roundToInt(),
                    )
                },
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroceriesUiState(),
    )

    fun onEvent(event: GroceriesUiEvent) {
        when (event) {
            is GroceriesUiEvent.ToggleItem -> toggleItem(event.itemName, event.currentlyChecked)
            is GroceriesUiEvent.QueryChanged -> {
                query.value = event.value
                searchIngredients(event.value)
            }

            GroceriesUiEvent.AddManualItem -> addCustomItem(state.value.query)
            is GroceriesUiEvent.AddItemFromSearch -> addCustomItem(event.name)
            GroceriesUiEvent.ClearCompleted -> clearCompleted()
        }
    }

    private fun toggleItem(itemName: String, currentlyChecked: Boolean) {
        viewModelScope.launch {
            val uiState = state.value
            if (!uiState.isViewingCurrentWeek) return@launch
            val displayedWeek = latestDisplayedWeek ?: return@launch
            toggleGroceryItemUseCase(
                weekStartDate = displayedWeek.snapshot.startDate,
                itemName = itemName,
                isChecked = currentlyChecked,
            )
        }
    }

    private fun clearCompleted() {
        viewModelScope.launch {
            if (!state.value.isViewingCurrentWeek) {
                effectEmitter.emit(GroceriesEffect.Message("Historical week is read-only"))
                return@launch
            }
            val displayedWeek = latestDisplayedWeek ?: return@launch
            val checkedItems = state.value.sections
                .flatMap { it.items }
                .filter { it.checked }
                .map { it.key }
                .distinct()
            if (checkedItems.isEmpty()) {
                effectEmitter.emit(GroceriesEffect.Message("Nothing to clear"))
                return@launch
            }
            groceryRepository.removeItems(displayedWeek.snapshot.startDate, checkedItems)
            effectEmitter.emit(GroceriesEffect.Message("Cleared ${checkedItems.size} items"))
        }
    }

    private fun addCustomItem(rawName: String) {
        viewModelScope.launch {
            val normalizedName = normalizeItemName(rawName)
            if (normalizedName.isBlank()) {
                effectEmitter.emit(GroceriesEffect.Message("Enter an item name"))
                return@launch
            }
            val displayedWeek = latestDisplayedWeek ?: return@launch
            if (!state.value.isViewingCurrentWeek) {
                effectEmitter.emit(GroceriesEffect.Message("Historical week is read-only"))
                return@launch
            }

            val alreadyListed = state.value.sections.any { section ->
                section.items.any { it.key.equals(normalizedName, ignoreCase = true) }
            }
            if (alreadyListed) {
                query.value = ""
                searchState.value = SearchState()
                effectEmitter.emit(GroceriesEffect.Message("Item already in list"))
                return@launch
            }

            groceryRepository.setChecked(
                weekStartDate = displayedWeek.snapshot.startDate,
                itemName = normalizedName,
                checked = false,
            )
            query.value = ""
            searchState.value = SearchState()
            effectEmitter.emit(GroceriesEffect.Message("Added \"$normalizedName\""))
        }
    }

    private fun searchIngredients(rawQuery: String) {
        val trimmed = rawQuery.trim()
        if (trimmed.length < 2) {
            searchJob?.cancel()
            searchState.value = SearchState()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            searchState.value = SearchState(loading = true)
            when (val result = searchIngredientsUseCase(trimmed)) {
                is AppResult.Success -> {
                    searchState.value = SearchState(
                        loading = false,
                        results = result.value,
                    )
                }

                is AppResult.Error -> {
                    searchState.value = SearchState(
                        loading = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    private fun normalizeItemName(name: String): String {
        return name
            .trim()
            .replace(WhitespaceRegex, " ")
    }

    private fun displayNameFromIngredient(ingredient: Ingredient): String {
        val source = ingredient.apiProductName?.takeIf { it.isNotBlank() } ?: ingredient.name
        val match = QuantitySuffixRegex.find(source)
        if (match == null) return source.trim()
        val suffix = match.groupValues.getOrNull(1).orEmpty()
        return if (suffix.contains("g", ignoreCase = true)) {
            source.replace(QuantitySuffixRegex, "").trim()
        } else {
            source.trim()
        }
    }

    private fun inferredAmountGrams(ingredient: Ingredient): Float {
        if (ingredient.amountGrams > 0f) return ingredient.amountGrams
        val gramsInName = GramsRegex.find(ingredient.name)?.groupValues?.getOrNull(1)?.toFloatOrNull()
        return gramsInName ?: 0f
    }

    private fun formatQuantity(totalGrams: Float, count: Int): String {
        if (totalGrams >= 1f) return "${totalGrams.roundToInt()}g"
        return if (count <= 1) "1 unit" else "$count units"
    }

    private fun sectionForCategory(category: IngredientCategory): GrocerySectionType {
        return when (category) {
            IngredientCategory.Produce -> GrocerySectionType.Produce
            IngredientCategory.Meat, IngredientCategory.Dairy -> GrocerySectionType.ProteinDairy
            IngredientCategory.Pantry, IngredientCategory.Freezer -> GrocerySectionType.Pantry
        }
    }

    private fun sectionTitle(type: GrocerySectionType): String {
        return when (type) {
            GrocerySectionType.Produce -> "Produce"
            GrocerySectionType.ProteinDairy -> "Protein & Dairy"
            GrocerySectionType.Pantry -> "Pantry"
        }
    }

    private companion object {
        val QuantitySuffixRegex = Regex("\\(([^)]+)\\)\\s*$")
        val GramsRegex = Regex("(\\d+(?:\\.\\d+)?)\\s*g", RegexOption.IGNORE_CASE)
        val WhitespaceRegex = Regex("\\s+")
    }
}
