package com.kazvoeten.omadketonics.domain.usecase

import com.kazvoeten.omadketonics.domain.repository.GroceryRepository
import java.time.LocalDate
import javax.inject.Inject

class ToggleGroceryItemUseCase @Inject constructor(
    private val groceryRepository: GroceryRepository,
) {
    suspend operator fun invoke(weekStartDate: LocalDate, itemName: String, isChecked: Boolean) {
        groceryRepository.setChecked(weekStartDate, itemName, !isChecked)
    }
}
