package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object EstadoMensaje {
    const val ENVIANDO = 0
    const val ENVIADO = 1
    const val ERROR = 2
    const val RECIBIDO = 3
    const val LEIDO = 4
}

// Tipo de contenido para multimedia
object TipoContenido {
    const val TEXTO = "TEXTO"
    const val IMAGEN = "IMAGEN"
    const val AUDIO = "AUDIO"
    const val VIDEO = "VIDEO"
    const val UBICACION = "UBICACION"
    const val ARCHIVO = "ARCHIVO"
}

@Entity(
    tableName = "mensajes_chat",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["remitenteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["destinatarioId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["remitenteId", "destinatarioId", "timestamp", "contenido"], unique = true)]
)
data class MensajeChat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remitenteId: Int,
    val destinatarioId: Int,
    val contenido: String, // Texto o URI del archivo
    val tipoContenido: String = TipoContenido.TEXTO, // Nuevo campo
    val timestamp: Long = System.currentTimeMillis(),
    val estado: Int = EstadoMensaje.ENVIADO
)
