package com.kazvoeten.omadketonics.feature.rankings

data class RankingItemUi(
    val recipeId: String,
    val name: String,
    val rating: Int,
)

data class RankingsUiState(
    val isLoading: Boolean = true,
    val items: List<RankingItemUi> = emptyList(),
)

sealed interface RankingsUiEvent {
    data class SetRating(val recipeId: String, val rating: Int) : RankingsUiEvent
}
