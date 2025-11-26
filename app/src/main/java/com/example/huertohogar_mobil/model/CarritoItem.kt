package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrito_items")
data class CarritoItem(
    @PrimaryKey val productoId: String, // ID del producto (Foreign Key conceptual)
    val cantidad: Int
)
