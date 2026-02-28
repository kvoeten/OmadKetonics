package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.model.MacroAverages
import com.kazvoeten.omadketonics.model.Recipe
import javax.inject.Inject

class GetWeeklyAveragesUseCase @Inject constructor() {
    operator fun invoke(recipes: List<Recipe>): MacroAverages {
        if (recipes.isEmpty()) {
            return MacroAverages(calories = 0, protein = 0, carbs = 0, fat = 0)
        }

        val calories = recipes.sumOf { it.calories } / recipes.size
        val protein = recipes.sumOf { it.protein } / recipes.size
        val carbs = recipes.sumOf { it.carbs } / recipes.size
        val fat = recipes.sumOf { it.fat } / recipes.size
        return MacroAverages(calories = calories, protein = protein, carbs = carbs, fat = fat)
    }
}
