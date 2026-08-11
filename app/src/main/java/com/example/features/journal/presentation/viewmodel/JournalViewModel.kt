package com.example.features.journal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.SupabaseClient
import com.example.core.network.dto.JournalEntryInsert
import com.example.features.journal.data.repository.JournalEdgeFunctionException
import com.example.features.journal.data.repository.JournalRepository
import com.example.features.journal.data.repository.JournalRepositoryImpl
import com.example.features.journal.presentation.state.JournalUiState
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel untuk semua screen Journal:
 *   - JournalScreen          (legacy full-entry save)
 *   - JournalHistoryScreen   (history list)
 *   - AiJournalScreen        (chat dengan Edge Function chat-gemini — T-003)
 *
 * T-003: tambah flow chat (startNewSession, sendMessage, loadConversationHistory).
 */
class JournalViewModel(
    private val repository: JournalRepository = JournalRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    // -------------------- LEGACY: full-entry (JournalScreen) --------------------

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
                        errorMessage = "Supabase belum dikonfigurasi. Isi .env lalu rebuild.",
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
                content = uiState.value.journalText,
            )

            val result = repository.insertJournalEntry(entry)

            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        isSaving = false,
                        journalText = "",
                        errorMessage = null,
                        isSuccess = true,
                    )
                } else {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save journal entry. Please try again.",
                        isSuccess = false,
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

    // -------------------- T-003: AI CHAT (AiJournalScreen) --------------------

    /**
     * Mulai sesi chat baru. Generate UUID fresh, reset messages.
     * Dipanggil saat user tap tombol "Mulai Refleksi" atau saat AiJournalScreen
     * dibuka pertama kali (kalau belum ada sesi).
     */
    fun onStartNewChatSession() {
        _uiState.update {
            it.copy(
                chatSessionId = UUID.randomUUID().toString(),
                chatMessages = emptyList(),
                chatError = null,
            )
        }
    }

    /**
     * Load conversation history untuk [sessionId]. Dipakai saat
     * AiJournalScreen dibuka dengan sessionId existing (mis. dari notifikasi
     * deep link) atau saat pertama load tanpa sessionId (akan start baru).
     *
     * Kalau [sessionId] null → otomatis start sesi baru (no history).
     */
    fun onLoadChatHistory(sessionId: String?) {
        viewModelScope.launch {
            val client = SupabaseClient.client
            if (client == null) {
                _uiState.update {
                    it.copy(
                        chatError = "Supabase belum dikonfigurasi. Isi .env lalu rebuild.",
                        chatSessionId = sessionId ?: UUID.randomUUID().toString(),
                    )
                }
                return@launch
            }
            val userId = client.auth.currentSessionOrNull()?.user?.id ?: ""
            if (userId.isEmpty()) {
                _uiState.update {
                    it.copy(
                        chatError = "User not logged in",
                        chatSessionId = sessionId ?: UUID.randomUUID().toString(),
                    )
                }
                return@launch
            }

            // Tanpa sessionId → langsung sesi baru, skip fetch.
            if (sessionId == null) {
                _uiState.update {
                    it.copy(
                        chatSessionId = UUID.randomUUID().toString(),
                        chatMessages = emptyList(),
                        chatError = null,
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    chatSessionId = sessionId,
                    isLoadingChatHistory = true,
                    chatError = null,
                )
            }

            repository.getConversationHistory(userId, sessionId)
                .onSuccess { rows ->
                    _uiState.update {
                        it.copy(
                            isLoadingChatHistory = false,
                            chatMessages = rows,
                            chatError = null,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingChatHistory = false,
                            chatError = e.message ?: "Gagal memuat riwayat chat.",
                        )
                    }
                }
        }
    }

    /**
     * Kirim pesan user ke Edge Function `chat-gemini`.
     *
     * Flow:
     *   1. Generate UUID untuk user message (id pre-determined).
     *   2. Save user message ke `journal_entries`.
     *   3. Append user row ke state (UI langsung tampil).
     *   4. Panggil EF `chat-gemini` → dapat AI reply text.
     *   5. Generate UUID untuk assistant message (parent_id = user UUID).
     *   6. Save assistant row ke `journal_entries`.
     *   7. Append assistant row ke state.
     *
     * Edge case:
     *   - Kalau user message save gagal → tampilkan error, JANGAN panggil EF.
     *   - Kalau EF gagal → tampilkan error, JANGAN append apapun (user
     *     message sudah tersimpan → user bisa retry).
     *   - Kalau assistant save gagal → tetap tampilkan reply inline dengan
     *     synthetic row (UI tidak boleh kosong setelah AI sudah jawab).
     */
    fun onSendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        if (_uiState.value.isSendingMessage) return // guard double-tap

        // Pastikan ada sessionId (kalau belum, start baru).
        val currentSession = _uiState.value.chatSessionId ?: UUID.randomUUID().toString()
        if (_uiState.value.chatSessionId == null) {
            _uiState.update { it.copy(chatSessionId = currentSession) }
        }

        viewModelScope.launch {
            val client = SupabaseClient.client
            if (client == null) {
                _uiState.update {
                    it.copy(chatError = "Supabase belum dikonfigurasi. Isi .env lalu rebuild.")
                }
                return@launch
            }
            val userId = client.auth.currentSessionOrNull()?.user?.id ?: ""
            if (userId.isEmpty()) {
                _uiState.update { it.copy(chatError = "User not logged in") }
                return@launch
            }

            _uiState.update { it.copy(isSendingMessage = true, chatError = null) }

            // 1) Pre-generate UUID untuk user message → bisa langsung pakai
            //    untuk parent_id AI reply tanpa SELECT roundtrip.
            val userMessageId = UUID.randomUUID().toString()
            val userInsert = JournalEntryInsert(
                id = userMessageId,
                userId = userId,
                content = trimmed,
                sessionId = currentSession,
                role = "user",
            )

            val userSaveResult = repository.saveJournalEntry(userInsert)
            if (userSaveResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isSendingMessage = false,
                        chatError = "Gagal mengirim pesan. Coba lagi.",
                    )
                }
                return@launch
            }

            // Konstruksi user row in-memory untuk append ke state.
            val userRow = com.example.core.network.dto.JournalEntryRow(
                id = userMessageId,
                userId = userId,
                content = trimmed,
                createdAt = "", // akan terisi saat reload history; tidak kritikal untuk UI
                sessionId = currentSession,
                role = "user",
                parentId = null,
            )
            _uiState.update { it.copy(chatMessages = it.chatMessages + userRow) }

            // 2) Panggil Edge Function `chat-gemini`.
            val efResult = repository.callChatGemini(
                sessionId = currentSession,
                userMessage = trimmed,
            )

            val replyText = efResult.fold(
                onSuccess = { it.reply },
                onFailure = { e ->
                    val msg = friendlyErrorMessage(e)
                    _uiState.update {
                        it.copy(
                            isSendingMessage = false,
                            chatError = msg,
                        )
                    }
                    return@launch
                },
            )

            // 3) Save AI reply.
            val assistantMessageId = UUID.randomUUID().toString()
            val assistantInsert = JournalEntryInsert(
                id = assistantMessageId,
                userId = userId,
                content = replyText,
                sessionId = currentSession,
                role = "assistant",
                parentId = userMessageId,
            )

            val assistantRow = com.example.core.network.dto.JournalEntryRow(
                id = assistantMessageId,
                userId = userId,
                content = replyText,
                createdAt = "",
                sessionId = currentSession,
                role = "assistant",
                parentId = userMessageId,
            )

            val assistantSave = repository.saveJournalEntry(assistantInsert)

            _uiState.update { current ->
                if (assistantSave.isSuccess) {
                    current.copy(
                        isSendingMessage = false,
                        chatMessages = current.chatMessages + assistantRow,
                        chatError = null,
                    )
                } else {
                    // Reply sudah diterima dari EF tapi gagal di-save ke DB.
                    // Tetap tampilkan reply inline + warning minor.
                    current.copy(
                        isSendingMessage = false,
                        chatMessages = current.chatMessages + assistantRow,
                        chatError = "Respons AI diterima tapi gagal disimpan ke riwayat.",
                    )
                }
            }
        }
    }

    /** Dismiss error banner di AiJournalScreen. */
    fun onChatErrorShown() {
        _uiState.update { it.copy(chatError = null) }
    }

    // -------------------- helpers --------------------

    /**
     * Map exception ke pesan user-friendly. Prioritas:
     *   - [JournalEdgeFunctionException] dengan errorCode → tampilkan kode +
     *     pesan (code = `missing_api_key`, `rate_limited`, dll).
     *   - Lainnya → tampilkan exception message apa adanya.
     */
    private fun friendlyErrorMessage(e: Throwable): String {
        return when (e) {
            is JournalEdgeFunctionException -> {
                val code = e.errorCode ?: "edge_function_error"
                val http = e.httpStatus?.let { " (HTTP $it)" } ?: ""
                "$code$http: ${e.message}"
            }
            else -> e.message ?: "Terjadi kesalahan. Coba lagi."
        }
    }
}
