package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.domain.repository.HealthRepository
import javax.inject.Inject

class SyncHealthDataUseCase @Inject constructor(
    private val healthRepository: HealthRepository,
) {
    suspend operator fun invoke(daysBack: Int = 90) {
        healthRepository.syncNow(daysBack = daysBack)
    }
}
