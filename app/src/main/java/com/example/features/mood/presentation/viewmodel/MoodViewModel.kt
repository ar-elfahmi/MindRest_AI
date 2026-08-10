package com.example.features.mood.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.features.mood.presentation.state.MoodUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.core.network.SupabaseClient
import com.example.core.network.dto.MoodLogInsert
import com.example.features.mood.data.repository.MoodRepository
import com.example.features.mood.data.repository.MoodRepositoryImpl
import io.github.jan.supabase.auth.auth

class MoodViewModel(
    private val repository: MoodRepository = MoodRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()

    fun onMoodSelected(mood: Int) {
        _uiState.update { it.copy(selectedMood = mood) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(errorMessage = null, isSuccess = false) }
    }

    /**
     * @param moodOverride bila tidak null, pakai skor ini (dipakai DailyCheckInBottomSheet).
     *                 Bila null, ambil dari [MoodUiState.selectedMood] (form MoodTrackingScreen).
     */
    fun onSaveMoodClicked(moodOverride: Int? = null) {
        viewModelScope.launch {
            val mood = moodOverride ?: uiState.value.selectedMood ?: return@launch

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

            val logInsert = MoodLogInsert(
                userId = userId,
                moodScore = mood
            )

            val result = repository.insertMoodLog(logInsert)

            _uiState.update {
                if (result.isSuccess) {
                    it.copy(isSaving = false, errorMessage = null, selectedMood = null, isSuccess = true)
                } else {
                    it.copy(isSaving = false, errorMessage = "Failed to save mood. Please try again.", isSuccess = false)
                }
            }

            // Auto-refresh history + today check-in setelah insert berhasil.
            if (result.isSuccess) {
                onLoadHistory()
                onLoadTodayMood()
            }
        }
    }

    /**
     * Shortcut untuk DailyCheckInBottomSheet di HomeScreen: simpan skor langsung
     * tanpa harus set selectedMood lewat [onMoodSelected] terlebih dahulu.
     */
    fun saveMoodScore(score: Int) {
        onSaveMoodClicked(moodOverride = score.coerceIn(1, 5))
    }

    /**
     * Ambil 50 log mood terakhir milik user saat ini.
     * Aman dipanggil dari UI (LaunchedEffect) — hanya update state, tidak men-trigger side-effect lain.
     */
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

            repository.getMoodLogs(userId, limit)
                .onSuccess { logs ->
                    _uiState.update {
                        it.copy(isLoadingHistory = false, recentMoods = logs, historyError = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingHistory = false,
                            historyError = e.message ?: "Failed to load mood history.",
                        )
                    }
                }
        }
    }

    fun onHistoryErrorShown() {
        _uiState.update { it.copy(historyError = null) }
    }

    /**
     * Ambil skor mood TERBARU hari ini untuk user saat ini.
     * Dipakai HomeScreen sebagai single source of truth "hasCheckedInToday".
     * Aman dipanggil dari UI (LaunchedEffect). Silent fail jika client/user
     * belum siap — state tetap null (berarti belum check-in, check-in card
     * tetap ditampilkan).
     */
    fun onLoadTodayMood() {
        viewModelScope.launch {
            val client = SupabaseClient.client ?: return@launch
            val userId = client.auth.currentSessionOrNull()?.user?.id ?: ""
            if (userId.isEmpty()) return@launch
            repository.getTodayMoodScore(userId)
                .onSuccess { score ->
                    _uiState.update { it.copy(todayMoodScore = score) }
                }
            // Silent fail: keep previous value. Jangan overwrite jadi null
            // kalau query gagal — UX lebih baik membiarkan user check-in lagi
            // daripada memblokir check-in karena network error.
        }
    }

    /**
     * Ambil rata-rata mood harian 7 hari terakhir, lalu konversi ke skala 0–100
     * (skor mood asli 1–5 dinormalisasi) untuk chart di HomeScreen.
     *
     * Hari tanpa data menghasilkan skor 0 (ditampilkan empty bar di chart).
     * Aman dipanggil dari UI (LaunchedEffect).
     */
    fun onLoadWeeklyScores(days: Int = 7) {
        viewModelScope.launch {
            val client = SupabaseClient.client
            if (client == null) {
                _uiState.update {
                    it.copy(isLoadingWeekly = false, weeklyError = "Supabase belum dikonfigurasi.")
                }
                return@launch
            }
            val userId = client.auth.currentSessionOrNull()?.user?.id ?: ""
            if (userId.isEmpty()) {
                _uiState.update {
                    it.copy(isLoadingWeekly = false, weeklyError = "User not logged in")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoadingWeekly = true, weeklyError = null) }

            repository.getDailyMoodAverages(userId, days = days)
                .onSuccess { dailyAverages ->
                    // Konversi skor 1-5 ke 0-100: round(avg / 5 * 100).
                    // Hari tanpa data → 0 (chart menampilkan bar kosong).
                    val scores = dailyAverages.map { d ->
                        d.averageScore
                            ?.let { (it / 5.0 * 100).toInt().coerceIn(0, 100) }
                            ?: 0
                    }
                    _uiState.update {
                        it.copy(
                            isLoadingWeekly = false,
                            weeklyMoodScores = scores,
                            weeklyError = null,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingWeekly = false,
                            weeklyError = e.message ?: "Failed to load weekly scores.",
                        )
                    }
                }
        }
    }

    fun onWeeklyErrorShown() {
        _uiState.update { it.copy(weeklyError = null) }
    }
}
