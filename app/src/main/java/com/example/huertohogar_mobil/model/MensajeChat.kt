package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mensajes_chat",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["remitenteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["destinatarioId"], onDelete = ForeignKey.CASCADE)
    ],
    // SOLUCIÓN: Agregamos índices para mejorar rendimiento
    indices = [Index(value = ["remitenteId"]), Index(value = ["destinatarioId"])]
)
data class MensajeChat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remitenteId: Int,
    val destinatarioId: Int,
    val contenido: String,
    val timestamp: Long = System.currentTimeMillis()
)
