// Modelo de datos Producto
package com.example.huertohogar_mobil.data

/**
 * Modelo base de productos para HuertoHogar.
 * Representa un ítem del catálogo (fruta, verdura o producto natural).
 */
data class Producto(
    val id: String,
    val nombre: String,
    val precioCLP: Int,
    val unidad: String,
    val descripcion: String,
    val imagenResId: Int? = null  // Referencia a drawable local (R.drawable.*)
)
