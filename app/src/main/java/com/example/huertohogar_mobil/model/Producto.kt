package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey val id: String = "",
    val nombre: String = "",
    val precioCLP: Int = 0,
    val unidad: String = "", // ej: "kg", "malla", "unid"
    val descripcion: String = "",
    val imagenRes: Int = 0, // ID de recurso drawable (para datos semilla)
    val imagenUri: String? = null, // Ruta URI si es imagen del usuario
    val providerEmail: String? = null // Email del proveedor (dueño del producto)
)
