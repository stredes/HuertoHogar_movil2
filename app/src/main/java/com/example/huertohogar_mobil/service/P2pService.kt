package com.example.huertohogar_mobil.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.huertohogar_mobil.R
import com.example.huertohogar_mobil.data.P2pManager
import com.example.huertohogar_mobil.data.FirebaseRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class P2pService : Service() {

    @Inject
    lateinit var p2pManager: P2pManager
    
    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val email = intent?.getStringExtra("USER_EMAIL")
        val action = intent?.action

        if (action == "STOP_SERVICE") {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            // p2pManager.pause()
            firebaseRepository.cleanup()
            return START_NOT_STICKY
        }

        if (email != null) {
            startForegroundServiceNotification()
            p2pManager.initialize(email)
            p2pManager.resume()
            firebaseRepository.initialize(email)
        }

        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = "P2P_SERVICE_CHANNEL"
        val channelName = "Huerto P2P Background Service"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Huerto Hogar Conectado")
            .setContentText("Servicio de chat P2P y Online activo")
            .setSmallIcon(R.drawable.icono)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        val notification = notificationBuilder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                 // 16 is FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                 startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
            } catch (e: Exception) {
                 startForeground(1, notification)
            }
        } else {
            startForeground(1, notification)
        }
    }
}
