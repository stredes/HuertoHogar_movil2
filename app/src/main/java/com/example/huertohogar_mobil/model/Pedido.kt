package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedidos")
data class Pedido(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pedidoId: String = "",
    val compradorEmail: String = "",
    val proveedorEmail: String = "",
    val detalleJson: String = "",
    val totalCLP: Int = 0,
    val estado: String = "PENDIENTE", // Cambiado a String para compatibilidad con Firestore
    val fechaPedido: Long = 0L,
    val metodoPago: String? = null,
    val datosTransferencia: String? = null,
    val direccionEntrega: String? = null,
    val fechaDespacho: Long? = null,
    val fechaEntrega: Long? = null,
    val compradorNombre: String? = null,
    val ultimaActualizacion: Long = 0L
) {
    constructor() : this(0, "", "", "", "", 0, "PENDIENTE", 0L, null, null, null, null, null, null, 0L)

    // Helper para obtener el estado como enum
    fun getEstadoEnum(): EstadoPedido {
        return try {
            EstadoPedido.valueOf(estado)
        } catch (e: Exception) {
            EstadoPedido.PENDIENTE
        }
    }
}
