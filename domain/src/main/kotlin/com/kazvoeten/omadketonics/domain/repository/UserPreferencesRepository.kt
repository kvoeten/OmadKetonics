package com.kazvoeten.omadketonics.domain.repository

import com.kazvoeten.omadketonics.model.ChartType
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val selectedWeekOffset: Flow<Int>
    val chartType: Flow<ChartType>

    suspend fun setSelectedWeekOffset(offset: Int)
    suspend fun setChartType(chartType: ChartType)
}
