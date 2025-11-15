package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "detalles_pedido",
    primaryKeys = ["pedidoId", "productoId"],
    foreignKeys = [
        ForeignKey(
            entity = Pedido::class,
            parentColumns = ["id"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DetallePedido(
    val pedidoId: String,
    val productoId: String,
    val cantidad: Int,
    val precioUnitario: Int
)
