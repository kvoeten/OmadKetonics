package com.kazvoeten.omadketonics.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface GroceryRepository {
    fun observeChecklist(weekStartDate: LocalDate): Flow<Map<String, Boolean>>
    suspend fun setChecked(weekStartDate: LocalDate, itemName: String, checked: Boolean)
    suspend fun removeItems(weekStartDate: LocalDate, itemNames: List<String>)
    suspend fun clearWeek(weekStartDate: LocalDate)
}
