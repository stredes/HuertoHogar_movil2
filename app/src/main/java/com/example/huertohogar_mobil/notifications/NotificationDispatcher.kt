package com.example.huertohogar_mobil.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.huertohogar_mobil.R

class NotificationDispatcher(private val context: Context) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())

    private val pendingNotifications = mutableSetOf<String>()
    private var batchRunnable: Runnable? = null

    companion object {
        private const val TAG = "NotificationDispatcher"
        private const val PREFS_NAME = "notification_dispatcher"
        private const val KEY_LAST_NOTIFICATION_TIME = "last_notif_time"
        private const val BATCH_DELAY_MS = 1500L
        private const val MIN_INTERVAL_MS = 2000L
        private const val MAX_NOTIFICATIONS_PER_SESSION = 5
        private const val CHANNEL_ID = "sync_notifications"
        private const val NOTIFICATION_ID = 1001
    }

    fun enqueueNotification(key: String, title: String, message: String) {
        synchronized(pendingNotifications) {
            if (pendingNotifications.contains(key)) {
                Log.d(TAG, "Notificación duplicada, ignorada: $key")
                return
            }

            pendingNotifications.add(key)
            Log.d(TAG, "Notificación encolada: $key (total en cola: ${pendingNotifications.size})")

            batchRunnable?.let { handler.removeCallbacks(it) }

            batchRunnable = Runnable {
                flushNotifications(title)
            }
            handler.postDelayed(batchRunnable!!, BATCH_DELAY_MS)
        }
    }

    private fun flushNotifications(title: String) {
        synchronized(pendingNotifications) {
            if (pendingNotifications.isEmpty()) {
                Log.d(TAG, "No hay notificaciones pendientes")
                return
            }

            val lastNotifTime = sharedPrefs.getLong(KEY_LAST_NOTIFICATION_TIME, 0L)
            val now = System.currentTimeMillis()
            if (now - lastNotifTime < MIN_INTERVAL_MS) {
                Log.d(TAG, "Rate-limit activo. Reintentando en ${MIN_INTERVAL_MS - (now - lastNotifTime)}ms")
                batchRunnable = Runnable { flushNotifications(title) }
                handler.postDelayed(batchRunnable!!, MIN_INTERVAL_MS - (now - lastNotifTime))
                return
            }

            val countKey = "notif_count_${System.currentTimeMillis() / 60000}"
            val currentCount = sharedPrefs.getInt(countKey, 0)

            if (currentCount >= MAX_NOTIFICATIONS_PER_SESSION) {
                Log.w(TAG, "Límite máximo de notificaciones alcanzado ($MAX_NOTIFICATIONS_PER_SESSION)")
                pendingNotifications.clear()
                return
            }

            val count = pendingNotifications.size
            val summaryMessage = if (count == 1) {
                pendingNotifications.first()
            } else {
                "$count eventos sincronizados"
            }

            try {
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(summaryMessage)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(NOTIFICATION_ID, notification)

                Log.d(TAG, "Notificación enviada: $summaryMessage")

                sharedPrefs.edit()
                    .putLong(KEY_LAST_NOTIFICATION_TIME, now)
                    .putInt(countKey, currentCount + 1)
                    .apply()

                pendingNotifications.clear()
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificación", e)
            }
        }
    }

    fun cancelPending() {
        synchronized(pendingNotifications) {
            batchRunnable?.let { handler.removeCallbacks(it) }
            pendingNotifications.clear()
            Log.d(TAG, "Notificaciones pendientes canceladas")
        }
    }
}

