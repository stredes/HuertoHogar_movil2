package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.ForeignKey

// Tabla simple que une dos usuarios como amigos
@Entity(
    tableName = "amistades",
    primaryKeys = ["usuarioId", "amigoId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["usuarioId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["amigoId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class Amistad(
    val usuarioId: Int, // Quién agrega
    val amigoId: Int    // A quién agrega
)
