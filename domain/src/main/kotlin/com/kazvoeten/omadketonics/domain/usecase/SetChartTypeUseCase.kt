package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.domain.repository.UserPreferencesRepository
import com.kazvoeten.omadketonics.model.ChartType
import javax.inject.Inject

class SetChartTypeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(type: ChartType) {
        userPreferencesRepository.setChartType(type)
    }
}
