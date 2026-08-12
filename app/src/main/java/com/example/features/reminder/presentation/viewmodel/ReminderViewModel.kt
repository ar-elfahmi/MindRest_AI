package com.example.features.reminder.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.reminder.BedtimeNotificationHelper
import com.example.features.reminder.data.ReminderPreferencesRepository
import com.example.features.reminder.data.ReminderSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk ReminderScreen — T-009 (FR-016).
 *
 * Hanya mengatur **bedtime reminder** (notification wind-down 30 min sebelum
 * ideal bedtime). Switch "morning/midday/evening" yang lain di screen tetap
 * dipakai Compose `remember` (offline toggle, belum di-schedule — bukan
 * scope T-009).
 *
 * Persistence pakai [ReminderPreferencesRepository] (DataStore Preferences)
 * supaya:
 * 1. Survive app restart (saat user buka ReminderScreen, state langsung load)
 * 2. Bisa di-read oleh [com.example.features.reminder.BootCompletedReceiver]
 *    untuk reschedule alarm setelah device reboot.
 *
 * Pattern: extends [AndroidViewModel] untuk akses `getApplication()` →
 * `BedtimeNotificationHelper.scheduleBedtimeNotification(context, hour, minute)`.
 * `viewModel()` factory (default) otomatis dapat `Application` saat instantiate.
 */
class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReminderPreferencesRepository(application)

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /**
     * Baca settings dari DataStore → update UI state.
     * Dipanggil sekali di `init` + setelah [setReminderTime] / [cancelReminder].
     */
    fun load() {
        viewModelScope.launch {
            val settings: ReminderSettings = repository.read()
            _uiState.update {
                it.copy(
                    hour = settings.hour,
                    minute = settings.minute,
                    isEnabled = settings.enabled,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Simpan hour+minute ke DataStore + schedule AlarmManager via
     * [BedtimeNotificationHelper]. BedtimeNotificationHelper hard-code
     * leadTime=30 (wind-down 30 min sebelum ideal bedtime).
     */
    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.save(hour, minute)
            BedtimeNotificationHelper.scheduleBedtimeNotification(
                context = getApplication(),
                bedtimeHour = hour,
                bedtimeMinute = minute
            )
            _uiState.update {
                it.copy(hour = hour, minute = minute, isEnabled = true)
            }
        }
    }

    /** Matikan reminder: disable di DataStore + cancel AlarmManager. */
    fun cancelReminder() {
        viewModelScope.launch {
            repository.disable()
            BedtimeNotificationHelper.cancelBedtimeNotification(getApplication())
            _uiState.update { it.copy(isEnabled = false) }
        }
    }

    /** Update hour dari TimePicker UI (belum persist — user tekan "Simpan" dulu). */
    fun onHourChange(hour: Int) {
        _uiState.update { it.copy(hour = hour) }
    }

    /** Update minute dari TimePicker UI (belum persist — user tekan "Simpan" dulu). */
    fun onMinuteChange(minute: Int) {
        _uiState.update { it.copy(minute = minute) }
    }
}

data class ReminderUiState(
    val hour: Int = ReminderPreferencesRepository.DEFAULT_HOUR,
    val minute: Int = ReminderPreferencesRepository.DEFAULT_MINUTE,
    val isEnabled: Boolean = false,
    val isLoading: Boolean = true
)
