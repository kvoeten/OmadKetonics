package com.kazvoeten.omadketonics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazvoeten.omadketonics.domain.usecase.GetDisplayedWeekUseCase
import com.kazvoeten.omadketonics.domain.usecase.SetSelectedWeekOffsetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WeekNavUiState(
    val title: String = "",
    val subtitle: String = "",
    val hasOlderWeek: Boolean = false,
    val hasNewerWeek: Boolean = false,
    val selectedWeekOffset: Int = 0,
)

@HiltViewModel
class WeekNavViewModel @Inject constructor(
    private val getDisplayedWeekUseCase: GetDisplayedWeekUseCase,
    private val setSelectedWeekOffsetUseCase: SetSelectedWeekOffsetUseCase,
) : ViewModel() {
    val state = getDisplayedWeekUseCase().map { displayedWeek ->
        if (displayedWeek == null) return@map WeekNavUiState()
        val fmt = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
        val title = "Week Of ${displayedWeek.snapshot.startDate.format(fmt)}"
        val eaten = displayedWeek.snapshot.eatenMealIds.count { displayedWeek.snapshot.mealIds.contains(it) }
        val subtitle = "${if (displayedWeek.isViewingCurrentWeek) "Current" else "Past"} - $eaten / ${displayedWeek.snapshot.mealIds.size} eaten"
        WeekNavUiState(
            title = title,
            subtitle = subtitle,
            hasOlderWeek = displayedWeek.hasOlderWeek,
            hasNewerWeek = displayedWeek.hasNewerWeek,
            selectedWeekOffset = displayedWeek.selectedWeekOffset,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeekNavUiState(),
    )

    fun goToOlderWeek() {
        val current = state.value
        if (!current.hasOlderWeek) return
        viewModelScope.launch {
            setSelectedWeekOffsetUseCase(current.selectedWeekOffset + 1)
        }
    }

    fun goToNewerWeek() {
        val current = state.value
        if (!current.hasNewerWeek) return
        viewModelScope.launch {
            setSelectedWeekOffsetUseCase((current.selectedWeekOffset - 1).coerceAtLeast(0))
        }
    }
}
