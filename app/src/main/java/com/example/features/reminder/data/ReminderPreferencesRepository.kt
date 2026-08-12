package com.example.features.reminder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore-backed persistence for bedtime reminder settings.
 *
 * Scope (T-009): stores 3 keys — hour, minute, enabled. Local-only (no DB).
 * Used by [com.example.features.reminder.presentation.viewmodel.ReminderViewModel]
 * for read/write and by [com.example.features.reminder.BootCompletedReceiver]
 * for re-scheduling after device reboot.
 */
private val Context.reminderDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "reminder_preferences"
)

class ReminderPreferencesRepository(private val context: Context) {

    private val dataStore: DataStore<Preferences> get() = context.reminderDataStore

    /** Reactive Flow — emits when keys change. */
    val flow: Flow<ReminderSettings> = dataStore.data
        .catch { e ->
            // IOException when reading fails (corruption, disk full) — fall back to default.
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            ReminderSettings(
                hour = prefs[KEY_HOUR] ?: DEFAULT_HOUR,
                minute = prefs[KEY_MINUTE] ?: DEFAULT_MINUTE,
                enabled = prefs[KEY_ENABLED] ?: false
            )
        }

    suspend fun save(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_HOUR] = hour
            prefs[KEY_MINUTE] = minute
            prefs[KEY_ENABLED] = true
        }
    }

    suspend fun disable() {
        dataStore.edit { prefs ->
            prefs[KEY_ENABLED] = false
        }
    }

    /** Snapshot read (single-shot) for receivers that can't observe Flow. */
    suspend fun read(): ReminderSettings {
        return flow.first()
    }

    companion object {
        private val KEY_HOUR = intPreferencesKey("reminder_hour")
        private val KEY_MINUTE = intPreferencesKey("reminder_minute")
        private val KEY_ENABLED = booleanPreferencesKey("reminder_enabled")

        // Defaults mirror the existing BedtimeNotificationHelper hard-coded values
        // (23:15 ideal bedtime, 30 min lead → 22:45 fire).
        const val DEFAULT_HOUR = 23
        const val DEFAULT_MINUTE = 15
    }
}

data class ReminderSettings(
    val hour: Int = ReminderPreferencesRepository.DEFAULT_HOUR,
    val minute: Int = ReminderPreferencesRepository.DEFAULT_MINUTE,
    val enabled: Boolean = false
)
