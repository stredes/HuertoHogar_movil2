package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "producto")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // ID numérico para la base de datos
    val code: String, // Código único que usaremos en la lógica de la app
    val nombre: String,
    val precio: Int,
    val unidad: String,
    val stock: Int,
    val imagen: String,
    val descripcion: String,
    val origen: String,
    val categoria: String
)