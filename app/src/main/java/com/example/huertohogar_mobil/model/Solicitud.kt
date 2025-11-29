package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudes")
data class Solicitud(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val senderEmail: String,
    val receiverEmail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val estado: String = "PENDIENTE" // PENDIENTE, ACEPTADA, RECHAZADA
)
