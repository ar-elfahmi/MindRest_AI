package com.example.features.ikigai.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// INSERT body untuk tabel ikigai_assessments.
// Dipakai saat user submit 6 pertanyaan onboarding.
// Kolom q1-q4 wajib diisi, q5 chip string, q6 int 1-10.
// ---------------------------------------------------------------------------
@Serializable
data class IkigaiAssessmentInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("q1_passion") val q1Passion: String,
    @SerialName("q2_skill") val q2Skill: String,
    @SerialName("q3_profession") val q3Profession: String,
    @SerialName("q4_mission") val q4Mission: String,
    @SerialName("q5_overthinking") val q5Overthinking: String,
    @SerialName("q6_satisfaction") val q6Satisfaction: Int,
)

// ---------------------------------------------------------------------------
// Response row (untuk decode hasil insert().select() dan query baca).
// createdAt disimpan sebagai ISO 8601 string (lihat pola di SupabaseDtos.kt).
// ---------------------------------------------------------------------------
@Serializable
data class IkigaiAssessmentRow(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("q1_passion") val q1Passion: String? = null,
    @SerialName("q2_skill") val q2Skill: String? = null,
    @SerialName("q3_profession") val q3Profession: String? = null,
    @SerialName("q4_mission") val q4Mission: String? = null,
    @SerialName("q5_overthinking") val q5Overthinking: String? = null,
    @SerialName("q6_satisfaction") val q6Satisfaction: Int? = null,
    @SerialName("created_at") val createdAt: String,
)
