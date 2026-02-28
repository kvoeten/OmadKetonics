package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.domain.model.DisplayedWeek
import com.kazvoeten.omadketonics.domain.repository.UserPreferencesRepository
import com.kazvoeten.omadketonics.domain.repository.WeekPlanRepository
import com.kazvoeten.omadketonics.model.WeekSnapshot
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetDisplayedWeekUseCase @Inject constructor(
    private val weekPlanRepository: WeekPlanRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<DisplayedWeek?> {
        return combine(
            weekPlanRepository.observeWeeksDescending(),
            userPreferencesRepository.selectedWeekOffset,
        ) { weeks, offset ->
            if (weeks.isEmpty()) return@combine null
            val safeOffset = offset.coerceIn(0, weeks.lastIndex)
            val snapshot: WeekSnapshot = weeks[safeOffset]
            DisplayedWeek(
                snapshot = snapshot,
                selectedWeekOffset = safeOffset,
                hasOlderWeek = safeOffset < weeks.lastIndex,
                hasNewerWeek = safeOffset > 0,
                isViewingCurrentWeek = safeOffset == 0,
            )
        }
    }
}
