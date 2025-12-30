package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.data.PedidoRepository
import com.example.huertohogar_mobil.model.EstadoPedido
import com.example.huertohogar_mobil.model.Pedido
import com.example.huertohogar_mobil.network.api.PedidosApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePedidoRepository @Inject constructor(
    private val api: PedidosApi
) : PedidoRepository {

    override fun getPedidosComoComprador(email: String): Flow<List<Pedido>> = flow {
        emit(api.misPedidos())
    }

    override fun getPedidosComoProveedor(email: String): Flow<List<Pedido>> = flow {
        emit(api.pedidosProveedor())
    }

    override fun getPedidosPendientesProveedor(email: String): Flow<List<Pedido>> = flow {
        emit(api.pendientesProveedor())
    }

    override fun getPedidosListosDespacho(email: String): Flow<List<Pedido>> = flow {
        emit(api.listosDespacho())
    }

    override fun countPedidosPendientes(email: String): Flow<Int> = flow {
        emit(api.countPendientes()["count"] ?: 0)
    }

    override fun countNotificacionesComprador(email: String): Flow<Int> = flow {
        emit(api.countNotificaciones()["count"] ?: 0)
    }

    override suspend fun crearPedido(pedido: Pedido): Boolean {
        api.crear(pedido)
        return true
    }

    override suspend fun confirmarPedido(pedidoId: String): Boolean {
        api.confirmar(pedidoId)
        return true
    }

    override suspend fun seleccionarMetodoPago(pedidoId: String, metodo: String, datosTransferencia: String?): Boolean {
        api.seleccionarMetodoPago(pedidoId, mapOf("metodoPago" to metodo, "datosTransferencia" to datosTransferencia))
        return true
    }

    override suspend fun marcarComoPagado(pedidoId: String): Boolean {
        api.pagado(pedidoId)
        return true
    }

    override suspend fun marcarComoListoDespacho(pedidoId: String): Boolean {
        api.listoDespacho(pedidoId)
        return true
    }

    override suspend fun marcarEnCamino(pedidoId: String): Boolean {
        api.enCamino(pedidoId)
        return true
    }

    override suspend fun marcarEntregado(pedidoId: String): Boolean {
        api.entregado(pedidoId)
        return true
    }

    override suspend fun cancelarPedido(pedidoId: String, motivo: String?): Boolean {
        val body = if (!motivo.isNullOrBlank()) mapOf("motivo" to motivo) else emptyMap()
        api.cancelar(pedidoId, body)
        return true
    }

    override suspend fun inicializarListeners(email: String, esProveedor: Boolean) {
        // No-op en modo remoto
    }
}

