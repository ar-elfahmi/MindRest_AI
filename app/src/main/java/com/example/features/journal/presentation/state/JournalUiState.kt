package com.example.features.journal.presentation.state

import com.example.core.network.dto.JournalEntryRow

data class JournalUiState(
    val journalText: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    // History
    val recentEntries: List<JournalEntryRow> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val historyError: String? = null,
)
