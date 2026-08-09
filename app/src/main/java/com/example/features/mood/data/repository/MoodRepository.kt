package com.example.features.mood.data.repository

import com.example.core.network.SupabaseClient
import com.example.core.network.dto.MoodLogInsert
import com.example.core.network.dto.MoodLogRow
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException

interface MoodRepository {
    suspend fun insertMoodLog(log: MoodLogInsert): Result<Unit>

    /**
     * Ambil log mood milik [userId] terbaru, diurutkan dari yang paling baru.
     *
     * @param limit jumlah maksimum baris yang dikembalikan (default 50).
     */
    suspend fun getMoodLogs(userId: String, limit: Long = 50): Result<List<MoodLogRow>>
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
}
