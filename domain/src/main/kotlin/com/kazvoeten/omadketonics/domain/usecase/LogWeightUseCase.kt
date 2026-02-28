package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import javax.inject.Inject

class LogWeightUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository,
    private val dateProvider: DateProvider,
) {
    suspend operator fun invoke(input: String): Boolean {
        val value = input.toFloatOrNull() ?: return false
        trackingRepository.setWeight(dateProvider.today(), value)
        return true
    }
}
