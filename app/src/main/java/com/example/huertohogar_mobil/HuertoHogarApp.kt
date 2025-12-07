package com.example.huertohogar_mobil

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Debug
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HuertoHogarApp : Application() {

    override fun onCreate() {
        // Chequeo anti-depuración
        if (Debug.isDebuggerConnected()) {
            android.os.Process.killProcess(android.os.Process.myPid())
        }
        
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Huerto Hogar Alertas"
            val descriptionText = "Canal para notificaciones administrativas"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("HUERTO_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            // Registrar el canal en el sistema
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
