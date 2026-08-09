package com.example.features.sleep.data.repository

import com.example.core.network.SupabaseClient
import com.example.core.network.dto.SleepLogInsert
import com.example.core.network.dto.SleepLogRow
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException

interface SleepRepository {
    suspend fun insertSleepLog(log: SleepLogInsert): Result<Unit>

    /**
     * Ambil log tidur milik [userId] terbaru, diurutkan dari yang paling baru.
     */
    suspend fun getSleepLogs(userId: String, limit: Long = 50): Result<List<SleepLogRow>>
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
}
