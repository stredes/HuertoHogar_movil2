package com.example.huertohogar_mobil.network.mappers

import com.example.huertohogar_mobil.model.EstadoPedido
import com.example.huertohogar_mobil.model.ItemPedido
import com.example.huertohogar_mobil.model.Pedido
import com.example.huertohogar_mobil.network.dto.PedidoDto
import com.example.huertohogar_mobil.network.dto.ItemPedidoDto
import com.google.gson.Gson

/**
 * Mappers para convertir entre PedidoDto y Pedido
 */

private val gson = Gson()

/**
 * Convierte PedidoDto a Pedido (Entity)
 */
fun PedidoDto.toEntity(): Pedido {
    // Serializar items a JSON
    val detalleJson = gson.toJson(this.items)

    return Pedido(
        id = 0, // Room auto-genera
        pedidoId = this.pedidoId,
        compradorEmail = this.compradorEmail,
        proveedorEmail = this.proveedorEmail,
        detalleJson = detalleJson,
        totalCLP = this.totalCLP,
        estado = this.estado.uppercase(),
        fechaPedido = this.fechaPedido,
        metodoPago = this.metodoPago,
        datosTransferencia = this.datosTransferencia,
        direccionEntrega = this.direccionEntrega,
        fechaDespacho = this.fechaDespacho,
        fechaEntrega = this.fechaEntrega,
        compradorNombre = this.compradorNombre,
        ultimaActualizacion = this.ultimaActualizacion,
        motivoCancelacion = this.motivoCancelacion
    )
}

/**
 * Convierte lista de PedidoDto a lista de Pedido
 */
fun List<PedidoDto>.toEntityList(): List<Pedido> {
    return this.map { it.toEntity() }
}

/**
 * Convierte Pedido (Entity) a PedidoDto
 */
fun Pedido.toDto(): PedidoDto {
    // Deserializar items desde JSON
    val items = try {
        gson.fromJson(this.detalleJson, Array<ItemPedidoDto>::class.java).toList()
    } catch (e: Exception) {
        emptyList()
    }

    return PedidoDto(
        id = this.pedidoId,
        pedidoId = this.pedidoId,
        numeroOrden = this.pedidoId,
        compradorEmail = this.compradorEmail,
        compradorNombre = this.compradorNombre,
        compradorTelefono = null,
        proveedorEmail = this.proveedorEmail,
        proveedorNombre = null,
        items = items,
        subtotal = this.totalCLP,
        costoEnvio = 0,
        descuento = 0,
        totalCLP = this.totalCLP,
        estado = this.estado.lowercase(),
        metodoPago = this.metodoPago,
        datosTransferencia = this.datosTransferencia,
        direccionEntrega = this.direccionEntrega,
        fechaPedido = this.fechaPedido,
        fechaDespacho = this.fechaDespacho,
        fechaEntrega = this.fechaEntrega,
        ultimaActualizacion = this.ultimaActualizacion,
        motivoCancelacion = this.motivoCancelacion,
        tracking = emptyList(),
        notas = null
    )
}

