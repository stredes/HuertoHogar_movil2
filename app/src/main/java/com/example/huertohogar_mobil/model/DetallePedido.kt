package com.example.huertohogar_mobil.model

data class DetallePedido(
    val id: String,
    val pedidoId: String,
    val productoId: String,
    val cantidad: Int,
    val precioUnitario: Int
)
