package com.kazvoeten.omadketonics.feature.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazvoeten.omadketonics.domain.usecase.GenerateWeekPlanUseCase
import com.kazvoeten.omadketonics.domain.usecase.GetDisplayedWeekUseCase
import com.kazvoeten.omadketonics.domain.usecase.GetWeeklyAveragesUseCase
import com.kazvoeten.omadketonics.domain.usecase.LogMealUseCase
import com.kazvoeten.omadketonics.domain.usecase.SetMoodUseCase
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.model.WeekSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val getDisplayedWeekUseCase: GetDisplayedWeekUseCase,
    private val recipeRepository: RecipeRepository,
    private val trackingRepository: TrackingRepository,
    private val getWeeklyAveragesUseCase: GetWeeklyAveragesUseCase,
    private val generateWeekPlanUseCase: GenerateWeekPlanUseCase,
    private val logMealUseCase: LogMealUseCase,
    private val setMoodUseCase: SetMoodUseCase,
) : ViewModel() {
    private val effectEmitter = MutableSharedFlow<PlanEffect>(extraBufferCapacity = 8)
    val effects = effectEmitter

    val state = combine(
        getDisplayedWeekUseCase().filterNotNull(),
        recipeRepository.observeRecipes(),
        trackingRepository.observeRatings(),
        trackingRepository.observeMoods(),
    ) { displayedWeek, recipes, ratings, moods ->
        val recipeById = recipes.associateBy { it.id }
        val weekRecipes = displayedWeek.snapshot.mealIds.mapNotNull { recipeById[it] }
        val eatenSet = displayedWeek.snapshot.eatenMealIds.toSet()

        val meals = weekRecipes.map { recipe ->
            PlanMealItemUi(
                recipeId = recipe.id,
                recipeIcon = recipe.icon,
                name = recipe.name,
                calories = recipe.calories,
                protein = recipe.protein,
                carbs = recipe.carbs,
                fat = recipe.fat,
                ingredients = recipe.ingredients,
                instructions = recipe.instructions,
                isEaten = eatenSet.contains(recipe.id),
                rating = ratings[recipe.id] ?: 0,
            )
        }

        val averages = getWeeklyAveragesUseCase(weekRecipes)
        PlanUiState(
            isLoading = false,
            weekTitle = buildWeekTitle(displayedWeek.snapshot),
            weekSubtitle = buildWeekSubtitle(displayedWeek.snapshot, displayedWeek.isViewingCurrentWeek),
            isViewingCurrentWeek = displayedWeek.isViewingCurrentWeek,
            averages = averages,
            todayMood = moods[LocalDate.now()],
            meals = meals,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlanUiState(),
    )

    fun onEvent(event: PlanUiEvent) {
        when (event) {
            is PlanUiEvent.SetMood -> {
                viewModelScope.launch {
                    setMoodUseCase(event.mood)
                }
            }

            is PlanUiEvent.GenerateWeek -> {
                viewModelScope.launch {
                    generateWeekPlanUseCase()
                }
            }

            is PlanUiEvent.SetMealEaten -> {
                viewModelScope.launch {
                    if (!state.value.isViewingCurrentWeek) {
                        effectEmitter.emit(PlanEffect.Message("Historical week is read-only"))
                        return@launch
                    }
                    val recipe = recipeRepository.getRecipe(event.recipeId)
                    if (recipe == null) {
                        effectEmitter.emit(PlanEffect.Message("Recipe not found"))
                        return@launch
                    }
                    logMealUseCase.setRecipeEaten(recipe, eaten = event.eaten)
                    effectEmitter.emit(
                        PlanEffect.Message(
                            if (event.eaten) "Meal completed!" else "Meal marked as pending",
                        ),
                    )
                }
            }

            is PlanUiEvent.LogCheatMeal -> {
                viewModelScope.launch {
                    if (!state.value.isViewingCurrentWeek) {
                        effectEmitter.emit(PlanEffect.Message("Cheat logging is only available for the current week"))
                        return@launch
                    }
                    val error = logMealUseCase.logCheatMeal(
                        name = event.name,
                        calories = event.calories,
                        protein = event.protein,
                        carbs = event.carbs,
                        fat = event.fat,
                    )
                    if (error != null) {
                        effectEmitter.emit(PlanEffect.Message(error))
                    } else {
                        effectEmitter.emit(PlanEffect.Message("Cheat meal logged"))
                    }
                }
            }
        }
    }

    suspend fun isCurrentWeek(): Boolean {
        return getDisplayedWeekUseCase().first()?.isViewingCurrentWeek ?: false
    }

    private fun buildWeekTitle(snapshot: WeekSnapshot): String {
        val fmt = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
        return "Week Of ${snapshot.startDate.format(fmt)}"
    }

    private fun buildWeekSubtitle(snapshot: WeekSnapshot, isCurrentWeek: Boolean): String {
        val eaten = snapshot.eatenMealIds.count { snapshot.mealIds.contains(it) }
        val total = snapshot.mealIds.size
        val prefix = if (isCurrentWeek) "Current" else "Past"
        return "$prefix - $eaten / $total eaten"
    }
}
