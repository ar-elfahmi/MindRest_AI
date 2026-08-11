package com.example.features.ikigai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.features.ikigai.data.repository.IkigaiRateLimitedException
import com.example.features.ikigai.data.repository.IkigaiReportRepository
import com.example.features.ikigai.data.repository.IkigaiReportRepositoryImpl
import com.example.features.ikigai.presentation.state.IkigaiReportUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk Ikigai Report Display (TASK 3.3).
 *
 * Flow:
 *   1. init → load latest report dari DB.
 *   2. Kalau user baru datang dari Assessment (autoTrigger = true) → panggil
 *      triggerGenerate() supaya Edge Function dipanggil otomatis.
 *   3. Tombol Refresh → triggerGenerate().
 *   4. Checkbox toggle → toggleRecommendation(reportId, recId, done).
 *
 * State 429: disable tombol Refresh, tampilkan snackbar "Coba besok".
 */
class IkigaiReportViewModel(
    private val repository: IkigaiReportRepository = IkigaiReportRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(IkigaiReportUiState())
    val uiState: StateFlow<IkigaiReportUiState> = _uiState.asStateFlow()

    init {
        // Default: load otomatis saat ViewModel dibuat.
        onLoadReport(autoTrigger = false)
    }

    // -------------------- LOAD --------------------

    /**
     * Ambil report terbaru. Kalau [autoTrigger] true dan DB kosong,
     * otomatis panggil triggerGenerate().
     */
    fun onLoadReport(autoTrigger: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isInitialLoading = true,
                    errorMessage = null,
                    autoTriggerOnFirstLoad = autoTrigger,
                )
            }
            val result = repository.getLatestReport()
            _uiState.update { current ->
                if (result.isSuccess) {
                    val report = result.getOrNull()
                    val shouldAuto = autoTrigger && report == null
                    current.copy(
                        isInitialLoading = false,
                        report = report,
                        errorMessage = null,
                        autoTriggerOnFirstLoad = shouldAuto,
                    )
                } else {
                    current.copy(
                        isInitialLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal load report",
                    )
                }
            }
            // Auto-trigger setelah load (kalau empty + autoTrigger flag).
            if (_uiState.value.autoTriggerOnFirstLoad) {
                _uiState.update { it.copy(autoTriggerOnFirstLoad = false) }
                onGenerateReport()
            }
        }
    }

    /**
     * Variasi untuk "navigate dari Assessment" — set autoTrigger supaya
     * kalau DB ternyata sudah ada report (belum expired), tinggal pakai;
     * kalau belum ada, langsung generate.
     */
    fun onScreenEnteredFromAssessment() {
        onLoadReport(autoTrigger = true)
    }

    // -------------------- GENERATE --------------------

    fun onGenerateReport() {
        if (_uiState.value.isRateLimited) return // guard
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    errorMessage = null,
                )
            }
            val result = repository.triggerGenerate()
            _uiState.update { current ->
                when {
                    result.isSuccess -> {
                        val report = result.getOrNull()
                        current.copy(
                            isGenerating = false,
                            report = report,
                            isInitialLoading = false,
                            isRateLimited = false,
                            errorMessage = null,
                            toggleMessage = if (report != null) "Laporan berhasil dibuat 🎉" else null,
                        )
                    }
                    result.exceptionOrNull() is IkigaiRateLimitedException -> {
                        current.copy(
                            isGenerating = false,
                            isRateLimited = true,
                            errorMessage = "Coba lagi besok ya.",
                        )
                    }
                    else -> {
                        current.copy(
                            isGenerating = false,
                            errorMessage = result.exceptionOrNull()?.message
                                ?: "Gagal generate laporan. Coba lagi.",
                        )
                    }
                }
            }
        }
    }

    // -------------------- TOGGLE RECOMMENDATION --------------------

    fun onToggleRecommendation(recId: String, done: Boolean) {
        val report = _uiState.value.report ?: return
        viewModelScope.launch {
            // Optimistic update: ubah state dulu, rollback kalau gagal.
            val prevList = report.recommendations
            val optimisticList = prevList.map { if (it.id == recId) it.copy(done = done) else it }
            _uiState.update {
                it.copy(report = report.copy(recommendations = optimisticList))
            }

            val result = repository.toggleRecommendation(report.id, recId, done)
            _uiState.update { current ->
                if (result.isSuccess) {
                    val updated = result.getOrNull()
                    current.copy(
                        report = updated ?: current.report,
                        toggleMessage = if (done) "Ditandai selesai ✓" else "Tandai balik aktif",
                    )
                } else {
                    // rollback
                    current.copy(
                        report = report, // restore original
                        errorMessage = "Gagal simpan perubahan. Coba lagi.",
                    )
                }
            }
        }
    }

    // -------------------- MESSAGE HANDLERS --------------------

    fun onMessageShown() {
        _uiState.update { it.copy(errorMessage = null, toggleMessage = null) }
    }
}
