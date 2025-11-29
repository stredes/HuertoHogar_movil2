package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

object EstadoMensaje {
    const val ENVIANDO = 0
    const val ENVIADO = 1
    const val ERROR = 2
    const val RECIBIDO = 3
}

@Entity(
    tableName = "mensajes_chat",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["remitenteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["destinatarioId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class MensajeChat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remitenteId: Int,
    val destinatarioId: Int,
    val contenido: String,
    val timestamp: Long = System.currentTimeMillis(),
    val estado: Int = EstadoMensaje.ENVIADO // Por defecto enviado para compatibilidad
)
