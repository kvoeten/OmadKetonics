package com.kazvoeten.omadketonics.domain.repository

import com.kazvoeten.omadketonics.model.DailyMood
import com.kazvoeten.omadketonics.model.MealHistoryEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TrackingRepository {
    fun observeMealHistory(): Flow<List<MealHistoryEntry>>
    fun observeWeights(): Flow<Map<LocalDate, Float>>
    fun observeMoods(): Flow<Map<LocalDate, DailyMood>>
    fun observeRatings(): Flow<Map<String, Int>>

    suspend fun upsertMealHistory(entry: MealHistoryEntry)
    suspend fun removeMealHistoryForDate(date: LocalDate)
    suspend fun setWeight(date: LocalDate, value: Float)
    suspend fun setMood(date: LocalDate, mood: DailyMood?)
    suspend fun setRating(recipeId: String, rating: Int)
    suspend fun getRatings(): Map<String, Int>
}
