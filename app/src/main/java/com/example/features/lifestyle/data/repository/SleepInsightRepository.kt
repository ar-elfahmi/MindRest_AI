package com.example.features.lifestyle.data.repository

import com.example.core.network.SupabaseClient
import com.example.features.lifestyle.data.dto.GenerateSleepInsightRequest
import com.example.features.lifestyle.data.dto.GenerateSleepInsightResponse
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Exception spesifik untuk error dari Edge Function `generate-sleep-insight`.
 * `httpStatus` null untuk error jaringan/decode (bukan HTTP error).
 */
class SleepInsightEdgeFunctionException(
    val httpStatus: Int?,
    val errorCode: String?,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Exception spesifik untuk kasus "user belum punya log tidur dalam window
 * analisis" — server return HTTP 404 dengan code `no_sleep_logs`. UI bisa
 * tangani dengan pesan khusus ("Tambah log tidur dulu").
 */
class NoSleepLogsException(message: String) : Exception(message)

interface SleepInsightRepository {

    /**
     * Trigger Edge Function `generate-sleep-insight` dengan [periodDays]
     * hari window analisis (default 7, max 30 — di-clamp server-side).
     *
     * Server (service role) yang INSERT ke `sleep_insights`. Client
     * langsung dapat row ter-enrich dari response (id + recommendations
     * sudah di-UUID).
     *
     * Return:
     *   - Success: [GenerateSleepInsightResponse] (insight + metadata).
     *   - Failure: [SleepInsightEdgeFunctionException] untuk HTTP non-2xx,
     *     [NoSleepLogsException] untuk 404 `no_sleep_logs`, atau exception
     *     lain untuk error jaringan/decode.
     */
    suspend fun generateInsight(periodDays: Int = 7): Result<GenerateSleepInsightResponse>
}

class SleepInsightRepositoryImpl : SleepInsightRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateInsight(periodDays: Int): Result<GenerateSleepInsightResponse> {
        return try {
            val client = SupabaseClient.requireClient()
            // supabase-kt otomatis menyertakan Authorization: Bearer <jwt> dari
            // session aktif. Jangan tambah manual — Ktor akan merge dua header
            // jadi 'Bearer a, Bearer b' dan EF akan reject dengan invalid_jwt.
            // Lihat T-010 untuk root-cause analysis.
            client.auth.currentSessionOrNull()
                ?: return Result.failure(IllegalStateException("User not logged in"))

            val response: HttpResponse = client.functions.invoke(
                function = "generate-sleep-insight",
                body = GenerateSleepInsightRequest(periodDays = periodDays),
            )

            val rawBody = response.bodyAsText()

            if (response.status.value == 404) {
                val (_, msg) = parseEdgeError(rawBody)
                if (msg?.contains("log tidur", ignoreCase = true) == true ||
                    msg?.contains("sleep", ignoreCase = true) == true
                ) {
                    // Smart cast: msg non-null di sini karena msg?.contains() == true.
                    return Result.failure(
                        NoSleepLogsException(
                            msg,
                        ),
                    )
                }
            }

            if (response.status.value !in 200..299) {
                val (code, msg) = parseEdgeError(rawBody)
                return Result.failure(
                    SleepInsightEdgeFunctionException(
                        httpStatus = response.status.value,
                        errorCode = code,
                        message = msg ?: "Edge Function error: HTTP ${response.status.value}",
                    ),
                )
            }

            val parsed = parseOkResponse(rawBody)
                ?: return Result.failure(
                    SleepInsightEdgeFunctionException(
                        httpStatus = response.status.value,
                        errorCode = "invalid_response",
                        message = "Edge Function response tidak sesuai contract.",
                    ),
                )
            Result.success(parsed)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Tangkap exception dari Ktor / decode / network.
            Result.failure(
                SleepInsightEdgeFunctionException(
                    httpStatus = null,
                    errorCode = null,
                    message = e.message ?: "Gagal memanggil Edge Function.",
                    cause = e,
                ),
            )
        }
    }

    /**
     * Parse response sukses: {ok:true, data:{insight, logs_analyzed, ...}}.
     * Karena wrapper {ok,data,error} generik, decode manual untuk ambil
     * `data` saja — pola sama dengan `JournalRepository.parseOkResponse`.
     */
    private fun parseOkResponse(rawBody: String): GenerateSleepInsightResponse? {
        return try {
            val root = json.parseToJsonElement(rawBody).jsonObject
            val ok = root["ok"]?.jsonPrimitive?.content == "true"
            if (!ok) return null
            val dataEl = root["data"] as? JsonObject ?: return null
            json.decodeFromString(
                GenerateSleepInsightResponse.serializer(),
                dataEl.toString(),
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract error code + message dari body response.
     * Return Pair<code, message> — salah satu bisa null kalau shape invalid.
     */
    private fun parseEdgeError(rawBody: String): Pair<String?, String?> {
        return try {
            val root = json.parseToJsonElement(rawBody).jsonObject
            val err = root["error"] as? JsonObject
            val code = err?.get("code")?.jsonPrimitive?.content
            val msg = err?.get("message")?.jsonPrimitive?.content
            code to msg
        } catch (e: Exception) {
            null to null
        }
    }
}
