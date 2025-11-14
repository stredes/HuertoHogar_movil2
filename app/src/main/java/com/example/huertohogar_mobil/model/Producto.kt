package com.example.huertohogar_mobil.model

import androidx.annotation.DrawableRes

data class Producto(
    val id: String,
    val nombre: String,
    val precio: Int,
    val formato: String,
    val descripcion: String,
    @DrawableRes val imagen: Int
)
