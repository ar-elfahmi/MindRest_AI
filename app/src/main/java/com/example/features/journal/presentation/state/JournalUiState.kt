package com.example.features.journal.presentation.state

import com.example.core.network.dto.JournalEntryRow

/**
 * UI state untuk JournalScreen (legacy full-entry) + AiJournalScreen (chat).
 *
 * T-003 (FR-009, FR-011):
 *   - Tambah field untuk chat conversation (messages, sessionId, isSending, dll).
 *   - Field legacy (journalText, isSaving) tetap dipertahankan agar
 *     `JournalScreen` (yang dipakai JournalHistoryScreen.FAB → JournalScreen)
 *     tidak rusak.
 */
data class JournalUiState(
    // -------------------- Legacy full-entry (JournalScreen) --------------------
    val journalText: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,

    // -------------------- History list (JournalHistoryScreen) --------------------
    val recentEntries: List<JournalEntryRow> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val historyError: String? = null,

    // -------------------- AI Chat (AiJournalScreen) --------------------
    /** Conversation history untuk sesi aktif. Ordered ASC (kronologis). */
    val chatMessages: List<JournalEntryRow> = emptyList(),
    /** Session ID untuk chat aktif. Null = belum ada sesi / sesi baru. */
    val chatSessionId: String? = null,
    /** Sedang memanggil Edge Function `chat-gemini` (loading indicator). */
    val isSendingMessage: Boolean = false,
    /** Error spesifik chat (tampil inline di AiJournalScreen). */
    val chatError: String? = null,
    /** Sedang load history pertama kali saat AiJournalScreen dibuka. */
    val isLoadingChatHistory: Boolean = false,
)
