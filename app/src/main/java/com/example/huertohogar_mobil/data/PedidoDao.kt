package com.example.huertohogar_mobil.data

import androidx.room.*
import com.example.huertohogar_mobil.model.EstadoPedido
import com.example.huertohogar_mobil.model.Pedido
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
    // Para el usuario comprador: ver sus pedidos realizados
    @Query("SELECT * FROM pedidos WHERE compradorEmail = :email ORDER BY fechaPedido DESC")
    fun getPedidosComoCoprador(email: String): Flow<List<Pedido>>

    // Para el proveedor: ver pedidos a su tienda
    @Query("SELECT * FROM pedidos WHERE proveedorEmail = :email ORDER BY fechaPedido DESC")
    fun getPedidosComoProveedor(email: String): Flow<List<Pedido>>

    // Pedidos pendientes de confirmación por proveedor
    @Query("SELECT * FROM pedidos WHERE proveedorEmail = :email AND estado = 'PENDIENTE' ORDER BY fechaPedido DESC")
    fun getPedidosPendientes(email: String): Flow<List<Pedido>>

    // Pedidos listos para despacho (proveedor puede marcar)
    @Query("SELECT * FROM pedidos WHERE proveedorEmail = :email AND estado IN ('PAGADO', 'LISTO_DESPACHO') ORDER BY fechaPedido DESC")
    fun getPedidosListosDespacho(email: String): Flow<List<Pedido>>

    // Obtener un pedido específico
    @Query("SELECT * FROM pedidos WHERE pedidoId = :pedidoId LIMIT 1")
    suspend fun getPedidoById(pedidoId: String): Pedido?

    @Query("SELECT * FROM pedidos WHERE id = :id LIMIT 1")
    suspend fun getPedidoByLocalId(id: Long): Pedido?

    // Insertar pedido
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPedido(pedido: Pedido): Long

    // Actualizar estado del pedido
    @Query("UPDATE pedidos SET estado = :estado WHERE pedidoId = :pedidoId")
    suspend fun updateEstado(pedidoId: String, estado: EstadoPedido)

    // Actualizar método de pago
    @Query("UPDATE pedidos SET metodoPago = :metodo, datosTransferencia = :datos, estado = :nuevoEstado WHERE pedidoId = :pedidoId")
    suspend fun updateMetodoPago(pedidoId: String, metodo: String, datos: String?, nuevoEstado: EstadoPedido)

    // Marcar como listo para despacho
    @Query("UPDATE pedidos SET estado = :estado, fechaDespacho = :fecha WHERE pedidoId = :pedidoId")
    suspend fun marcarListoDespacho(pedidoId: String, estado: EstadoPedido, fecha: Long)

    // Contar pedidos no leídos/nuevos para notificación
    @Query("SELECT COUNT(*) FROM pedidos WHERE proveedorEmail = :email AND estado = 'PENDIENTE'")
    fun countPedidosPendientes(email: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM pedidos WHERE compradorEmail = :email AND estado IN ('CONFIRMADO', 'LISTO_DESPACHO')")
    fun countNotificacionesComprador(email: String): Flow<Int>

    // Verificar si existe un pedido (evitar duplicados en sincronización)
    @Query("SELECT COUNT(*) > 0 FROM pedidos WHERE pedidoId = :pedidoId")
    suspend fun existsPedido(pedidoId: String): Boolean
}

