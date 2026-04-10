package com.devpa.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.journeyPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "journey_prefs")

object JourneyPrefs {
    val ACTIVE_JOURNEY_ID = longPreferencesKey("active_journey_id")
    val JOURNEY_VIEW_MODE = stringPreferencesKey("journey_view_mode")
}

@Singleton
class JourneyPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val activeJourneyId: Flow<Long?> = context.journeyPrefsDataStore.data.map { prefs ->
        prefs[JourneyPrefs.ACTIVE_JOURNEY_ID]
    }

    suspend fun setActiveJourney(id: Long) {
        context.journeyPrefsDataStore.edit { prefs ->
            prefs[JourneyPrefs.ACTIVE_JOURNEY_ID] = id
        }
    }

    val viewMode: Flow<String> = context.journeyPrefsDataStore.data.map { prefs ->
        prefs[JourneyPrefs.JOURNEY_VIEW_MODE] ?: "list"
    }

    suspend fun setViewMode(mode: String) {
        context.journeyPrefsDataStore.edit { prefs ->
            prefs[JourneyPrefs.JOURNEY_VIEW_MODE] = mode
        }
    }
}
