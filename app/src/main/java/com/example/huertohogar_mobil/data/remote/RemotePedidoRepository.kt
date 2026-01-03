package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.data.PedidoRepository
import com.example.huertohogar_mobil.model.EstadoPedido
import com.example.huertohogar_mobil.model.Pedido
import com.example.huertohogar_mobil.network.api.PedidosApi
import com.example.huertohogar_mobil.network.dto.CrearPedidoRequest
import com.example.huertohogar_mobil.network.dto.MetodoPagoRequest
import com.example.huertohogar_mobil.network.dto.CancelarPedidoRequest
import com.example.huertohogar_mobil.network.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePedidoRepository @Inject constructor(
    private val api: PedidosApi
) : PedidoRepository {

    override fun getPedidosComoComprador(email: String): Flow<List<Pedido>> = flow {
        val resp = api.misPedidos()
        if (resp.isSuccessful && resp.body() != null) {
            emit(resp.body()!!.map { it.toEntity() })
        } else {
            emit(emptyList())
        }
    }

    override fun getPedidosComoProveedor(email: String): Flow<List<Pedido>> = flow {
        val resp = api.pedidosProveedor()
        if (resp.isSuccessful && resp.body() != null) {
            emit(resp.body()!!.map { it.toEntity() })
        } else {
            emit(emptyList())
        }
    }

    override fun getPedidosPendientesProveedor(email: String): Flow<List<Pedido>> = flow {
        val resp = api.pendientesProveedor()
        if (resp.isSuccessful && resp.body() != null) {
            emit(resp.body()!!.map { it.toEntity() })
        } else {
            emit(emptyList())
        }
    }

    override fun getPedidosListosDespacho(email: String): Flow<List<Pedido>> = flow {
        val resp = api.listosDespacho()
        if (resp.isSuccessful && resp.body() != null) {
            emit(resp.body()!!.map { it.toEntity() })
        } else {
            emit(emptyList())
        }
    }

    override fun countPedidosPendientes(email: String): Flow<Int> = flow {
        val resp = api.countPendientes()
        if (resp.isSuccessful && resp.body() != null) {
            emit(resp.body()!!.count)
        } else {
            emit(0)
        }
    }

    override fun countNotificacionesComprador(email: String): Flow<Int> = flow {
        val resp = api.countNotificaciones()
        if (resp.isSuccessful && resp.body() != null) {
            emit(resp.body()!!.count)
        } else {
            emit(0)
        }
    }

    override suspend fun crearPedido(pedido: Pedido): Boolean {
        // Parsear items desde el JSON
        val gson = com.google.gson.Gson()
        val items = try {
            gson.fromJson(pedido.detalleJson, Array<com.example.huertohogar_mobil.network.dto.ItemPedidoDto>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }

        val request = CrearPedidoRequest(
            proveedorEmail = pedido.proveedorEmail,
            items = items.map {
                CrearPedidoRequest.ItemPedidoRequest(
                    productoId = it.productoId,
                    cantidad = it.cantidad
                )
            },
            direccionEntrega = pedido.direccionEntrega,
            notas = null
        )
        val resp = api.crear(request)
        return resp.isSuccessful
    }

    override suspend fun confirmarPedido(pedidoId: String): Boolean {
        val resp = api.confirmar(pedidoId)
        return resp.isSuccessful
    }

    override suspend fun seleccionarMetodoPago(pedidoId: String, metodo: String, datosTransferencia: String?): Boolean {
        val request = MetodoPagoRequest(metodo, datosTransferencia)
        val resp = api.seleccionarMetodoPago(pedidoId, request)
        return resp.isSuccessful
    }

    override suspend fun marcarComoPagado(pedidoId: String): Boolean {
        val resp = api.pagado(pedidoId)
        return resp.isSuccessful
    }

    override suspend fun marcarComoListoDespacho(pedidoId: String): Boolean {
        val resp = api.listoDespacho(pedidoId)
        return resp.isSuccessful
    }

    override suspend fun marcarEnCamino(pedidoId: String): Boolean {
        val resp = api.enCamino(pedidoId)
        return resp.isSuccessful
    }

    override suspend fun marcarEntregado(pedidoId: String): Boolean {
        val resp = api.entregado(pedidoId)
        return resp.isSuccessful
    }

    override suspend fun cancelarPedido(pedidoId: String, motivo: String?): Boolean {
        val request = CancelarPedidoRequest(motivo ?: "")
        val resp = api.cancelar(pedidoId, request)
        return resp.isSuccessful
    }

    override suspend fun inicializarListeners(email: String, esProveedor: Boolean) {
        // No-op en modo remoto
    }
}

