package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val apellido: String,
    val email: String,
    val direccion: String
)
