package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetSelectedWeekOffsetUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(offset: Int) {
        userPreferencesRepository.setSelectedWeekOffset(offset.coerceAtLeast(0))
    }
}
