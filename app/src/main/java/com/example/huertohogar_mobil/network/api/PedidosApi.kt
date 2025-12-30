package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.model.Pedido
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PedidosApi {
    @POST("pedidos") suspend fun crear(@Body pedido: Pedido): Pedido
    @GET("pedidos/mis") suspend fun misPedidos(): List<Pedido>
    @GET("pedidos/proveedor") suspend fun pedidosProveedor(): List<Pedido>
    @GET("pedidos/proveedor/pendientes") suspend fun pendientesProveedor(): List<Pedido>
    @GET("pedidos/proveedor/listos-despacho") suspend fun listosDespacho(): List<Pedido>
    @GET("pedidos/counts/pendientes") suspend fun countPendientes(): Map<String, Int>
    @GET("pedidos/counts/notificaciones") suspend fun countNotificaciones(): Map<String, Int>
    @GET("pedidos/{pedidoId}") suspend fun get(@Path("pedidoId") pedidoId: String): Pedido

    @PATCH("pedidos/{pedidoId}/metodo-pago")
    suspend fun seleccionarMetodoPago(
        @Path("pedidoId") pedidoId: String,
        @Body body: Map<String, Any?>
    ): Pedido

    @PATCH("pedidos/{pedidoId}/estado")
    suspend fun actualizarEstado(
        @Path("pedidoId") pedidoId: String,
        @Body body: Map<String, Any?>
    ): Pedido

    @POST("pedidos/{pedidoId}/confirmar") suspend fun confirmar(@Path("pedidoId") pedidoId: String): Pedido
    @POST("pedidos/{pedidoId}/pagado") suspend fun pagado(@Path("pedidoId") pedidoId: String): Pedido
    @POST("pedidos/{pedidoId}/listo-despacho") suspend fun listoDespacho(@Path("pedidoId") pedidoId: String): Pedido
    @POST("pedidos/{pedidoId}/en-camino") suspend fun enCamino(@Path("pedidoId") pedidoId: String): Pedido
    @POST("pedidos/{pedidoId}/entregado") suspend fun entregado(@Path("pedidoId") pedidoId: String): Pedido
    @POST("pedidos/{pedidoId}/cancelar") suspend fun cancelar(@Path("pedidoId") pedidoId: String, @Body body: Map<String, Any?> = emptyMap()): Pedido

    // Paginación y filtros opcionales
    @GET("pedidos/mis") suspend fun misPedidosPaginados(
        @Query("page") page: Int?,
        @Query("size") size: Int?
    ): List<Pedido>
}

