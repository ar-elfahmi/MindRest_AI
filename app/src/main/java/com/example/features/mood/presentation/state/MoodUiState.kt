package com.example.features.mood.presentation.state

import com.example.core.network.dto.MoodLogRow

data class MoodUiState(
    val selectedMood: Int? = null, // 1 to 5 scale (1: Terrible, 5: Awesome)
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    // History — di-load terpisah lewat onLoadHistory()
    val recentMoods: List<MoodLogRow> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val historyError: String? = null,
    // Weekly aggregation — di-load terpisah lewat onLoadWeeklyScores().
    // 7 elemen (Monday..Sunday), rentang 0..100. Hari tanpa data = 0.
    val weeklyMoodScores: List<Int> = List(7) { 0 },
    val isLoadingWeekly: Boolean = false,
    val weeklyError: String? = null,
    // Skor mood TERBARU hari ini (device-local). Null = belum check-in.
    // Single source of truth untuk HomeScreen "hasCheckedInToday".
    val todayMoodScore: Int? = null,
)
