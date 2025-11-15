package com.example.huertohogar_mobil.model

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Modelo de datos unificado para un Producto.
 * Representa un ítem del catálogo y es una entidad de la base de datos Room.
 */
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val precio: Int,
    val formato: String,
    val descripcion: String,
    @DrawableRes val imagen: Int? = null
)
