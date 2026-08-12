package com.example.features.lifestyle.presentation.state

import com.example.features.lifestyle.data.dto.SleepInsightData

/**
 * State untuk LifestyleScreen — fokus pada Sleep Insight section (T-005 / FR-014).
 *
 * State lain di LifestyleScreen (goals, streak) tetap lokal di-Compose
 * karena masih UI-only / belum ada backend binding.
 *
 * State machine untuk insight:
 *   - [Idle]            : belum ada insight + belum trigger
 *   - [Loading]         : sedang generate via Edge Function
 *   - [Loaded]          : insight siap ditampilkan
 *   - [Error]           : error (bukan no_sleep_logs)
 *   - [EmptyLogs]       : user belum punya log tidur (server 404)
 *
 * Properti turunan (computed) diturunkan via nullable `insight` + flag,
 * bukan sealed class, supaya UI bisa render sebagian (mis. cache insight
 * + banner "sedang refresh").
 */
data class LifestyleUiState(
    /** Apakah sedang generate via Edge Function. */
    val isGeneratingInsight: Boolean = false,
    /** Insight yang sedang ditampilkan (null = belum ada / empty). */
    val insight: SleepInsightData? = null,
    /** Error message (null = tidak ada error). */
    val errorMessage: String? = null,
    /** Pesan singkat tentang log kosong (null = tidak ada masalah). */
    val emptyLogsMessage: String? = null,
    /** Pesan sukses / info singkat (untuk snackbar). */
    val infoMessage: String? = null,
) {
    /** Tentukan apakah section insight harus tampilkan "empty state CTA". */
    val showEmptyState: Boolean
        get() = !isGeneratingInsight && insight == null &&
                errorMessage == null && emptyLogsMessage == null

    /** Tentukan apakah section insight harus tampilkan "no logs" CTA. */
    val showEmptyLogs: Boolean
        get() = !isGeneratingInsight && insight == null && emptyLogsMessage != null
}
