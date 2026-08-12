package com.example.features.lifestyle.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =============================================================================
// DTOs — Sleep Insight (T-005 / FR-014)
// =============================================================================
// Kontrak antara Android client dan Edge Function `generate-sleep-insight`.
// Skema HARUS match dengan output contract di
// `supabase/functions/generate-sleep-insight/index.ts`.
// =============================================================================


/**
 * Request body untuk Edge Function `generate-sleep-insight`.
 *
 * Body kosong `{}` = default 7 hari. Set [periodDays] untuk window
 * analisis lain (max 30 — di-clamp server-side).
 */
@Serializable
data class GenerateSleepInsightRequest(
    @SerialName("period_days") val periodDays: Int? = null,
)


/**
 * Response sukses dari Edge Function `generate-sleep-insight`.
 *
 * `data` berisi:
 *   - insight         : insight ter-enrich (id + recommendations sudah di-UUID)
 *   - logs_analyzed   : jumlah log tidur yang dipakai untuk analisis
 *   - latency_ms      : waktu eksekusi server
 *   - usage           : token usage (opsional)
 */
@Serializable
data class GenerateSleepInsightResponse(
    @SerialName("insight") val insight: SleepInsightData,
    @SerialName("logs_analyzed") val logsAnalyzed: Int = 0,
    @SerialName("latency_ms") val latencyMs: Int = 0,
    @SerialName("usage") val usage: SleepInsightUsage? = null,
)

/** Output insight lengkap (seperti row `sleep_insights` di DB). */
@Serializable
data class SleepInsightData(
    @SerialName("id") val id: String,
    @SerialName("summary") val summary: String,
    @SerialName("recommendations") val recommendations: SleepInsightRecommendations,
    @SerialName("period_days") val periodDays: Int,
    @SerialName("generated_at") val generatedAt: String,
)

/** Container 3 list rekomendasi (activities / foods / music). */
@Serializable
data class SleepInsightRecommendations(
    @SerialName("activities") val activities: List<SleepInsightItem> = emptyList(),
    @SerialName("foods") val foods: List<SleepInsightItem> = emptyList(),
    @SerialName("music") val music: List<SleepInsightItem> = emptyList(),
)

/** Item rekomendasi tunggal: text + id (UUID dari server). */
@Serializable
data class SleepInsightItem(
    @SerialName("id") val id: String,
    @SerialName("text") val text: String,
)

/** Token usage (opsional, kalau SDK Gemini expose). */
@Serializable
data class SleepInsightUsage(
    @SerialName("promptTokenCount") val promptTokenCount: Int = 0,
    @SerialName("candidatesTokenCount") val candidatesTokenCount: Int = 0,
    @SerialName("totalTokenCount") val totalTokenCount: Int = 0,
)
