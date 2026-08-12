package com.example.features.lifestyle.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.SupabaseClient
import com.example.features.lifestyle.data.repository.NoSleepLogsException
import com.example.features.lifestyle.data.repository.SleepInsightEdgeFunctionException
import com.example.features.lifestyle.data.repository.SleepInsightRepository
import com.example.features.lifestyle.data.repository.SleepInsightRepositoryImpl
import com.example.features.lifestyle.presentation.state.LifestyleUiState
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk LifestyleScreen — fokus pada Sleep Insight flow (T-005 / FR-014).
 *
 * Flow:
 *   1. Tap tombol "Generate Insight" → onGenerateInsight(periodDays)
 *   2. ViewModel call repository.generateInsight(periodDays)
 *   3. Loading state, lalu hasilnya di-set ke state.insight
 *   4. UI render 3 section (activities/foods/music) + summary
 *   5. Tap "Refresh" → ulangi dengan data terbaru
 *
 * Edge cases:
 *   - User belum login → tampilkan "User not logged in"
 *   - Belum ada log tidur dalam window → tampilkan "no_sleep_logs" CTA
 *   - Edge Function error (rate limit / safety / unknown) → tampilkan
 *     error code + message di snackbar
 */
class LifestyleViewModel(
    private val repository: SleepInsightRepository = SleepInsightRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(LifestyleUiState())
    val uiState: StateFlow<LifestyleUiState> = _uiState.asStateFlow()

    // -------------------- GENERATE --------------------

    /**
     * Trigger generate insight via Edge Function `generate-sleep-insight`.
     *
     * @param periodDays Window analisis (default 7). Server akan clamp ke
     *                   1..30. Invalid values di-ignore.
     */
    fun onGenerateInsight(periodDays: Int = 7) {
        if (_uiState.value.isGeneratingInsight) return // guard double-tap
        viewModelScope.launch {
            // Guard: Supabase belum dikonfigurasi.
            val client = SupabaseClient.client
            if (client == null) {
                _uiState.update {
                    it.copy(
                        isGeneratingInsight = false,
                        errorMessage = "Supabase belum dikonfigurasi. Isi .env lalu rebuild.",
                    )
                }
                return@launch
            }
            // Guard: belum login.
            val userId = client.auth.currentSessionOrNull()?.user?.id ?: ""
            if (userId.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isGeneratingInsight = false,
                        errorMessage = "User not logged in",
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isGeneratingInsight = true,
                    errorMessage = null,
                    emptyLogsMessage = null,
                )
            }

            val result = repository.generateInsight(periodDays = periodDays)
            _uiState.update { current ->
                when {
                    result.isSuccess -> {
                        val response = result.getOrNull()
                        current.copy(
                            isGeneratingInsight = false,
                            insight = response?.insight,
                            errorMessage = null,
                            emptyLogsMessage = null,
                            infoMessage = "Insight berhasil dibuat ✨",
                        )
                    }
                    result.exceptionOrNull() is NoSleepLogsException -> {
                        current.copy(
                            isGeneratingInsight = false,
                            insight = null,
                            errorMessage = null,
                            emptyLogsMessage = result.exceptionOrNull()?.message
                                ?: "Belum ada log tidur. Tambah log tidur dulu.",
                        )
                    }
                    else -> {
                        current.copy(
                            isGeneratingInsight = false,
                            errorMessage = friendlyErrorMessage(
                                result.exceptionOrNull(),
                            ),
                        )
                    }
                }
            }
        }
    }

    // -------------------- MESSAGE HANDLERS --------------------

    /** Dismiss error banner / snackbar. */
    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Dismiss info message (snackbar). */
    fun onInfoMessageShown() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    // -------------------- helpers --------------------

    /**
     * Map exception ke pesan user-friendly. Prioritas:
     *   - [SleepInsightEdgeFunctionException] dengan errorCode → tampilkan kode +
     *     pesan (code = `rate_limited`, `no_sleep_logs`, dll).
     *   - Lainnya → tampilkan exception message apa adanya.
     */
    private fun friendlyErrorMessage(e: Throwable?): String {
        if (e == null) return "Terjadi kesalahan. Coba lagi."
        return when (e) {
            is SleepInsightEdgeFunctionException -> {
                val code = e.errorCode ?: "edge_function_error"
                val http = e.httpStatus?.let { " (HTTP $it)" } ?: ""
                "$code$http: ${e.message}"
            }
            else -> e.message ?: "Terjadi kesalahan. Coba lagi."
        }
    }
}
