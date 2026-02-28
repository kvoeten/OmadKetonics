package com.kazvoeten.omadketonics.data.repository.impl

import com.kazvoeten.omadketonics.data.local.dao.GroceryDao
import com.kazvoeten.omadketonics.data.local.entity.GroceryCheckEntity
import com.kazvoeten.omadketonics.domain.repository.GroceryRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class GroceryRepositoryImpl @Inject constructor(
    private val groceryDao: GroceryDao,
) : GroceryRepository {
    override fun observeChecklist(weekStartDate: LocalDate): Flow<Map<String, Boolean>> {
        return groceryDao.observeChecklist(weekStartDate.toString()).map { list ->
            list.associate { it.itemName to it.checked }
        }
    }

    override suspend fun setChecked(weekStartDate: LocalDate, itemName: String, checked: Boolean) {
        groceryDao.upsert(
            GroceryCheckEntity(
                weekStart = weekStartDate.toString(),
                itemName = itemName,
                checked = checked,
            ),
        )
    }

    override suspend fun clearWeek(weekStartDate: LocalDate) {
        groceryDao.clearWeek(weekStartDate.toString())
    }
}
