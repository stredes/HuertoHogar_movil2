package com.example.huertohogar_mobil.data

import com.example.huertohogar_mobil.model.EstadoPedido
import com.example.huertohogar_mobil.model.Pedido
import kotlinx.coroutines.flow.Flow

interface PedidoRepository {
    // Observar pedidos
    fun getPedidosComoComprador(email: String): Flow<List<Pedido>>
    fun getPedidosComoProveedor(email: String): Flow<List<Pedido>>
    fun getPedidosPendientesProveedor(email: String): Flow<List<Pedido>>
    fun getPedidosListosDespacho(email: String): Flow<List<Pedido>>

    // Contadores para badges/notificaciones
    fun countPedidosPendientes(email: String): Flow<Int>
    fun countNotificacionesComprador(email: String): Flow<Int>

    // Acciones
    suspend fun crearPedido(pedido: Pedido): Boolean
    suspend fun confirmarPedido(pedidoId: String): Boolean
    suspend fun seleccionarMetodoPago(pedidoId: String, metodo: String, datosTransferencia: String?): Boolean
    suspend fun marcarComoPagado(pedidoId: String): Boolean
    suspend fun marcarComoListoDespacho(pedidoId: String): Boolean
    suspend fun marcarEnCamino(pedidoId: String): Boolean
    suspend fun marcarEntregado(pedidoId: String): Boolean
    suspend fun cancelarPedido(pedidoId: String, motivo: String? = null): Boolean

    // Inicialización
    suspend fun inicializarListeners(email: String, esProveedor: Boolean)
}
