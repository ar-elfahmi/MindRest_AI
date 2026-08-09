package com.example.features.sleep.presentation.state

import com.example.core.network.dto.SleepLogRow

data class SleepUiState(
    val bedTime: String = "22:00",
    val wakeUpTime: String = "06:00",
    val sleepQuality: SleepQuality = SleepQuality.GOOD,
    val totalSleepDuration: String = "8 hours 0 minutes",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    // History
    val recentSleepLogs: List<SleepLogRow> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val historyError: String? = null,
)

enum class SleepQuality {
    POOR, FAIR, GOOD, EXCELLENT
}
