package com.kazvoeten.omadketonics.core.common

import java.time.LocalDate
import javax.inject.Inject

class DateProvider @Inject constructor() {
    fun today(): LocalDate = LocalDate.now()

    fun sundayStart(date: LocalDate): LocalDate {
        val daysFromSunday = date.dayOfWeek.value % 7
        return date.minusDays(daysFromSunday.toLong())
    }
}
