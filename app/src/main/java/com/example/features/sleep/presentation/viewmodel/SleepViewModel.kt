package com.example.features.sleep.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.features.sleep.presentation.state.SleepQuality
import com.example.features.sleep.presentation.state.SleepUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.Calendar
import java.util.TimeZone
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.core.network.SupabaseClient
import com.example.core.network.dto.SleepLogInsert
import com.example.features.sleep.data.repository.SleepRepository
import com.example.features.sleep.data.repository.SleepRepositoryImpl
import io.github.jan.supabase.auth.auth

class SleepViewModel(
    private val repository: SleepRepository = SleepRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    fun onBedTimeChanged(time: String) {
        _uiState.update {
            it.copy(
                bedTime = time,
                totalSleepDuration = calculateSleepDuration(time, it.wakeUpTime)
            )
        }
    }

    fun onWakeUpTimeChanged(time: String) {
        _uiState.update {
            it.copy(
                wakeUpTime = time,
                totalSleepDuration = calculateSleepDuration(it.bedTime, time)
            )
        }
    }

    fun onSleepQualityChanged(quality: SleepQuality) {
        _uiState.update { it.copy(sleepQuality = quality) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(errorMessage = null, isSuccess = false) }
    }

    /**
     * @param bedTimeOverride bila tidak null, pakai nilai ini (dipakai DailyCheckInBottomSheet).
     * @param wakeUpTimeOverride bila tidak null, pakai nilai ini.
     * @param qualityOverride bila tidak null, pakai nilai ini.
     */
    fun onSaveClicked(
        bedTimeOverride: String? = null,
        wakeUpTimeOverride: String? = null,
        qualityOverride: SleepQuality? = null,
    ) {
        val bedTime = bedTimeOverride ?: uiState.value.bedTime
        val wakeUpTime = wakeUpTimeOverride ?: uiState.value.wakeUpTime
        val quality = qualityOverride ?: uiState.value.sleepQuality

        if (!isValidTimeFormat(bedTime) || !isValidTimeFormat(wakeUpTime)) {
            _uiState.update {
                it.copy(errorMessage = "Enter bed and wake times as HH:mm (24-hour).")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, isSuccess = false) }

            val client = SupabaseClient.client
            if (client == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Supabase belum dikonfigurasi. Isi .env lalu rebuild."
                    )
                }
                return@launch
            }

            val userId = client.auth.currentSessionOrNull()?.user?.id ?: ""
            if (userId.isEmpty()) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "User not logged in") }
                return@launch
            }

            val (bedTimeIso, wakeUpTimeIso) = formatSleepTimesToIso(bedTime, wakeUpTime)
            val logInsert = SleepLogInsert(
                userId = userId,
                bedTime = bedTimeIso,
                wakeUpTime = wakeUpTimeIso,
                sleepQuality = quality.name
            )

            val result = repository.insertSleepLog(logInsert)

            _uiState.update {
                if (result.isSuccess) {
                    it.copy(isSaving = false, errorMessage = null, isSuccess = true)
                } else {
                    it.copy(isSaving = false, errorMessage = "Failed to save sleep log. Please try again.", isSuccess = false)
                }
            }

            if (result.isSuccess) onLoadHistory()
        }
    }

    /**
     * Shortcut untuk DailyCheckInBottomSheet: simpan log tidur langsung dengan
     * nilai eksplisit (HH:mm 24-jam + kualitas) tanpa set state form terlebih dahulu.
     */
    fun saveSleepLog(bedTime: String, wakeUpTime: String, quality: SleepQuality) {
        onSaveClicked(
            bedTimeOverride = bedTime,
            wakeUpTimeOverride = wakeUpTime,
            qualityOverride = quality,
        )
    }

    fun onLoadHistory(limit: Long = 50) {
        viewModelScope.launch {
            val client = SupabaseClient.client
            if (client == null) {
                _uiState.update { it.copy(historyError = "Supabase belum dikonfigurasi.") }
                return@launch
            }
            val userId = client.auth.currentSessionOrNull()?.user?.id ?: ""
            if (userId.isEmpty()) {
                _uiState.update { it.copy(historyError = "User not logged in") }
                return@launch
            }

            _uiState.update { it.copy(isLoadingHistory = true, historyError = null) }

            repository.getSleepLogs(userId, limit)
                .onSuccess { logs ->
                    _uiState.update {
                        it.copy(isLoadingHistory = false, recentSleepLogs = logs, historyError = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingHistory = false,
                            historyError = e.message ?: "Failed to load sleep history.",
                        )
                    }
                }
        }
    }

    fun onHistoryErrorShown() {
        _uiState.update { it.copy(historyError = null) }
    }

    private fun calculateSleepDuration(bedTime: String, wakeUpTime: String): String {
        try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val bedDate: Date = format.parse(bedTime) ?: return "0 hours 0 minutes"
            var wakeDate: Date = format.parse(wakeUpTime) ?: return "0 hours 0 minutes"

            if (wakeDate.before(bedDate)) {
                wakeDate = Date(wakeDate.time + (1000 * 60 * 60 * 24))
            }

            val diffMs = wakeDate.time - bedDate.time
            val diffHours = diffMs / (1000 * 60 * 60)
            val diffMinutes = (diffMs / (1000 * 60)) % 60

            return "$diffHours hours $diffMinutes minutes"
        } catch (e: Exception) {
            return "Invalid time format"
        }
    }

    private fun isValidTimeFormat(time: String): Boolean {
        val parts = time.split(":")
        if (parts.size != 2) return false
        val hour = parts[0].toIntOrNull() ?: return false
        val minute = parts[1].toIntOrNull() ?: return false
        return hour in 0..23 && minute in 0..59
    }

    private fun timeToUtcCalendar(time: String): Calendar? {
        val parts = time.split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun formatSleepTimesToIso(bedTime: String, wakeUpTime: String): Pair<String, String> {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")

        val bedCal = timeToUtcCalendar(bedTime)
        var wakeCal = timeToUtcCalendar(wakeUpTime)
        if (bedCal != null && wakeCal != null && wakeCal.before(bedCal)) {
            wakeCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        val bedIso = bedCal?.let { isoFormat.format(it.time) } ?: isoFormat.format(Date())
        val wakeIso = wakeCal?.let { isoFormat.format(it.time) } ?: isoFormat.format(Date())
        return bedIso to wakeIso
    }
}
