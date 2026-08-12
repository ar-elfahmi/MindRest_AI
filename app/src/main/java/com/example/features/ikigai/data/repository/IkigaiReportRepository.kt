package com.example.features.ikigai.data.repository

import com.example.core.network.SupabaseClient
import com.example.features.ikigai.data.dto.IkigaiCircles
import com.example.features.ikigai.data.dto.IkigaiRecommendation
import com.example.features.ikigai.data.dto.IkigaiReportResponse
import com.example.features.ikigai.data.dto.IkigaiReportRow
import com.example.features.ikigai.data.dto.IkigaiReportUpdate
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Domain model hasil decode lengkap. UI/Render pakai ini saja,
 * bukan DTO mentah (supaya mapping JSONB → typed terisolasi).
 */
data class IkigaiReport(
    val id: String,
    val assessmentId: String,
    val reportMarkdown: String,
    val circles: IkigaiCircles,
    val recommendations: List<IkigaiRecommendation>,
    val version: Int,
    val generatedAt: String,
)

/**
 * Exception spesifik untuk menandai rate-limit (HTTP 429) dari Edge Function.
 * UI pakai ini untuk disable tombol Refresh + tampilkan snackbar "Coba besok".
 */
class IkigaiRateLimitedException(message: String = "Rate limited") : Exception(message)

/**
 * Error umum dari Edge Function. `httpStatus` null bila bukan HTTP error
 * (mis. network / decoding).
 */
class IkigaiEdgeFunctionException(
    val httpStatus: Int?,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

interface IkigaiReportRepository {

    /**
     * Ambil report terbaru milik user (kalau ada).
     * Return null kalau belum ada report (empty state).
     */
    suspend fun getLatestReport(): Result<IkigaiReport?>

    /**
     * Trigger regeneration via Edge Function `generate-ikigai-report`.
     *
     * Edge Function (server, service role) yang INSERT ke ikigai_reports.
     * Client TIDAK insert — setelah response, client GET latest row.
     * Expected latency 3-5 detik (lihat ROADMAP).
     */
    suspend fun triggerGenerate(): Result<IkigaiReport>

    /**
     * Toggle satu rekomendasi. UPDATE kolom `recommendations` (JSONB)
     * dengan list baru di mana item[recId].done = [done].
     * Return row ter-update supaya caller bisa sync state.
     */
    suspend fun toggleRecommendation(
        reportId: String,
        recId: String,
        done: Boolean,
    ): Result<IkigaiReport>
}

class IkigaiReportRepositoryImpl : IkigaiReportRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // -------------------- READ --------------------

    override suspend fun getLatestReport(): Result<IkigaiReport?> {
        return try {
            val userId = currentUserId() ?: return Result.success(null)
            val rows = SupabaseClient.requireClient()
                .postgrest["ikigai_reports"]
                .select {
                    filter { eq("user_id", userId) }
                    order("generated_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeList<IkigaiReportRow>()
            Result.success(rows.firstOrNull()?.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------- TRIGGER (Edge Function) --------------------

    override suspend fun triggerGenerate(): Result<IkigaiReport> = callEdgeFunction()

    /**
     * Edge Function `generate-ikigai-report` sudah INSERT row baru di server
     * (service role, bypass RLS). Client hanya GET latest row untuk dapat
     * id + generated_at yang benar. Expected latency 3-5 detik (ROADMAP).
     */
    private suspend fun callEdgeFunction(): Result<IkigaiReport> {
        return try {
            val client = SupabaseClient.requireClient()
            // supabase-kt otomatis menyertakan Authorization: Bearer <jwt> dari
            // session aktif. Jangan tambah manual — Ktor akan merge dua header
            // jadi 'Bearer a, Bearer b' dan EF akan reject dengan invalid_jwt.
            // Lihat T-010 untuk root-cause analysis.
            client.auth.currentSessionOrNull()
                ?: return Result.failure(IllegalStateException("User not logged in"))

            val response = client.functions.invoke(
                function = "generate-ikigai-report",
                body = emptyMap<String, String>(),
            )

            if (response.status.value == 429) {
                return Result.failure(IkigaiRateLimitedException())
            }
            if (response.status.value !in 200..299) {
                return Result.failure(
                    IkigaiEdgeFunctionException(
                        httpStatus = response.status.value,
                        message = "Edge Function error: ${response.status.value}",
                    )
                )
            }

            val bodyText = response.bodyAsText()
            val parsed = json.decodeFromString(IkigaiReportResponse.serializer(), bodyText)

            // Edge Function sudah INSERT row baru (service role, bypass RLS).
            // Client GET latest untuk dapat id + generated_at resmi dari DB.
            val latest = getLatestReport().getOrNull()
            if (latest != null) return Result.success(latest)

            // Fallback: EF response tanpa INSERT (mis. bug di EF) — pakai response
            // langsung. generatedAt kosong karena client tidak tahu timestamp server.
            val fallback = IkigaiReport(
                id = "",
                assessmentId = "",
                reportMarkdown = parsed.reportMarkdown,
                circles = json.decodeFromJsonElement(IkigaiCircles.serializer(), parsed.ikigaiCircles),
                recommendations = decodeRecommendations(parsed.recommendations),
                version = 1,
                generatedAt = "",
            )
            Result.success(fallback)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Tangkap HTTPException dari Ktor (status non-2xx)
            if (e.message?.contains("429") == true) {
                Result.failure(IkigaiRateLimitedException())
            } else {
                Result.failure(e)
            }
        }
    }

    // -------------------- UPDATE (toggle checkbox) --------------------

    override suspend fun toggleRecommendation(
        reportId: String,
        recId: String,
        done: Boolean,
    ): Result<IkigaiReport> {
        return try {
            // Ambil row dulu untuk dapat list recommendations terbaru
            val current = SupabaseClient.requireClient()
                .postgrest["ikigai_reports"]
                .select {
                    filter { eq("id", reportId) }
                    limit(1)
                }
                .decodeSingle<IkigaiReportRow>()

            val updatedList = decodeRecommendations(current.recommendations).map { rec ->
                if (rec.id == recId) rec.copy(done = done) else rec
            }
            val updatedJson = json.encodeToJsonElement(
                kotlinx.serialization.builtins.ListSerializer(IkigaiRecommendation.serializer()),
                updatedList
            )

            val patched = SupabaseClient.requireClient()
                .postgrest["ikigai_reports"]
                .update(
                    IkigaiReportUpdate(recommendations = updatedJson)
                ) {
                    filter { eq("id", reportId) }
                    select()
                }
                .decodeSingle<IkigaiReportRow>()

            Result.success(patched.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------- helpers --------------------

    private suspend fun currentUserId(): String? {
        val client = SupabaseClient.client ?: return null
        return client.auth.currentSessionOrNull()?.user?.id
    }

    private fun decodeRecommendations(element: kotlinx.serialization.json.JsonElement): List<IkigaiRecommendation> {
        return try {
            json.decodeFromJsonElement(
                kotlinx.serialization.builtins.ListSerializer(IkigaiRecommendation.serializer()),
                element
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// ---------------------------------------------------------------------------
// Mappers & mock data
// ---------------------------------------------------------------------------

private val mapperJson = Json { ignoreUnknownKeys = true }

private fun IkigaiReportRow.toDomain(): IkigaiReport {
    val circlesJson = runCatching {
        mapperJson.decodeFromJsonElement(IkigaiCircles.serializer(), ikigaiCircles)
    }.getOrElse { IkigaiCircles() }

    val recsJson = runCatching {
        mapperJson.decodeFromJsonElement(
                kotlinx.serialization.builtins.ListSerializer(IkigaiRecommendation.serializer()),
                recommendations
            )
    }.getOrElse { emptyList() }

    return IkigaiReport(
        id = id,
        assessmentId = assessmentId,
        reportMarkdown = reportMarkdown,
        circles = circlesJson,
        recommendations = recsJson,
        version = version,
        generatedAt = generatedAt,
    )
}

// Catatan: mock data (MOCK_RECOMMENDATIONS, MOCK_MARKDOWN) dihapus saat fix
// bug 22007. Mock INSERT dari client melanggar kontrak arsitektur (INSERT
// hanya via Edge Function service role) dan mencemari DB. Real path
// (callEdgeFunction) sudah benar: EF insert, client GET latest.
