package com.kazvoeten.omadketonics.data.repository.impl

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import androidx.room.withTransaction
import com.kazvoeten.omadketonics.core.common.AppDispatchers
import com.kazvoeten.omadketonics.data.local.OmadDatabase
import com.kazvoeten.omadketonics.data.local.dao.HealthDao
import com.kazvoeten.omadketonics.data.local.entity.HealthOutboxEntity
import com.kazvoeten.omadketonics.data.local.entity.HealthSyncStateEntity
import com.kazvoeten.omadketonics.data.local.entity.ManualActivityLogEntity
import com.kazvoeten.omadketonics.data.local.mapper.toDomain
import com.kazvoeten.omadketonics.data.local.mapper.toEntity
import com.kazvoeten.omadketonics.domain.repository.HealthRepository
import com.kazvoeten.omadketonics.model.ActivitySummary
import com.kazvoeten.omadketonics.model.DailyHealthSummary
import com.kazvoeten.omadketonics.model.HealthAvailability
import com.kazvoeten.omadketonics.model.HealthConnectionState
import com.kazvoeten.omadketonics.model.HealthOutboxType
import com.kazvoeten.omadketonics.model.ManualActivityInput
import com.kazvoeten.omadketonics.model.ManualActivityLog
import com.kazvoeten.omadketonics.model.ManualActivitySource
import com.kazvoeten.omadketonics.model.SleepSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

@Singleton
class HealthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: OmadDatabase,
    private val healthDao: HealthDao,
    private val appDispatchers: AppDispatchers,
) : HealthRepository {
    private val scope = CoroutineScope(SupervisorJob() + appDispatchers.io)

    private val internalState = MutableStateFlow(
        InternalConnectionState(
            availability = sdkAvailability(),
        ),
    )

    override val connectionState: Flow<HealthConnectionState> = combine(
        internalState,
        healthDao.observePendingOutboxCount(),
    ) { state, pending ->
        HealthConnectionState(
            availability = state.availability,
            hasPermissions = state.hasPermissions,
            isSyncing = state.isSyncing,
            lastSyncedAtEpochMillis = state.lastSyncedAtEpochMillis,
            pendingOutboxCount = pending,
            lastError = state.lastError,
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = HealthConnectionState(
            availability = sdkAvailability(),
            hasPermissions = false,
            isSyncing = false,
            lastSyncedAtEpochMillis = null,
            pendingOutboxCount = 0,
            lastError = null,
        ),
    )

    init {
        scope.launch {
            val lastSync = healthDao.getSyncState(KEY_LAST_SYNC)?.valueLong
            if (lastSync != null) {
                internalState.update { it.copy(lastSyncedAtEpochMillis = lastSync) }
            }
            refreshPermissions()
        }
    }

    override fun requiredPermissions(): Set<String> = REQUIRED_PERMISSIONS

    override suspend fun updateGrantedPermissions(grantedPermissions: Set<String>) {
        val availability = sdkAvailability()
        if (availability != HealthAvailability.Available) {
            internalState.update {
                it.copy(
                    availability = availability,
                    hasPermissions = false,
                )
            }
            return
        }

        val resolvedGranted = runCatching {
            HealthConnectClient.getOrCreate(context, PROVIDER_PACKAGE_NAME)
                .permissionController
                .getGrantedPermissions()
        }.getOrElse { grantedPermissions }

        internalState.update {
            it.copy(
                availability = availability,
                hasPermissions = REQUIRED_PERMISSIONS.all(resolvedGranted::contains),
            )
        }
    }

    override fun observeDailySummaries(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<DailyHealthSummary>> {
        return healthDao.observeDailySummaries(startDate.toString(), endDate.toString())
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeManualActivityLogs(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<ManualActivityLog>> {
        val zoneId = ZoneId.systemDefault()
        val startEpoch = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endEpoch = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1L
        return healthDao.observeManualActivities(startEpoch, endEpoch)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun queueManualActivity(
        input: ManualActivityInput,
        estimatedCalories: Int,
        clientRecordId: String,
        source: String,
    ) {
        val now = System.currentTimeMillis()
        val entity = ManualActivityLogEntity(
            id = "manual-$clientRecordId",
            startTimeEpochMillis = input.startTimeEpochMillis,
            endTimeEpochMillis = input.endTimeEpochMillis,
            activityType = input.activityType,
            exertion = input.exertion,
            calories = estimatedCalories,
            source = ManualActivitySource.AppManual.name,
            outboxStatus = "Pending",
            healthClientRecordId = clientRecordId,
            notes = input.notes,
            createdAtEpochMillis = now,
            syncedAtEpochMillis = null,
        )

        val payload = JSONObject()
            .put("manualId", entity.id)
            .put("start", entity.startTimeEpochMillis)
            .put("end", entity.endTimeEpochMillis)
            .put("activityType", entity.activityType)
            .put("exertion", entity.exertion)
            .put("calories", entity.calories)
            .put("clientRecordId", clientRecordId)
            .put("notes", entity.notes)
            .put("source", source)
            .toString()

        database.withTransaction {
            healthDao.upsertManualActivity(entity)
            enqueueOutbox(
                type = HealthOutboxType.ActivityUpsert,
                payloadJson = payload,
                now = now,
            )
        }
    }

    override suspend fun queueNutritionUpsert(
        date: LocalDate,
        mealName: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
    ) {
        val payload = JSONObject()
            .put("date", date.toString())
            .put("mealName", mealName)
            .put("calories", calories)
            .put("protein", protein)
            .put("carbs", carbs)
            .put("fat", fat)
            .put("clientRecordId", nutritionClientRecordId(date))
            .toString()

        val now = System.currentTimeMillis()
        enqueueOutbox(
            type = HealthOutboxType.NutritionUpsert,
            payloadJson = payload,
            now = now,
        )
    }

    override suspend fun queueNutritionDelete(date: LocalDate) {
        val payload = JSONObject()
            .put("date", date.toString())
            .put("clientRecordId", nutritionClientRecordId(date))
            .toString()

        val now = System.currentTimeMillis()
        enqueueOutbox(
            type = HealthOutboxType.NutritionDelete,
            payloadJson = payload,
            now = now,
        )
    }

    override suspend fun syncNow(daysBack: Int) {
        val availability = sdkAvailability()
        if (availability != HealthAvailability.Available) {
            internalState.update {
                it.copy(
                    availability = availability,
                    hasPermissions = false,
                )
            }
            return
        }

        val client = runCatching {
            HealthConnectClient.getOrCreate(context, PROVIDER_PACKAGE_NAME)
        }.getOrElse { error ->
            internalState.update {
                it.copy(
                    availability = availability,
                    hasPermissions = false,
                    lastError = error.message,
                )
            }
            return
        }

        val granted = runCatching { client.permissionController.getGrantedPermissions() }
            .getOrElse { emptySet() }
        val hasPermissions = REQUIRED_PERMISSIONS.all(granted::contains)

        internalState.update {
            it.copy(
                availability = availability,
                hasPermissions = hasPermissions,
                isSyncing = hasPermissions,
                lastError = if (hasPermissions) null else "Health Connect permissions missing",
            )
        }

        if (!hasPermissions) return

        val now = System.currentTimeMillis()
        try {
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays((daysBack - 1).coerceAtLeast(0).toLong())
            val summaries = readDailyHealthSummaries(client, startDate, endDate)

            database.withTransaction {
                healthDao.upsertDailySummaries(summaries.map { it.toEntity(updatedAtEpoch = now) })
                healthDao.upsertSyncState(HealthSyncStateEntity(key = KEY_LAST_READ_DATE, valueText = endDate.toString()))
            }

            flushOutbox(client)
            healthDao.upsertSyncState(HealthSyncStateEntity(key = KEY_LAST_SYNC, valueLong = now))

            internalState.update {
                it.copy(
                    isSyncing = false,
                    lastError = null,
                    lastSyncedAtEpochMillis = now,
                )
            }
        } catch (t: Throwable) {
            internalState.update {
                it.copy(
                    isSyncing = false,
                    lastError = t.message ?: "Health sync failed",
                )
            }
        }
    }

    private suspend fun refreshPermissions() {
        val availability = sdkAvailability()
        if (availability != HealthAvailability.Available) {
            internalState.update {
                it.copy(
                    availability = availability,
                    hasPermissions = false,
                )
            }
            return
        }
        val granted = runCatching {
            HealthConnectClient.getOrCreate(context, PROVIDER_PACKAGE_NAME)
                .permissionController
                .getGrantedPermissions()
        }.getOrDefault(emptySet())
        internalState.update {
            it.copy(
                availability = availability,
                hasPermissions = REQUIRED_PERMISSIONS.all(granted::contains),
            )
        }
    }

    private suspend fun enqueueOutbox(
        type: HealthOutboxType,
        payloadJson: String,
        now: Long,
    ) {
        healthDao.insertOutbox(
            HealthOutboxEntity(
                itemType = type.name,
                payloadJson = payloadJson,
                status = "Pending",
                attempts = 0,
                lastError = null,
                createdAtEpoch = now,
                updatedAtEpoch = now,
            ),
        )
    }

    private suspend fun flushOutbox(client: HealthConnectClient) {
        val items = healthDao.getPendingOutbox(limit = 500)
        for (item in items) {
            val nextAttempts = item.attempts + 1
            healthDao.updateOutboxState(
                id = item.id,
                status = "Processing",
                attempts = nextAttempts,
                lastError = null,
                updatedAt = System.currentTimeMillis(),
            )

            runCatching {
                when (HealthOutboxType.entries.firstOrNull { it.name == item.itemType }) {
                    HealthOutboxType.ActivityUpsert -> processActivityOutbox(client, item)
                    HealthOutboxType.NutritionUpsert -> processNutritionUpsertOutbox(client, item)
                    HealthOutboxType.NutritionDelete -> processNutritionDeleteOutbox(client, item)
                    null -> error("Unknown outbox type: ${item.itemType}")
                }
            }.onSuccess {
                healthDao.updateOutboxState(
                    id = item.id,
                    status = "Synced",
                    attempts = nextAttempts,
                    lastError = null,
                    updatedAt = System.currentTimeMillis(),
                )
            }.onFailure { throwable ->
                healthDao.updateOutboxState(
                    id = item.id,
                    status = "Failed",
                    attempts = nextAttempts,
                    lastError = throwable.message,
                    updatedAt = System.currentTimeMillis(),
                )
            }
        }

        healthDao.pruneSyncedOutbox()
    }

    private suspend fun processActivityOutbox(client: HealthConnectClient, item: HealthOutboxEntity) {
        val payload = JSONObject(item.payloadJson)
        val start = Instant.ofEpochMilli(payload.getLong("start"))
        val end = Instant.ofEpochMilli(payload.getLong("end"))
        val exerciseType = mapExerciseType(payload.getString("activityType"))
        val clientRecordId = payload.getString("clientRecordId")
        val notes = payload.optString("notes").ifBlank { null }
        val calories = payload.getInt("calories").coerceAtLeast(0)
        val metadata = Metadata.manualEntry(clientRecordId = clientRecordId, clientRecordVersion = item.attempts.toLong())

        val exerciseRecord = ExerciseSessionRecord(
            startTime = start,
            startZoneOffset = zoneOffsetAt(start),
            endTime = end,
            endZoneOffset = zoneOffsetAt(end),
            metadata = metadata,
            exerciseType = exerciseType,
            title = payload.getString("activityType"),
            notes = notes,
        )

        val activeCaloriesRecord = ActiveCaloriesBurnedRecord(
            startTime = start,
            startZoneOffset = zoneOffsetAt(start),
            endTime = end,
            endZoneOffset = zoneOffsetAt(end),
            energy = Energy.kilocalories(calories.toDouble()),
            metadata = Metadata.manualEntry(clientRecordId = "$clientRecordId-kcal", clientRecordVersion = item.attempts.toLong()),
        )

        client.insertRecords(listOf(exerciseRecord))
        client.insertRecords(listOf(activeCaloriesRecord))

        payload.optString("manualId").takeIf { it.isNotBlank() }?.let { manualId ->
            healthDao.updateManualActivityStatus(
                id = manualId,
                status = "Synced",
                syncedAt = System.currentTimeMillis(),
            )
        }
    }

    private suspend fun processNutritionUpsertOutbox(client: HealthConnectClient, item: HealthOutboxEntity) {
        val payload = JSONObject(item.payloadJson)
        val date = LocalDate.parse(payload.getString("date"))
        val dateTime = LocalDateTime.of(date.year, date.month, date.dayOfMonth, 12, 0)
        val start = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        val end = dateTime.plusMinutes(1).atZone(ZoneId.systemDefault()).toInstant()
        val clientRecordId = payload.getString("clientRecordId")

        val record = NutritionRecord(
            startTime = start,
            startZoneOffset = zoneOffsetAt(start),
            endTime = end,
            endZoneOffset = zoneOffsetAt(end),
            metadata = Metadata.manualEntry(clientRecordId = clientRecordId, clientRecordVersion = item.attempts.toLong()),
            energy = Energy.kilocalories(payload.getInt("calories").toDouble()),
            protein = Mass.grams(payload.getInt("protein").toDouble()),
            totalCarbohydrate = Mass.grams(payload.getInt("carbs").toDouble()),
            totalFat = Mass.grams(payload.getInt("fat").toDouble()),
            name = payload.getString("mealName"),
            mealType = MealType.MEAL_TYPE_DINNER,
        )

        client.insertRecords(listOf(record))
    }

    private suspend fun processNutritionDeleteOutbox(client: HealthConnectClient, item: HealthOutboxEntity) {
        val payload = JSONObject(item.payloadJson)
        val clientRecordId = payload.getString("clientRecordId")
        client.deleteRecords(
            recordType = NutritionRecord::class,
            recordIdsList = emptyList(),
            clientRecordIdsList = listOf(clientRecordId),
        )
    }

    private suspend fun readDailyHealthSummaries(
        client: HealthConnectClient,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<DailyHealthSummary> {
        val zoneId = ZoneId.systemDefault()
        val startInstant = startDate.atStartOfDay(zoneId).toInstant()
        val endInstant = endDate.plusDays(1).atStartOfDay(zoneId).toInstant()

        val sleepRecords = client.readRecords(
            ReadRecordsRequest<SleepSessionRecord>(
                timeRangeFilter = TimeRangeFilter.between(startInstant, endInstant),
            ),
        ).records

        val exerciseRecords = client.readRecords(
            ReadRecordsRequest<ExerciseSessionRecord>(
                timeRangeFilter = TimeRangeFilter.between(startInstant, endInstant),
            ),
        ).records

        val activeCalorieRecords = client.readRecords(
            ReadRecordsRequest<ActiveCaloriesBurnedRecord>(
                timeRangeFilter = TimeRangeFilter.between(startInstant, endInstant),
            ),
        ).records

        val summaryMap = linkedMapOf<LocalDate, DailyHealthSummary>()
        var dateCursor = startDate
        while (!dateCursor.isAfter(endDate)) {
            summaryMap[dateCursor] = DailyHealthSummary(date = dateCursor)
            dateCursor = dateCursor.plusDays(1)
        }

        sleepRecords.forEach { record ->
            val date = record.startTime.atZone(zoneId).toLocalDate()
            val current = summaryMap[date] ?: DailyHealthSummary(date = date)
            val durationMinutes = ((record.endTime.toEpochMilli() - record.startTime.toEpochMilli()) / 60_000L)
                .toInt()
                .coerceAtLeast(0)

            var deep = 0
            var rem = 0
            var light = 0
            if (record.stages.isEmpty()) {
                light = durationMinutes
            } else {
                record.stages.forEach { stage ->
                    val stageDuration = ((stage.endTime.toEpochMilli() - stage.startTime.toEpochMilli()) / 60_000L)
                        .toInt()
                        .coerceAtLeast(0)
                    when (stage.stage) {
                        5 -> deep += stageDuration
                        6 -> rem += stageDuration
                        else -> light += stageDuration
                    }
                }
                val tracked = deep + rem + light
                if (tracked < durationMinutes) {
                    light += durationMinutes - tracked
                }
            }

            summaryMap[date] = current.copy(
                sleep = SleepSummary(
                    totalSleepMinutes = current.sleep.totalSleepMinutes + durationMinutes,
                    deepSleepMinutes = current.sleep.deepSleepMinutes + deep,
                    remSleepMinutes = current.sleep.remSleepMinutes + rem,
                    lightSleepMinutes = current.sleep.lightSleepMinutes + light,
                    sessionCount = current.sleep.sessionCount + 1,
                ),
            )
        }

        exerciseRecords.forEach { record ->
            val date = record.startTime.atZone(zoneId).toLocalDate()
            val current = summaryMap[date] ?: DailyHealthSummary(date = date)
            val durationMinutes = ((record.endTime.toEpochMilli() - record.startTime.toEpochMilli()) / 60_000L)
                .toInt()
                .coerceAtLeast(0)

            val high = if (durationMinutes >= 45) 1 else 0
            val moderate = if (durationMinutes in 25..44) 1 else 0
            val low = if (durationMinutes in 1..24) 1 else 0

            summaryMap[date] = current.copy(
                activity = ActivitySummary(
                    exerciseMinutes = current.activity.exerciseMinutes + durationMinutes,
                    activeCalories = current.activity.activeCalories,
                    sessionCount = current.activity.sessionCount + 1,
                    highIntensitySessions = current.activity.highIntensitySessions + high,
                    moderateIntensitySessions = current.activity.moderateIntensitySessions + moderate,
                    lowIntensitySessions = current.activity.lowIntensitySessions + low,
                ),
            )
        }

        activeCalorieRecords.forEach { record ->
            val date = record.startTime.atZone(zoneId).toLocalDate()
            val current = summaryMap[date] ?: DailyHealthSummary(date = date)
            summaryMap[date] = current.copy(
                activity = current.activity.copy(
                    activeCalories = current.activity.activeCalories + record.energy.inKilocalories.roundToInt(),
                ),
            )
        }

        return summaryMap.values.toList()
    }

    private fun mapExerciseType(activityType: String): Int {
        return when {
            activityType.contains("walk", ignoreCase = true) -> ExerciseSessionRecord.EXERCISE_TYPE_WALKING
            activityType.contains("run", ignoreCase = true) -> ExerciseSessionRecord.EXERCISE_TYPE_RUNNING
            activityType.contains("cycle", ignoreCase = true) -> ExerciseSessionRecord.EXERCISE_TYPE_BIKING
            activityType.contains("lift", ignoreCase = true) -> ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING
            activityType.contains("hiit", ignoreCase = true) -> ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING
            else -> ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT
        }
    }

    private fun zoneOffsetAt(instant: Instant): ZoneOffset {
        return ZoneId.systemDefault().rules.getOffset(instant)
    }

    private fun nutritionClientRecordId(date: LocalDate): String = "nutrition-$date"

    private fun sdkAvailability(): HealthAvailability {
        return when (HealthConnectClient.getSdkStatus(context, PROVIDER_PACKAGE_NAME)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthAvailability.Available
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthAvailability.ProviderUpdateRequired
            else -> HealthAvailability.Unavailable
        }
    }

    private data class InternalConnectionState(
        val availability: HealthAvailability,
        val hasPermissions: Boolean = false,
        val isSyncing: Boolean = false,
        val lastSyncedAtEpochMillis: Long? = null,
        val lastError: String? = null,
    )

    private companion object {
        const val PROVIDER_PACKAGE_NAME = "com.google.android.apps.healthdata"
        const val KEY_LAST_SYNC = "last_sync_epoch"
        const val KEY_LAST_READ_DATE = "last_read_date"

        val REQUIRED_PERMISSIONS: Set<String> = setOf(
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getWritePermission(ExerciseSessionRecord::class),
            HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(NutritionRecord::class),
            HealthPermission.getWritePermission(NutritionRecord::class),
        )
    }
}
