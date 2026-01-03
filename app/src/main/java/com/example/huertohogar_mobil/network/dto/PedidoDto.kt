package com.example.huertohogar_mobil.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para Pedidos
 */

/**
 * Pedido completo del servidor
 */
data class PedidoDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("pedidoId")
    val pedidoId: String,

    @SerializedName("numeroOrden")
    val numeroOrden: String,

    @SerializedName("compradorEmail")
    val compradorEmail: String,

    @SerializedName("compradorNombre")
    val compradorNombre: String?,

    @SerializedName("compradorTelefono")
    val compradorTelefono: String?,

    @SerializedName("proveedorEmail")
    val proveedorEmail: String,

    @SerializedName("proveedorNombre")
    val proveedorNombre: String?,

    @SerializedName("items")
    val items: List<ItemPedidoDto>,

    @SerializedName("subtotal")
    val subtotal: Int,

    @SerializedName("costoEnvio")
    val costoEnvio: Int = 0,

    @SerializedName("descuento")
    val descuento: Int = 0,

    @SerializedName("totalCLP")
    val totalCLP: Int,

    @SerializedName("estado")
    val estado: String, // PENDIENTE, CONFIRMADO, PAGADO, LISTO_DESPACHO, EN_CAMINO, ENTREGADO, CANCELADO

    @SerializedName("metodoPago")
    val metodoPago: String?,

    @SerializedName("datosTransferencia")
    val datosTransferencia: String?,

    @SerializedName("direccionEntrega")
    val direccionEntrega: String?,

    @SerializedName("fechaPedido")
    val fechaPedido: Long,

    @SerializedName("fechaDespacho")
    val fechaDespacho: Long?,

    @SerializedName("fechaEntrega")
    val fechaEntrega: Long?,

    @SerializedName("ultimaActualizacion")
    val ultimaActualizacion: Long,

    @SerializedName("motivoCancelacion")
    val motivoCancelacion: String?,

    @SerializedName("tracking")
    val tracking: List<TrackingDto> = emptyList(),

    @SerializedName("notas")
    val notas: String?
)

/**
 * Item de un pedido
 */
data class ItemPedidoDto(
    @SerializedName("productoId")
    val productoId: String,

    @SerializedName("productoNombre")
    val productoNombre: String,

    @SerializedName("productoImagenUrl")
    val productoImagenUrl: String? = null,

    @SerializedName("cantidad")
    val cantidad: Int,

    @SerializedName("precioUnitario")
    val precioUnitario: Int,

    @SerializedName("precioUnitarioCLP")
    val precioUnitarioCLP: Int = precioUnitario, // Alias

    @SerializedName("subtotal")
    val subtotal: Int,

    @SerializedName("subtotalCLP")
    val subtotalCLP: Int = subtotal, // Alias

    @SerializedName("unidad")
    val unidad: String = "kg",

    @SerializedName("proveedorEmail")
    val proveedorEmail: String? = null
)

/**
 * Información de seguimiento
 */
data class TrackingDto(
    @SerializedName("estado")
    val estado: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("fecha")
    val fecha: Long,

    @SerializedName("ubicacion")
    val ubicacion: String?
)

/**
 * Request para crear pedido
 */
data class CrearPedidoRequest(
    @SerializedName("proveedorEmail")
    val proveedorEmail: String,

    @SerializedName("items")
    val items: List<ItemPedidoRequest>,

    @SerializedName("direccionEntrega")
    val direccionEntrega: String?,

    @SerializedName("notas")
    val notas: String?
)

/**
 * Item para crear pedido
 */
data class ItemPedidoRequest(
    @SerializedName("productoId")
    val productoId: String,

    @SerializedName("cantidad")
    val cantidad: Int
)

/**
 * Request para actualizar estado
 */
data class ActualizarEstadoRequest(
    @SerializedName("estado")
    val estado: String,

    @SerializedName("notas")
    val notas: String?
)

/**
 * Request para método de pago
 */
data class MetodoPagoRequest(
    @SerializedName("metodoPago")
    val metodoPago: String, // transferencia, efectivo, mercadopago, etc.

    @SerializedName("datosTransferencia")
    val datosTransferencia: String?
)

/**
 * Request para cancelar pedido
 */
data class CancelarPedidoRequest(
    @SerializedName("motivo")
    val motivo: String
)

/**
 * Respuesta de contadores
 */
data class ContadoresResponse(
    @SerializedName("pendientes")
    val pendientes: Int = 0,

    @SerializedName("confirmados")
    val confirmados: Int = 0,

    @SerializedName("enCamino")
    val enCamino: Int = 0,

    @SerializedName("total")
    val total: Int = 0
)

/**
 * Respuesta simple de contador
 */
data class CountResponse(
    @SerializedName("count")
    val count: Int = 0
)

/**
 * Notificación de pedido
 */
data class NotificacionPedidoDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("pedidoId")
    val pedidoId: String,

    @SerializedName("usuarioEmail")
    val usuarioEmail: String,

    @SerializedName("tipo")
    val tipo: String, // nuevo_pedido, cambio_estado, mensaje, etc.

    @SerializedName("titulo")
    val titulo: String,

    @SerializedName("mensaje")
    val mensaje: String,

    @SerializedName("leida")
    val leida: Boolean = false,

    @SerializedName("fechaCreacion")
    val fechaCreacion: Long,

    @SerializedName("metadata")
    val metadata: Map<String, Any>? = null
)

/**
 * Respuesta paginada de pedidos
 */
data class PedidosPaginadosResponse(
    @SerializedName("pedidos")
    val pedidos: List<PedidoDto>,

    @SerializedName("totalPedidos")
    val totalPedidos: Int,

    @SerializedName("paginaActual")
    val paginaActual: Int,

    @SerializedName("totalPaginas")
    val totalPaginas: Int
)

