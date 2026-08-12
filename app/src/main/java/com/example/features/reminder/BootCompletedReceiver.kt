package com.example.features.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.features.reminder.data.ReminderPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Receiver untuk [Intent.ACTION_BOOT_COMPLETED] dan
 * [Intent.ACTION_MY_PACKAGE_REPLACED].
 *
 * Setelah device reboot (atau update APK), semua [android.app.AlarmManager]
 * schedule di-clear oleh sistem. Receiver ini baca settings dari
 * [ReminderPreferencesRepository] (DataStore) → kalau `enabled=true`,
 * panggil [BedtimeNotificationHelper.scheduleBedtimeNotification] lagi
 * untuk re-arm alarm.
 *
 * Pattern: `runBlocking` dengan timeout 5 detik karena `BroadcastReceiver`
 * tidak boleh suspend function, dan sudah jadi pola standar untuk quick
 * DataStore read di receiver (lihat DataStore samples).
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = ReminderPreferencesRepository(context)
                // Snapshot read dengan timeout safety — kalau DataStore hang,
                // jangan block receiver selamanya.
                val settings = withTimeoutOrNull(5_000L) {
                    runBlocking { repository.read() }
                } ?: return@launch

                if (settings.enabled) {
                    BedtimeNotificationHelper.scheduleBedtimeNotification(
                        context = context,
                        bedtimeHour = settings.hour,
                        bedtimeMinute = settings.minute
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
