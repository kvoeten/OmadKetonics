package com.kazvoeten.omadketonics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazvoeten.omadketonics.domain.repository.HealthRepository
import com.kazvoeten.omadketonics.domain.usecase.BackfillNutritionToHealthUseCase
import com.kazvoeten.omadketonics.domain.usecase.SyncHealthDataUseCase
import com.kazvoeten.omadketonics.model.HealthAvailability
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppHealthConnectUiState(
    val showPermissionPrompt: Boolean = false,
    val isAvailable: Boolean = false,
    val hasPermissions: Boolean = false,
    val pendingOutboxCount: Int = 0,
)

sealed interface AppHealthConnectEvent {
    data object DismissPrompt : AppHealthConnectEvent
    data object RefreshPermissionState : AppHealthConnectEvent
    data class PermissionsResult(val grantedPermissions: Set<String>) : AppHealthConnectEvent
}

@HiltViewModel
class AppHealthConnectViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val syncHealthDataUseCase: SyncHealthDataUseCase,
    private val backfillNutritionToHealthUseCase: BackfillNutritionToHealthUseCase,
) : ViewModel() {
    private val dismissedPrompt = MutableStateFlow(false)

    val state = combine(
        healthRepository.connectionState,
        dismissedPrompt,
    ) { connection, dismissed ->
        val available = connection.availability == HealthAvailability.Available
        AppHealthConnectUiState(
            showPermissionPrompt = available && !connection.hasPermissions && !dismissed,
            isAvailable = available,
            hasPermissions = connection.hasPermissions,
            pendingOutboxCount = connection.pendingOutboxCount,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppHealthConnectUiState(),
    )

    fun requiredPermissions(): Set<String> = healthRepository.requiredPermissions()

    fun onEvent(event: AppHealthConnectEvent) {
        when (event) {
            AppHealthConnectEvent.DismissPrompt -> {
                dismissedPrompt.update { true }
            }

            AppHealthConnectEvent.RefreshPermissionState -> {
                viewModelScope.launch {
                    healthRepository.updateGrantedPermissions(emptySet())
                }
            }

            is AppHealthConnectEvent.PermissionsResult -> {
                viewModelScope.launch {
                    healthRepository.updateGrantedPermissions(event.grantedPermissions)
                    val hasAll = healthRepository.connectionState.first().hasPermissions
                    if (hasAll) {
                        dismissedPrompt.update { true }
                        runCatching { backfillNutritionToHealthUseCase(daysBack = 90) }
                        runCatching { syncHealthDataUseCase(daysBack = 90) }
                    }
                }
            }
        }
    }
}
