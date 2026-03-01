package com.kazvoeten.omadketonics.domain.repository

import com.kazvoeten.omadketonics.model.DailyHealthSummary
import com.kazvoeten.omadketonics.model.HealthConnectionState
import com.kazvoeten.omadketonics.model.ManualActivityInput
import com.kazvoeten.omadketonics.model.ManualActivityLog
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface HealthRepository {
    val connectionState: Flow<HealthConnectionState>

    fun requiredPermissions(): Set<String>
    suspend fun updateGrantedPermissions(grantedPermissions: Set<String>)

    fun observeDailySummaries(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<DailyHealthSummary>>

    fun observeManualActivityLogs(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<ManualActivityLog>>

    suspend fun queueManualActivity(
        input: ManualActivityInput,
        estimatedCalories: Int,
        clientRecordId: String,
        source: String = "app_manual",
    )

    suspend fun queueNutritionUpsert(
        date: LocalDate,
        mealName: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
    )

    suspend fun queueNutritionDelete(date: LocalDate)

    suspend fun syncNow(daysBack: Int = 90)
}
