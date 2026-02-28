package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.domain.policy.WeekMealSelectionPolicy
import com.kazvoeten.omadketonics.domain.repository.GroceryRepository
import com.kazvoeten.omadketonics.domain.repository.RecipeRepository
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.domain.repository.UserPreferencesRepository
import com.kazvoeten.omadketonics.domain.repository.WeekPlanRepository
import javax.inject.Inject

class GenerateWeekPlanUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val trackingRepository: TrackingRepository,
    private val weekPlanRepository: WeekPlanRepository,
    private val groceryRepository: GroceryRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val dateProvider: DateProvider,
    private val weekMealSelectionPolicy: WeekMealSelectionPolicy,
) {
    suspend operator fun invoke() {
        val recipes = recipeRepository.getRecipes()
        if (recipes.isEmpty()) return

        val ratings = trackingRepository.getRatings()
        val selected = weekMealSelectionPolicy.selectMealIds(recipes, ratings)
        val current = weekPlanRepository.getCurrentWeek()
        val nextStart = if (current == null) {
            dateProvider.sundayStart(dateProvider.today())
        } else {
            current.startDate.plusWeeks(1)
        }

        weekPlanRepository.generateNewCurrentWeek(nextStart, selected)
        groceryRepository.clearWeek(nextStart)
        userPreferencesRepository.setSelectedWeekOffset(0)
    }
}
