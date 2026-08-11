package com.example.features.sleep.data.repository

import com.example.core.network.SupabaseClient
import com.example.core.network.dto.SleepLogInsert
import com.example.core.network.dto.SleepLogRow
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException

/**
 * Hasil rata-rata skor kualitas tidur untuk 1 hari dalam seminggu (Mon..Sun).
 *
 * @param dayOfWeekIndex 0=Monday, 6=Sunday (urutan chart).
 * @param averageScore nilai 0.0..1.0 (null jika tidak ada data pada hari itu).
 *                      POOR=0.25, FAIR=0.50, GOOD=0.75, EXCELLENT=1.00.
 * @param entryCount jumlah log yang masuk ke rata-rata tersebut.
 */
data class DailySleepScore(
    val dayOfWeekIndex: Int,
    val averageScore: Double?,
    val entryCount: Int,
)

/** Map string kualitas tidur (case-insensitive) ke skor 0.0..1.0. Return null jika unknown. */
private fun sleepQualityScore(q: String): Double? = when (q.uppercase()) {
    "POOR" -> 0.25
    "FAIR" -> 0.50
    "GOOD" -> 0.75
    "EXCELLENT" -> 1.00
    else -> null
}

interface SleepRepository {
    suspend fun insertSleepLog(log: SleepLogInsert): Result<Unit>

    /**
     * Ambil log tidur milik [userId] terbaru, diurutkan dari yang paling baru.
     */
    suspend fun getSleepLogs(userId: String, limit: Long = 50): Result<List<SleepLogRow>>

    /**
     * Ambil rata-rata skor kualitas tidur harian untuk [days] hari terakhir (default 7).
     * Hari tanpa data = [DailySleepScore.averageScore] = null.
     * Urutan hasil: [Monday, Tuesday, ..., Sunday] (7 elemen, index 0..6).
     */
    suspend fun getDailySleepScores(
        userId: String,
        days: Int = 7,
    ): Result<List<DailySleepScore>>
}

class SleepRepositoryImpl : SleepRepository {
    override suspend fun insertSleepLog(log: SleepLogInsert): Result<Unit> {
        return try {
            SupabaseClient.requireClient().postgrest["sleep_logs"].insert(log)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSleepLogs(userId: String, limit: Long): Result<List<SleepLogRow>> {
        return try {
            val logs = SupabaseClient.requireClient().postgrest["sleep_logs"]
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(limit)
                }
                .decodeList<SleepLogRow>()
            Result.success(logs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDailySleepScores(
        userId: String,
        days: Int,
    ): Result<List<DailySleepScore>> {
        return try {
            // Cutoff timestamp dalam ISO 8601 (UTC). Format OffsetDateTime.toString()
            // sudah kompatibel dengan kolom `created_at` bertipe timestamptz di Postgres.
            val cutoff = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)
                .minusDays(days.toLong())
                .toString()

            val logs = SupabaseClient.requireClient().postgrest["sleep_logs"].select {
                filter {
                    eq("user_id", userId)
                    gte("created_at", cutoff)
                }
            }.decodeList<SleepLogRow>()

            // Java DayOfWeek: MONDAY=1 .. SUNDAY=7. Konversi ke 0-based Senin.
            val grouped = logs.groupBy { row ->
                java.time.OffsetDateTime.parse(row.createdAt)
                    .toZonedDateTime()
                    .dayOfWeek
                    .value - 1 // 0..6
            }

            val result = (0..6).map { dayIdx ->
                val dayLogs = grouped[dayIdx].orEmpty()
                val scores = dayLogs.mapNotNull { sleepQualityScore(it.sleepQuality) }
                val avg = if (scores.isEmpty()) null else scores.average()
                DailySleepScore(
                    dayOfWeekIndex = dayIdx,
                    averageScore = avg,
                    entryCount = dayLogs.size,
                )
            }
            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
