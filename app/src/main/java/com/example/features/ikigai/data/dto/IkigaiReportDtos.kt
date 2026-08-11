package com.example.features.ikigai.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ---------------------------------------------------------------------------
// Row dari tabel `ikigai_reports` (Supabase Postgres).
//
// Pola decode sama dengan DTO lain di core/network/dto/SupabaseDtos.kt:
// `*_at` disimpan sebagai ISO 8601 string, decode ke LocalDateTime
// dilakukan di ViewModel kalau perlu.
// ---------------------------------------------------------------------------
@Serializable
data class IkigaiReportRow(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("assessment_id") val assessmentId: String,
    @SerialName("report_markdown") val reportMarkdown: String,
    @SerialName("ikigai_circles") val ikigaiCircles: JsonElement,
    @SerialName("recommendations") val recommendations: JsonElement,
    @SerialName("version") val version: Int = 1,
    @SerialName("generated_at") val generatedAt: String,
)

// ---------------------------------------------------------------------------
// Sub-DTOs untuk struktur JSONB di kolom `ikigai_circles` & `recommendations`.
// UI boleh pakai class ini langsung (lihat IkigaiReportDomain mapper).
// ---------------------------------------------------------------------------

/**
 * 4 lingkaran Ikigai – setiap field bisa TEXT (deskripsi singkat) atau
 * string panjang. Decoder fleksibel: payload AI mungkin mengembalikan
 * tipe lain, kita map manual di repository.
 */
@Serializable
data class IkigaiCircles(
    @SerialName("passion") val passion: String = "",
    @SerialName("skill") val skill: String = "",
    @SerialName("profession") val profession: String = "",
    @SerialName("mission") val mission: String = "",
)

/**
 * Satu rekomendasi aktivitas. `id` di-generate client-side kalau belum
 * datang dari server (untuk toggle UI yang konsisten sebelum persist).
 */
@Serializable
data class IkigaiRecommendation(
    @SerialName("id") val id: String,
    @SerialName("text") val text: String,
    @SerialName("done") val done: Boolean = false,
)

// ---------------------------------------------------------------------------
// Edge Function `generate-ikigai-report` request/response.
//
// Sesuai API CONTRACT di TASK 3.3:
//   Endpoint: POST https://<PROJECT_REF>.functions.supabase.co/generate-ikigai-report
//   Header : Authorization: Bearer <supabase_access_token>
//   Body   : (kosong – server ambil assessment dari DB)
//   Response: { report_markdown, ikigai_circles, recommendations }
//
// Kita decode langsung pakai IkigaiReportRow untuk konsistensi dengan
// struktur row DB (kolom JSONB di-decode ke JsonElement dulu, lalu
// di-mapping ke typed model di repository).
// ---------------------------------------------------------------------------

/**
 * Response body Edge Function. Sama dengan IkigaiReportRow tapi TANPA
 * kolom server-only (id, user_id, assessment_id, version, generated_at).
 * Edge Function juga bisa return kolom ini (untuk client yang ingin
 * langsung save tanpa query ulang) — saat ini tidak dipakai, tapi
 * deklarasi tersedia supaya decode fleksibel.
 */
@Serializable
data class IkigaiReportResponse(
    @SerialName("report_markdown") val reportMarkdown: String,
    @SerialName("ikigai_circles") val ikigaiCircles: JsonElement,
    @SerialName("recommendations") val recommendations: JsonElement,
)

/**
 * Body yang dikirim saat UPDATE kolom `recommendations` (toggle checkbox).
 * Client cuma boleh update kolom ini (RLS UPDATE full row, tapi field
 * lain kita kirim persis nilai lama supaya tidak terjadi perubahan).
 */
@Serializable
data class IkigaiReportUpdate(
    @SerialName("recommendations") val recommendations: JsonElement,
)
