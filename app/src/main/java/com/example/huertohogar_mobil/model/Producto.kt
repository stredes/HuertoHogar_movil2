package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Modelo base de productos para HuertoHogar.
 * Representa un ítem del catálogo (fruta, verdura o producto natural).
 */
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey val id: String,
    val nombre: String,
    val precioCLP: Int,
    val unidad: String,
    val descripcion: String,
    val imagenResId: Int? = null  // Referencia a drawable local (R.drawable.*)
)
