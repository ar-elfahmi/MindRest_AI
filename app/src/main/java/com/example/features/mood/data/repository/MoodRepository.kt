package com.example.features.mood.data.repository

import com.example.core.network.SupabaseClient
import com.example.core.network.dto.MoodLogInsert
import com.example.core.network.dto.MoodLogRow
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.time.ZoneId

/**
 * Hasil rata-rata mood untuk 1 hari dalam seminggu (Mon..Sun).
 *
 * @param dayOfWeekIndex 0=Monday, 6=Sunday (urutan chart).
 * @param averageScore nilai 1.0–5.0 (null jika tidak ada data pada hari itu).
 * @param entryCount jumlah log yang masuk ke rata-rata tersebut.
 */
data class DailyMoodAverage(
    val dayOfWeekIndex: Int,
    val averageScore: Double?,
    val entryCount: Int,
)

interface MoodRepository {
    suspend fun insertMoodLog(log: MoodLogInsert): Result<Unit>

    /**
     * Ambil log mood milik [userId] terbaru, diurutkan dari yang paling baru.
     *
     * @param limit jumlah maksimum baris yang dikembalikan (default 50).
     */
    suspend fun getMoodLogs(userId: String, limit: Long = 50): Result<List<MoodLogRow>>

    /**
     * Ambil skor mood TERBARU milik [userId] pada hari ini (device-local timezone).
     * Return null jika belum ada check-in hari ini. Sumber kebenaran untuk
     * logika "sudah check-in hari ini" di HomeScreen — survive restart & tab switch.
     */
    suspend fun getTodayMoodScore(userId: String): Result<Int?>

    /**
     * Ambil rata-rata mood harian untuk [days] hari terakhir (default 7).
     * Hari tanpa data = [DailyMoodAverage.averageScore] = null.
     * Urutan hasil: [Monday, Tuesday, ..., Sunday] (7 elemen, index 0..6).
     */
    suspend fun getDailyMoodAverages(
        userId: String,
        days: Int = 7,
    ): Result<List<DailyMoodAverage>>
}

class MoodRepositoryImpl : MoodRepository {
    override suspend fun insertMoodLog(log: MoodLogInsert): Result<Unit> {
        return try {
            SupabaseClient.requireClient().postgrest["mood_logs"].insert(log)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMoodLogs(userId: String, limit: Long): Result<List<MoodLogRow>> {
        return try {
            val logs = SupabaseClient.requireClient().postgrest["mood_logs"]
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(limit)
                }
                .decodeList<MoodLogRow>()
            Result.success(logs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTodayMoodScore(userId: String): Result<Int?> {
        return try {
            // Hari ini dalam device-local timezone, lalu konversi ke UTC ISO instant
            // agar kompatibel dengan kolom `created_at` bertipe timestamptz.
            val todayStart = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toString()
            val logs = SupabaseClient.requireClient().postgrest["mood_logs"].select {
                filter {
                    eq("user_id", userId)
                    gte("created_at", todayStart)
                }
                order("created_at", Order.DESCENDING)
                limit(1)
            }.decodeList<MoodLogRow>()
            Result.success(logs.firstOrNull()?.moodScore)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDailyMoodAverages(
        userId: String,
        days: Int,
    ): Result<List<DailyMoodAverage>> {
        return try {
            // Cutoff timestamp dalam ISO 8601 (UTC). Format OffsetDateTime.toString()
            // sudah kompatibel dengan kolom `created_at` bertipe timestamptz di Postgres.
            val cutoff = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)
                .minusDays(days.toLong())
                .toString()

            val logs = SupabaseClient.requireClient().postgrest["mood_logs"].select {
                filter {
                    eq("user_id", userId)
                    gte("created_at", cutoff)
                }
            }.decodeList<MoodLogRow>()

            // Java DayOfWeek: MONDAY=1 .. SUNDAY=7. Konversi ke 0-based Senin.
            val grouped = logs.groupBy { row ->
                java.time.OffsetDateTime.parse(row.createdAt)
                    .toZonedDateTime()
                    .dayOfWeek
                    .value - 1 // 0..6
            }

            val result = (0..6).map { dayIdx ->
                val dayLogs = grouped[dayIdx].orEmpty()
                val avg = if (dayLogs.isEmpty()) {
                    null
                } else {
                    dayLogs.map { it.moodScore.toDouble() }.average()
                }
                DailyMoodAverage(
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
