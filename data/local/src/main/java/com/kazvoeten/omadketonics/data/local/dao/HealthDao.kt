package com.kazvoeten.omadketonics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kazvoeten.omadketonics.data.local.entity.HealthDailySummaryEntity
import com.kazvoeten.omadketonics.data.local.entity.HealthOutboxEntity
import com.kazvoeten.omadketonics.data.local.entity.HealthSyncStateEntity
import com.kazvoeten.omadketonics.data.local.entity.ManualActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_daily_summary WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeDailySummaries(startDate: String, endDate: String): Flow<List<HealthDailySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailySummaries(items: List<HealthDailySummaryEntity>)

    @Query("DELETE FROM health_daily_summary WHERE date < :beforeDate")
    suspend fun deleteDailySummariesBefore(beforeDate: String)

    @Query(
        """
        SELECT * FROM manual_activity_logs
        WHERE start_time_epoch BETWEEN :startEpochMillis AND :endEpochMillis
        ORDER BY start_time_epoch DESC
        """,
    )
    fun observeManualActivities(startEpochMillis: Long, endEpochMillis: Long): Flow<List<ManualActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertManualActivity(item: ManualActivityLogEntity)

    @Query(
        """
        UPDATE manual_activity_logs
        SET outbox_status = :status, synced_at_epoch = :syncedAt
        WHERE id = :id
        """,
    )
    suspend fun updateManualActivityStatus(id: String, status: String, syncedAt: Long?)

    @Query("SELECT * FROM health_outbox WHERE status IN ('Pending', 'Failed') ORDER BY created_at_epoch ASC LIMIT :limit")
    suspend fun getPendingOutbox(limit: Int): List<HealthOutboxEntity>

    @Query("SELECT COUNT(*) FROM health_outbox WHERE status IN ('Pending', 'Failed')")
    fun observePendingOutboxCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM health_outbox WHERE status IN ('Pending', 'Failed')")
    suspend fun getPendingOutboxCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutbox(item: HealthOutboxEntity): Long

    @Query(
        """
        UPDATE health_outbox
        SET status = :status, attempts = :attempts, last_error = :lastError, updated_at_epoch = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateOutboxState(
        id: Long,
        status: String,
        attempts: Int,
        lastError: String?,
        updatedAt: Long,
    )

    @Query("DELETE FROM health_outbox WHERE id = :id")
    suspend fun deleteOutbox(id: Long)

    @Query("SELECT * FROM health_sync_state WHERE key = :key LIMIT 1")
    suspend fun getSyncState(key: String): HealthSyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncState(item: HealthSyncStateEntity)

    @Query("DELETE FROM health_outbox WHERE status = 'Synced'")
    suspend fun pruneSyncedOutbox()
}
