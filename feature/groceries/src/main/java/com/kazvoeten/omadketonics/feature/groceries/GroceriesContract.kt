package com.kazvoeten.omadketonics.feature.groceries

import com.kazvoeten.omadketonics.model.IngredientCategory

data class GroceryItemUi(
    val key: String,
    val name: String,
    val quantityLabel: String,
    val checked: Boolean,
    val category: IngredientCategory,
    val isCustom: Boolean,
)

enum class GrocerySectionType {
    Produce,
    ProteinDairy,
    Pantry,
}

data class GrocerySectionUi(
    val type: GrocerySectionType,
    val title: String,
    val items: List<GroceryItemUi>,
)

data class GrocerySearchResultUi(
    val name: String,
    val brand: String?,
    val caloriesPer100g: Int,
)

data class GroceriesUiState(
    val isLoading: Boolean = true,
    val isViewingCurrentWeek: Boolean = true,
    val sections: List<GrocerySectionUi> = emptyList(),
    val query: String = "",
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val searchResults: List<GrocerySearchResultUi> = emptyList(),
)

sealed interface GroceriesUiEvent {
    data class ToggleItem(val itemName: String, val currentlyChecked: Boolean) : GroceriesUiEvent
    data class QueryChanged(val value: String) : GroceriesUiEvent
    data object AddManualItem : GroceriesUiEvent
    data class AddItemFromSearch(val name: String) : GroceriesUiEvent
    data object ClearCompleted : GroceriesUiEvent
}

sealed interface GroceriesEffect {
    data class Message(val value: String) : GroceriesEffect
}
