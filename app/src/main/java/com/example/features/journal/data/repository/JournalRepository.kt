package com.example.features.journal.data.repository

import com.example.core.network.SupabaseClient
import com.example.core.network.dto.JournalEntryInsert
import com.example.core.network.dto.JournalEntryRow
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException

interface JournalRepository {
    suspend fun insertJournalEntry(entry: JournalEntryInsert): Result<Unit>

    /**
     * Ambil entry jurnal milik [userId] terbaru, diurutkan dari yang paling baru.
     */
    suspend fun getJournalEntries(userId: String, limit: Long = 50): Result<List<JournalEntryRow>>
}

class JournalRepositoryImpl : JournalRepository {
    override suspend fun insertJournalEntry(entry: JournalEntryInsert): Result<Unit> {
        return try {
            SupabaseClient.requireClient().postgrest["journal_entries"].insert(entry)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
}
