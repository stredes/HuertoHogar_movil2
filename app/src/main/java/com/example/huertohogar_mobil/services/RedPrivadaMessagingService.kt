package com.example.huertohogar_mobil.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.huertohogar_mobil.MainActivity
import com.example.huertohogar_mobil.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID

class RedPrivadaMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "RedPrivadaFCM"
        private const val MSG_CHANNEL_ID = "REDPRIVADA_MESSAGES_CHANNEL"
        private const val PREFS_FILE = "fcm_processed_notifications"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "📨 Mensaje FCM recibido desde: ${remoteMessage.from}")

        val data = remoteMessage.data
        val tipo = data["tipo"] ?: return
        val id = data["id"] ?: UUID.randomUUID().toString()

        if (esNotificacionYaProcesada(id)) {
            Log.d(TAG, "⊘ Notificación ya procesada, descartando: $id")
            return
        }

        marcarComoProcessada(id)

        when (tipo) {
            "SOLICITUD_AMISTAD" -> {
                val senderName = data["senderName"] ?: "Usuario"
                mostrarNotificacion(
                    "Solicitud de Amistad",
                    "$senderName quiere conectar",
                    id.hashCode()
                )
            }
            "SOLICITUD_ACEPTADA" -> {
                val senderName = data["senderName"] ?: "Usuario"
                mostrarNotificacion(
                    "Nuevo Amigo",
                    "$senderName aceptó tu solicitud",
                    id.hashCode()
                )
            }
            "NUEVO_MENSAJE" -> {
                val senderName = data["senderName"] ?: "Usuario"
                val contenido = data["contenido"] ?: "Nuevo mensaje"
                mostrarNotificacion(
                    "Mensaje de $senderName",
                    contenido,
                    id.hashCode()
                )
            }
            "NUEVO_PEDIDO" -> {
                val compradorNombre = data["compradorNombre"] ?: "Cliente"
                val total = data["total"] ?: "?"
                mostrarNotificacion(
                    "Nuevo Pedido 🛒",
                    "$compradorNombre realizó un pedido de $$total",
                    id.hashCode()
                )
            }
            "CAMBIO_PEDIDO" -> {
                val estado = data["estado"] ?: "desconocido"
                val detalles = when (estado) {
                    "CONFIRMADO" -> "Tu pedido ha sido confirmado. Selecciona tu método de pago."
                    "LISTO_DESPACHO" -> "Tu pedido está listo para despacho"
                    "EN_CAMINO" -> "Tu pedido está en camino"
                    "ENTREGADO" -> "Tu pedido ha sido entregado"
                    "PAGO_RECIBIDO" -> "Tu pago ha sido recibido. Procederemos con el despacho."
                    else -> "Estado: $estado"
                }
                mostrarNotificacion(
                    "Actualización de Pedido 📦",
                    detalles,
                    id.hashCode()
                )
            }
            "PAGO_CONFIRMADO" -> {
                val monto = data["monto"] ?: "?"
                mostrarNotificacion(
                    "Pago Confirmado 💳",
                    "Se confirmó el pago de $$monto",
                    id.hashCode()
                )
            }
            else -> {
                Log.d(TAG, "⊘ Tipo de notificación desconocido: $tipo")
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "🔄 Token FCM renovado: ${token.take(20)}...")
    }

    private fun esNotificacionYaProcesada(id: String): Boolean {
        val prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE)
        return prefs.getBoolean("notif_$id", false)
    }

    private fun marcarComoProcessada(id: String) {
        val prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE)
        prefs.edit {
            putBoolean("notif_$id", true)
        }
    }

    private fun mostrarNotificacion(titulo: String, contenido: String, id: Int) {
        try {
            crearCanalNotificacionSiNecesario()

            val context: Context = this
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, MSG_CHANNEL_ID)
                .setSmallIcon(R.drawable.icono)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVibrate(longArrayOf(0, 500))

            // Verificar permiso POST_NOTIFICATIONS en Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(context).notify(id, builder.build())
                }
            } else {
                NotificationManagerCompat.from(context).notify(id, builder.build())
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error mostrando notificación", e)
        }
    }

    private fun crearCanalNotificacionSiNecesario() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mensajes y Alertas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(MSG_CHANNEL_ID, name, importance).apply {
                description = "Notificaciones de Red Privada"
                enableVibration(true)
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
