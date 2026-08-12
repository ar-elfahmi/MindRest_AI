package com.example.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.SupabaseClient
import com.example.features.home.presentation.state.HomeUiState
import com.example.features.ikigai.data.repository.IkigaiRepository
import com.example.features.ikigai.data.repository.IkigaiRepositoryImpl
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk HomeScreen — fokus T-004 (FR-015 Dashboard Integration).
 *
 * Aggregation mood / sleep TETAP di MoodViewModel / SleepViewModel (sesuai
 * scope T-2A / T-2B + "DON'T Touch" di T-004). HomeViewModel ini baru
 * bertanggung jawab untuk 2 widget yang BELUM ada di HomeScreen:
 * 1. Ikigai progress (count via IkigaiRepository.getAssessmentCount).
 * 2. Sleep Insight preview (placeholder null untuk T-005).
 *
 * Pattern: ViewModel terpisah (bukan di-extend ke MoodVM/SleepVM) supaya
 * HomeScreen tetap simpel dan setiap ViewModel punya single responsibility.
 * State consumption dari beberapa VM paralel adalah pola yang sehat di Compose.
 */
class HomeViewModel(
    private val ikigaiRepository: IkigaiRepository = IkigaiRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Hitung jumlah assessment Ikigai user saat ini lewat [IkigaiRepository].
     * Aman dipanggil dari UI (LaunchedEffect). Silent-fail ke 0 kalau Supabase
     * belum konfigurasi atau user belum login — UX tetap menampilkan empty
     * state CTA tanpa crash.
     */
    fun onLoadIkigaiAssessmentCount() {
        viewModelScope.launch {
            val client = SupabaseClient.client
            if (client == null) {
                _uiState.update {
                    it.copy(isLoadingIkigai = false, ikigaiError = "Supabase belum dikonfigurasi.")
                }
                return@launch
            }
            val userId = client.auth.currentSessionOrNull()?.user?.id ?: ""
            if (userId.isEmpty()) {
                _uiState.update {
                    it.copy(isLoadingIkigai = false, ikigaiError = "User not logged in")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoadingIkigai = true, ikigaiError = null) }

            ikigaiRepository.getAssessmentCount()
                .onSuccess { count ->
                    _uiState.update {
                        it.copy(
                            isLoadingIkigai = false,
                            ikigaiAssessmentCount = count,
                            ikigaiError = null,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingIkigai = false,
                            ikigaiError = e.message ?: "Failed to load Ikigai assessment count.",
                        )
                    }
                }
        }
    }

    fun onIkigaiErrorShown() {
        _uiState.update { it.copy(ikigaiError = null) }
    }

    /**
     * Placeholder Sleep Insight preview — real implementation generate teks
     * dari pola tidur 7 hari via Edge Function (FR-014) masuk T-005.
     *
     * Untuk sekarang, set null supaya HomeScreen render empty state
     * ("Insight belum tersedia, cek tidur 7 hari"). Fungsi ini tetap ada
     * agar pemanggil UI ready untuk T-005 (tidak perlu ubah HomeScreen lagi).
     */
    fun onLoadSleepInsightPreview() {
        _uiState.update {
            it.copy(
                isLoadingSleepInsight = false,
                sleepInsightPreview = null,
                sleepInsightError = null,
            )
        }
    }

    fun onSleepInsightErrorShown() {
        _uiState.update { it.copy(sleepInsightError = null) }
    }
}
