package com.kazvoeten.omadketonics.data.repository.impl

import com.kazvoeten.omadketonics.data.local.store.UserPreferencesStore
import com.kazvoeten.omadketonics.domain.repository.UserPreferencesRepository
import com.kazvoeten.omadketonics.model.ChartType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesStore: UserPreferencesStore,
) : UserPreferencesRepository {
    override val selectedWeekOffset: Flow<Int>
        get() = userPreferencesStore.selectedWeekOffset

    override val chartType: Flow<ChartType>
        get() = userPreferencesStore.chartType

    override suspend fun setSelectedWeekOffset(offset: Int) {
        userPreferencesStore.setSelectedWeekOffset(offset)
    }

    override suspend fun setChartType(chartType: ChartType) {
        userPreferencesStore.setChartType(chartType)
    }
}
