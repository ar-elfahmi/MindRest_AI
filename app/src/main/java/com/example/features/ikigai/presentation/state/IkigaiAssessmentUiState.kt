package com.example.features.ikigai.presentation.state

/**
 * State untuk 6-step onboarding Ikigai assessment.
 *
 * - currentStep 0..5 (total 6 langkah).
 * - Step 0-3 = text pertanyaan (q1-q4, WAJIB diisi).
 * - Step 4   = chip pilihan overthinking (q5, WAJIB pilih 1).
 * - Step 5   = slider kepuasan hidup 1-10 (q6, default 5).
 *
 * Tombol "Lanjut" disable sampai langkah saat ini valid.
 * Tombol "Generate Laporan" cuma muncul di step 5 dan aktif bila
 * seluruh 6 field valid (lihat [canSave]).
 */
data class IkigaiAssessmentUiState(
    val currentStep: Int = 0,
    val q1Passion: String = "",
    val q2Skill: String = "",
    val q3Profession: String = "",
    val q4Mission: String = "",
    val q5Overthinking: String? = null,
    val q6Satisfaction: Int = 5,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    /** Diset setelah insert berhasil; trigger navigasi ke loading screen. */
    val savedAssessmentId: String? = null,
) {
    val totalSteps: Int = 6

    fun isCurrentStepValid(): Boolean = when (currentStep) {
        0 -> q1Passion.trim().isNotEmpty()
        1 -> q2Skill.trim().isNotEmpty()
        2 -> q3Profession.trim().isNotEmpty()
        3 -> q4Mission.trim().isNotEmpty()
        4 -> q5Overthinking != null
        5 -> q6Satisfaction in 1..10
        else -> false
    }

    /** True jika SEMUA 6 langkah sudah valid → tombol save boleh aktif. */
    fun canSave(): Boolean =
        q1Passion.trim().isNotEmpty() &&
            q2Skill.trim().isNotEmpty() &&
            q3Profession.trim().isNotEmpty() &&
            q4Mission.trim().isNotEmpty() &&
            q5Overthinking != null &&
            q6Satisfaction in 1..10
}
