package com.kazvoeten.omadketonics.domain.model

import com.kazvoeten.omadketonics.model.WeekSnapshot

data class DisplayedWeek(
    val snapshot: WeekSnapshot,
    val selectedWeekOffset: Int,
    val hasOlderWeek: Boolean,
    val hasNewerWeek: Boolean,
    val isViewingCurrentWeek: Boolean,
)
