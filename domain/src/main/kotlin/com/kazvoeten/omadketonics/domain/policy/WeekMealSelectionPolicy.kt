package com.kazvoeten.omadketonics.domain.policy

import com.kazvoeten.omadketonics.model.Recipe

interface WeekMealSelectionPolicy {
    fun selectMealIds(
        recipes: List<Recipe>,
        ratings: Map<String, Int>,
        weekSize: Int = 7,
    ): List<String>
}
