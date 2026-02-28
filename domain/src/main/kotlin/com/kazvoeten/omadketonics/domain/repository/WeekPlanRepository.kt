package com.kazvoeten.omadketonics.domain.repository

import com.kazvoeten.omadketonics.model.WeekSnapshot
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface WeekPlanRepository {
    fun observeWeeksDescending(): Flow<List<WeekSnapshot>>
    suspend fun getCurrentWeek(): WeekSnapshot?
    suspend fun ensureCurrentWeekExists(mealIds: List<String>, date: LocalDate)
    suspend fun generateNewCurrentWeek(startDate: LocalDate, mealIds: List<String>)
    suspend fun addMealToCurrentWeek(recipeId: String)
    suspend fun setMealEatenInCurrentWeek(recipeId: String, eaten: Boolean)
}
