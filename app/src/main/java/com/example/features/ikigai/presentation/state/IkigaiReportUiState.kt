package com.example.features.ikigai.presentation.state

import com.example.features.ikigai.data.repository.IkigaiReport

/**
 * State untuk Ikigai Report Display (TASK 3.3).
 *
 * State machine sederhana:
 *   - [Initial] belum load apa-apa (auto-trigger init load)
 *   - [Loading] sedang fetch latest report
 *   - [Empty]   user belum punya report (tampilin CTA "Mulai Assessment")
 *   - [Generating] sedang trigger Edge Function (latency 3-5 detik)
 *   - [Loaded]  report siap ditampilkan
 *   - [Error]   error (bukan rate limit)
 *   - [RateLimited] 429 dari Edge Function
 *
 * Properti turunan (computed) diturunkan via nullable `report` + flag,
 * bukan sealed class, supaya UI bisa render sebagian (mis. cache report
 * + banner "sedang refresh").
 */
data class IkigaiReportUiState(
    /** Apakah sedang load pertama kali (belum ada data sama sekali). */
    val isInitialLoading: Boolean = true,
    /** Apakah sedang regenerate via Edge Function. */
    val isGenerating: Boolean = false,
    /** Report yang sedang ditampilkan (null = belum ada / empty). */
    val report: IkigaiReport? = null,
    /** Error message (null = tidak ada error). */
    val errorMessage: String? = null,
    /** Set true setelah 429 dari server (UI disable Refresh). */
    val isRateLimited: Boolean = false,
    /** Pesan terakhir tentang toggle rekomendasi (untuk snackbar). */
    val toggleMessage: String? = null,
    /**
     * Flag agar screen bisa auto-trigger generate begitu sampai (mis.
     * user baru saja submit assessment). Di-reset setelah diproses.
     */
    val autoTriggerOnFirstLoad: Boolean = false,
) {
    /** Tentukan apa yang harus ditampilkan di tengah. */
    val showEmptyState: Boolean
        get() = !isInitialLoading && !isGenerating && report == null && !isRateLimited && errorMessage == null

    /** Sedang dalam fase loading (init atau regenerate). */
    val showLoading: Boolean
        get() = isInitialLoading || isGenerating
}
