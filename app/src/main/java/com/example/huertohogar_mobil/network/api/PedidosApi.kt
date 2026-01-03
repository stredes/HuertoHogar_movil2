package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API para gestión de pedidos y órdenes
 * Maneja el flujo completo desde creación hasta entrega
 */
interface PedidosApi {

    /**
     * Crear nuevo pedido
     */
    @POST("pedidos")
    suspend fun crear(@Body pedido: CrearPedidoRequest): Response<PedidoDto>

    /**
     * Obtener mis pedidos como comprador
     */
    @GET("pedidos/mis")
    suspend fun misPedidos(): Response<List<PedidoDto>>

    /**
     * Obtener mis pedidos paginados
     */
    @GET("pedidos/mis")
    suspend fun misPedidosPaginados(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("estado") estado: String? = null
    ): Response<PedidosPaginadosResponse>

    /**
     * Obtener pedidos como proveedor/vendedor
     */
    @GET("pedidos/proveedor")
    suspend fun pedidosProveedor(): Response<List<PedidoDto>>

    /**
     * Obtener pedidos pendientes del proveedor
     */
    @GET("pedidos/proveedor/pendientes")
    suspend fun pendientesProveedor(): Response<List<PedidoDto>>

    /**
     * Obtener pedidos listos para despacho
     */
    @GET("pedidos/proveedor/listos-despacho")
    suspend fun listosDespacho(): Response<List<PedidoDto>>

    /**
     * Obtener un pedido específico por ID
     */
    @GET("pedidos/{pedidoId}")
    suspend fun get(@Path("pedidoId") pedidoId: String): Response<PedidoDto>

    /**
     * Filtrar pedidos por estado
     */
    @GET("pedidos/estado/{estado}")
    suspend fun porEstado(
        @Path("estado") estado: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<PedidosPaginadosResponse>

    /**
     * Obtener contadores de pedidos pendientes
     */
    @GET("pedidos/counts/pendientes")
    suspend fun countPendientes(): Response<ContadoresResponse>

    /**
     * Obtener contador de notificaciones de pedidos
     */
    @GET("pedidos/counts/notificaciones")
    suspend fun countNotificaciones(): Response<Map<String, Int>>

    /**
     * Seleccionar método de pago
     */
    @PATCH("pedidos/{pedidoId}/metodo-pago")
    suspend fun seleccionarMetodoPago(
        @Path("pedidoId") pedidoId: String,
        @Body body: MetodoPagoRequest
    ): Response<PedidoDto>

    /**
     * Actualizar estado del pedido
     */
    @PATCH("pedidos/{pedidoId}/estado")
    suspend fun actualizarEstado(
        @Path("pedidoId") pedidoId: String,
        @Body body: ActualizarEstadoRequest
    ): Response<PedidoDto>

    /**
     * Confirmar pedido (vendedor acepta)
     */
    @POST("pedidos/{pedidoId}/confirmar")
    suspend fun confirmar(@Path("pedidoId") pedidoId: String): Response<PedidoDto>

    /**
     * Marcar como pagado
     */
    @POST("pedidos/{pedidoId}/pagado")
    suspend fun pagado(@Path("pedidoId") pedidoId: String): Response<PedidoDto>

    /**
     * Marcar como listo para despacho
     */
    @POST("pedidos/{pedidoId}/listo-despacho")
    suspend fun listoDespacho(@Path("pedidoId") pedidoId: String): Response<PedidoDto>

    /**
     * Marcar como en camino
     */
    @POST("pedidos/{pedidoId}/en-camino")
    suspend fun enCamino(@Path("pedidoId") pedidoId: String): Response<PedidoDto>

    /**
     * Marcar como entregado
     */
    @POST("pedidos/{pedidoId}/entregado")
    suspend fun entregado(@Path("pedidoId") pedidoId: String): Response<PedidoDto>

    /**
     * Cancelar pedido
     */
    @POST("pedidos/{pedidoId}/cancelar")
    suspend fun cancelar(
        @Path("pedidoId") pedidoId: String,
        @Body body: CancelarPedidoRequest
    ): Response<PedidoDto>

    /**
     * Obtener historial/tracking del pedido
     */
    @GET("pedidos/{pedidoId}/tracking")
    suspend fun tracking(@Path("pedidoId") pedidoId: String): Response<List<TrackingDto>>

    /**
     * Obtener notificaciones de pedidos
     */
    @GET("pedidos/notificaciones")
    suspend fun notificaciones(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("soloNoLeidas") soloNoLeidas: Boolean = false
    ): Response<List<NotificacionPedidoDto>>

    /**
     * Marcar notificación como leída
     */
    @PATCH("pedidos/notificaciones/{notificacionId}/leida")
    suspend fun marcarNotificacionLeida(
        @Path("notificacionId") notificacionId: String
    ): Response<NotificacionPedidoDto>

    /**
     * Marcar todas las notificaciones como leídas
     */
    @POST("pedidos/notificaciones/marcar-todas-leidas")
    suspend fun marcarTodasLeidas(): Response<SuccessResponse>
}



