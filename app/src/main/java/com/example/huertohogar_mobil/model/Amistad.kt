package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "amistades",
    primaryKeys = ["usuarioId", "amigoId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["usuarioId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["amigoId"], onDelete = ForeignKey.CASCADE)
    ],
    // SOLUCIÓN: Agregamos índices para mejorar rendimiento
    indices = [Index(value = ["usuarioId"]), Index(value = ["amigoId"])]
)
data class Amistad(
    val usuarioId: Int, // Quién agrega
    val amigoId: Int    // A quién agrega
)
