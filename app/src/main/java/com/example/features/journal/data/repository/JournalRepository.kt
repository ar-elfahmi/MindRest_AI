package com.example.features.journal.data.repository

import com.example.core.network.SupabaseClient
import com.example.core.network.dto.JournalEntryInsert
import com.example.core.network.dto.JournalEntryRow
import com.example.features.journal.data.dto.ChatGeminiRequest
import com.example.features.journal.data.dto.ChatGeminiResponseData
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface JournalRepository {
    /**
     * Insert satu entry ke `journal_entries`.
     *
     * @param entry payload insert. Wajib isi userId + content. Field
     *   `sessionId` / `role` / `parentId` opsional (di-set saat chat history,
     *   null untuk legacy full-entry).
     */
    suspend fun insertJournalEntry(entry: JournalEntryInsert): Result<Unit>

    /**
     * Alias untuk konsistensi dengan task spec (T-003 step 3). Saat ini
     * identik dengan [insertJournalEntry] — nama method yang lebih eksplisit
     * untuk chat use case supaya caller tidak ambigu.
     */
    suspend fun saveJournalEntry(entry: JournalEntryInsert): Result<Unit>

    /**
     * Ambil entry jurnal milik [userId] terbaru, diurutkan dari yang paling baru.
     * Pakai untuk Journal History (FR-010).
     */
    suspend fun getJournalEntries(userId: String, limit: Long = 50): Result<List<JournalEntryRow>>

    /**
     * Ambil conversation history untuk satu sesi chat.
     *
     * Filter: `user_id = ? AND session_id = ? AND role IS NOT NULL`,
     * urut `created_at ASC` (kronologis). Hanya return pesan chat, bukan
     * legacy full-entry (role NULL).
     */
    suspend fun getConversationHistory(
        userId: String,
        sessionId: String,
        limit: Long = 100,
    ): Result<List<JournalEntryRow>>

    /**
     * Panggil Edge Function `chat-gemini` dengan [userMessage] + [sessionId].
     *
     * Server fetch history dari `journal_entries`, kirim ke Gemini dengan
     * system instruction CBT-style, dan return AI reply.
     *
     * Return:
     *   - Success: [ChatGeminiResponseData] (reply + metadata).
     *   - Failure: berisi [JournalEdgeFunctionException] untuk non-2xx HTTP,
     *     atau exception lain untuk error jaringan/decode.
     */
    suspend fun callChatGemini(
        sessionId: String?,
        userMessage: String,
    ): Result<ChatGeminiResponseData>
}

/**
 * Exception spesifik untuk error dari Edge Function `chat-gemini`.
 * `httpStatus` null untuk error jaringan/decode (bukan HTTP error).
 */
class JournalEdgeFunctionException(
    val httpStatus: Int?,
    val errorCode: String?,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class JournalRepositoryImpl : JournalRepository {

    private val json = Json { ignoreUnknownKeys = true }

    // -------------------- INSERT --------------------

    override suspend fun insertJournalEntry(entry: JournalEntryInsert): Result<Unit> = save(entry)

    override suspend fun saveJournalEntry(entry: JournalEntryInsert): Result<Unit> = save(entry)

    private suspend fun save(entry: JournalEntryInsert): Result<Unit> {
        return try {
            SupabaseClient.requireClient().postgrest["journal_entries"].insert(entry)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------- READ --------------------

    override suspend fun getJournalEntries(userId: String, limit: Long): Result<List<JournalEntryRow>> {
        return try {
            val logs = SupabaseClient.requireClient().postgrest["journal_entries"]
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(limit)
                }
                .decodeList<JournalEntryRow>()
            Result.success(logs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConversationHistory(
        userId: String,
        sessionId: String,
        limit: Long,
    ): Result<List<JournalEntryRow>> {
        return try {
            val rows = SupabaseClient.requireClient().postgrest["journal_entries"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("session_id", sessionId)
                        // Exclude legacy full-entry rows (role = NULL).
                        // PostgREST: `not.is.null` → SQL `IS NOT NULL`.
                        filterNot("role", FilterOperator.IS, null)
                    }
                    order("created_at", Order.ASCENDING)
                    limit(limit)
                }
                .decodeList<JournalEntryRow>()
            Result.success(rows)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------------------- EDGE FUNCTION --------------------

    override suspend fun callChatGemini(
        sessionId: String?,
        userMessage: String,
    ): Result<ChatGeminiResponseData> {
        return try {
            val client = SupabaseClient.requireClient()
            val accessToken = client.auth.currentSessionOrNull()?.accessToken
                ?: return Result.failure(IllegalStateException("User not logged in"))

            val response: HttpResponse = client.functions.invoke(
                function = "chat-gemini",
                body = ChatGeminiRequest(
                    message = userMessage,
                    sessionId = sessionId,
                ),
                headers = Headers.build {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                },
            )

            val rawBody = response.bodyAsText()

            if (response.status.value !in 200..299) {
                // Try to extract structured error from {ok:false, error:{code,message}}.
                val (code, msg) = parseEdgeError(rawBody)
                return Result.failure(
                    JournalEdgeFunctionException(
                        httpStatus = response.status.value,
                        errorCode = code,
                        message = msg ?: "Edge Function error: HTTP ${response.status.value}",
                    ),
                )
            }

            val parsed = parseOkResponse(rawBody)
                ?: return Result.failure(
                    JournalEdgeFunctionException(
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
                JournalEdgeFunctionException(
                    httpStatus = null,
                    errorCode = null,
                    message = e.message ?: "Gagal memanggil Edge Function.",
                    cause = e,
                ),
            )
        }
    }

    /**
     * Parse response sukses: {ok:true, data:{reply, model, session_id, ...}}.
     * Karena wrapper {ok,data,error} generik, kita decode manual untuk
     * ambil `data.reply` dkk — tidak perlu DTO wrapper (sesuai pola Edge
     * Function lain di repo ini).
     */
    private fun parseOkResponse(rawBody: String): ChatGeminiResponseData? {
        return try {
            val root = json.parseToJsonElement(rawBody).jsonObject
            val ok = root["ok"]?.jsonPrimitive?.content == "true"
            if (!ok) return null
            val dataEl = root["data"] as? JsonObject ?: return null
            val dataJson = dataEl.toString()
            json.decodeFromString(ChatGeminiResponseData.serializer(), dataJson)
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
