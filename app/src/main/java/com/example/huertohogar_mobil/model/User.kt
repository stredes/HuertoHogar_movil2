package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String // En producción usar hash real, aquí texto simple por ahora
)
