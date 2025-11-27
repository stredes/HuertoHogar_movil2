package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mensajes_contacto")
data class MensajeContacto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val email: String,
    val mensaje: String,
    val fecha: String, // Guardaremos fecha como String simple por ahora
    val respondido: Boolean = false
)
