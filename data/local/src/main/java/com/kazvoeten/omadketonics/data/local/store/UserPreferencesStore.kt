package com.kazvoeten.omadketonics.data.local.store

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kazvoeten.omadketonics.model.ChartType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore by preferencesDataStore(name = "omad_preferences")

class UserPreferencesStore(
    private val context: Context,
) {
    val selectedWeekOffset: Flow<Int> = context.userPrefsDataStore.data.map { prefs ->
        prefs[KEY_SELECTED_WEEK_OFFSET] ?: 0
    }

    val chartType: Flow<ChartType> = context.userPrefsDataStore.data.map { prefs ->
        val raw = prefs[KEY_CHART_TYPE] ?: ChartType.Weight.name
        ChartType.entries.firstOrNull { it.name == raw } ?: ChartType.Weight
    }

    suspend fun setSelectedWeekOffset(offset: Int) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[KEY_SELECTED_WEEK_OFFSET] = offset
        }
    }

    suspend fun setChartType(chartType: ChartType) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[KEY_CHART_TYPE] = chartType.name
        }
    }

    private companion object {
        val KEY_SELECTED_WEEK_OFFSET: Preferences.Key<Int> = intPreferencesKey("selected_week_offset")
        val KEY_CHART_TYPE: Preferences.Key<String> = stringPreferencesKey("chart_type")
    }
}
