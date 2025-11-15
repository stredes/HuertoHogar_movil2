package com.example.huertohogar_mobil.model

import java.util.Date

data class Pedido(
    val id: String,
    val clienteId: String,
    val fecha: Date,
    val total: Int
)
