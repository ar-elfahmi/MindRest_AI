package com.example.features.journal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.journal.presentation.state.JournalUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.features.journal.data.repository.JournalRepository
import com.example.features.journal.data.repository.JournalRepositoryImpl
import com.example.core.network.dto.JournalEntryInsert
import com.example.core.network.SupabaseClient
import io.github.jan.supabase.auth.auth

class JournalViewModel(
    private val repository: JournalRepository = JournalRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    fun onTextChanged(text: String) {
        _uiState.update { it.copy(journalText = text) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(errorMessage = null, isSuccess = false) }
    }

    fun onSaveEntryClicked() {
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

            val entry = JournalEntryInsert(
                userId = userId,
                content = uiState.value.journalText
            )

            val result = repository.insertJournalEntry(entry)

            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        isSaving = false,
                        journalText = "",
                        errorMessage = null,
                        isSuccess = true
                    )
                } else {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save journal entry. Please try again.",
                        isSuccess = false
                    )
                }
            }

            if (result.isSuccess) onLoadHistory()
        }
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

            repository.getJournalEntries(userId, limit)
                .onSuccess { logs ->
                    _uiState.update {
                        it.copy(isLoadingHistory = false, recentEntries = logs, historyError = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingHistory = false,
                            historyError = e.message ?: "Failed to load journal history.",
                        )
                    }
                }
        }
    }

    fun onHistoryErrorShown() {
        _uiState.update { it.copy(historyError = null) }
    }
}
