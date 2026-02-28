package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.core.common.DateProvider
import com.kazvoeten.omadketonics.domain.repository.TrackingRepository
import com.kazvoeten.omadketonics.model.DailyMood
import javax.inject.Inject

class SetMoodUseCase @Inject constructor(
    private val trackingRepository: TrackingRepository,
    private val dateProvider: DateProvider,
) {
    suspend operator fun invoke(mood: DailyMood?) {
        trackingRepository.setMood(dateProvider.today(), mood)
    }
}
