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
)
