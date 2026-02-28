package com.kazvoeten.omadketonics.feature.rankings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RankingsViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val trackingRepository: TrackingRepository,
) : ViewModel() {
    val state = combine(
        recipeRepository.observeRecipes(),
        trackingRepository.observeRatings(),
    ) { recipes, ratings ->
        val sorted = recipes.sortedByDescending { ratings[it.id] ?: 0 }
        RankingsUiState(
            isLoading = false,
            items = sorted.map { recipe ->
                RankingItemUi(
                    recipeId = recipe.id,
                    name = recipe.name,
                    rating = ratings[recipe.id] ?: 0,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RankingsUiState(),
    )

    fun onEvent(event: RankingsUiEvent) {
        when (event) {
            is RankingsUiEvent.SetRating -> {
                viewModelScope.launch {
                    trackingRepository.setRating(event.recipeId, event.rating)
                }
            }
        }
    }
}
