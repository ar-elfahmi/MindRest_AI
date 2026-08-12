package com.example.features.home.presentation.state

/**
 * State untuk HomeScreen — dipakai [com.example.features.home.presentation.viewmodel.HomeViewModel].
 *
 * T-004 (FR-015 Dashboard) menambah 2 field:
 * - [ikigaiAssessmentCount] — jumlah assessment Ikigai user, sumber tunggal
 *   untuk empty-state CTA "Mulai Ikigai Assessment" atau filled-state
 *   "Lihat Laporan" + badge "X assessment".
 * - [sleepInsightPreview] — preview teks insight tidur. Null = belum ada
 *   (real Gemini-generated preview menyusul di T-005 / FR-014).
 *
 * Field lama (mood / sleep score) tetap di MoodUiState / SleepUiState
 * masing-masing — task ini tidak menggabungkan aggregation lintas fitur,
 * hanya menambahkan 2 widget baru yang belum ada.
 */
data class HomeUiState(
    val ikigaiAssessmentCount: Int = 0,
    val isLoadingIkigai: Boolean = false,
    val ikigaiError: String? = null,
    val sleepInsightPreview: String? = null,
    val isLoadingSleepInsight: Boolean = false,
    val sleepInsightError: String? = null,
) {
    /** True jika user sudah pernah submit minimal 1 assessment Ikigai. */
    val hasIkigaiAssessment: Boolean get() = ikigaiAssessmentCount > 0
}
