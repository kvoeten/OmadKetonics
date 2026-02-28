package com.kazvoeten.omadketonics.data.repository.policy

import com.kazvoeten.omadketonics.domain.policy.WeekMealSelectionPolicy
import com.kazvoeten.omadketonics.model.Recipe
import javax.inject.Inject
import kotlin.random.Random

class DefaultWeekMealSelectionPolicy @Inject constructor() : WeekMealSelectionPolicy {
    override fun selectMealIds(
        recipes: List<Recipe>,
        ratings: Map<String, Int>,
        weekSize: Int,
    ): List<String> {
        if (recipes.isEmpty()) return emptyList()
        val sorted = recipes.sortedByDescending { ratings[it.id] ?: 0 }
        val top = sorted.take(3)
        val fillCount = (weekSize - top.size).coerceAtLeast(0)
        val randomFill = sorted.drop(top.size).shuffled(Random(System.currentTimeMillis())).take(fillCount)
        return (top + randomFill).map { it.id }
    }
}
