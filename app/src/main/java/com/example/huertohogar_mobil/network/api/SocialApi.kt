package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API para funcionalidades sociales
 * Maneja amistades, solicitudes y conexiones entre usuarios
 */
interface SocialApi {

    // ========== SOLICITUDES DE AMISTAD ==========

    /**
     * Enviar solicitud de amistad
     */
    @POST("social/requests")
    suspend fun createRequest(
        @Body body: CrearSolicitudAmistadRequest
    ): Response<SolicitudAmistadDto>

    /**
     * Obtener solicitudes recibidas (entrantes)
     */
    @GET("social/requests/incoming")
    suspend fun incoming(): Response<List<SolicitudAmistadDto>>

    /**
     * Obtener solicitudes enviadas (salientes)
     */
    @GET("social/requests/outgoing")
    suspend fun outgoing(): Response<List<SolicitudAmistadDto>>

    /**
     * Obtener solicitud específica por ID
     */
    @GET("social/requests/{id}")
    suspend fun getRequest(@Path("id") id: String): Response<SolicitudAmistadDto>

    /**
     * Aceptar solicitud de amistad
     */
    @POST("social/requests/{id}/accept")
    suspend fun accept(@Path("id") id: String): Response<SolicitudAmistadDto>

    /**
     * Rechazar solicitud de amistad
     */
    @POST("social/requests/{id}/reject")
    suspend fun reject(
        @Path("id") id: String,
        @Body body: Map<String, String>? = null // Opcional: { "motivo": "..." }
    ): Response<SolicitudAmistadDto>

    /**
     * Cancelar solicitud enviada
     */
    @DELETE("social/requests/{id}")
    suspend fun cancelRequest(@Path("id") id: String): Response<SuccessResponse>

    // ========== AMISTADES ==========

    /**
     * Obtener lista de amigos
     */
    @GET("social/friends")
    suspend fun friends(): Response<List<AmistadDto>>

    /**
     * Obtener amigos paginados
     */
    @GET("social/friends")
    suspend fun friendsPaginados(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<List<AmistadDto>>

    /**
     * Buscar entre mis amigos
     */
    @GET("social/friends/search")
    suspend fun buscarAmigos(
        @Query("q") query: String
    ): Response<List<AmistadDto>>

    /**
     * Obtener amistad específica
     */
    @GET("social/friends/{id}")
    suspend fun getFriend(@Path("id") id: String): Response<AmistadDto>

    /**
     * Eliminar amistad
     */
    @DELETE("social/friends/{id}")
    suspend fun deleteFriend(@Path("id") id: String): Response<SuccessResponse>

    /**
     * Verificar si es amigo
     */
    @GET("social/friends/verificar")
    suspend fun verificarAmistad(
        @Query("email") email: String
    ): Response<Map<String, Boolean>> // { "esAmigo": true/false }

    // ========== BÚSQUEDA Y SUGERENCIAS ==========

    /**
     * Buscar usuarios
     */
    @POST("social/buscar-usuarios")
    suspend fun buscarUsuarios(
        @Body body: BuscarUsuariosRequest
    ): Response<BuscarUsuariosResponse>

    /**
     * Obtener usuarios sugeridos
     */
    @GET("social/sugerencias")
    suspend fun sugerencias(
        @Query("limit") limit: Int = 10
    ): Response<List<UsuarioSugeridoDto>>

    /**
     * Obtener amigos en común con otro usuario
     */
    @GET("social/amigos-comunes")
    suspend fun amigosComunes(
        @Query("email") email: String
    ): Response<List<AmistadDto>>

    // ========== ESTADÍSTICAS ==========

    /**
     * Obtener estadísticas sociales
     */
    @GET("social/estadisticas")
    suspend fun estadisticas(): Response<EstadisticasSocialesDto>

    /**
     * Obtener contador de solicitudes pendientes
     */
    @GET("social/count/pendientes")
    suspend fun countPendientes(): Response<Map<String, Int>> // { "count": 3 }

    // ========== BLOQUEOS ==========

    /**
     * Bloquear usuario
     */
    @POST("social/bloquear")
    suspend fun bloquear(
        @Body body: BloquearUsuarioRequest
    ): Response<SuccessResponse>

    /**
     * Desbloquear usuario
     */
    @POST("social/desbloquear")
    suspend fun desbloquear(
        @Body body: Map<String, String> // { "usuarioEmail": "..." }
    ): Response<SuccessResponse>

    /**
     * Obtener lista de usuarios bloqueados
     */
    @GET("social/bloqueados")
    suspend fun bloqueados(): Response<List<UsuarioBloqueadoDto>>

    /**
     * Verificar si un usuario está bloqueado
     */
    @GET("social/bloqueados/verificar")
    suspend fun verificarBloqueado(
        @Query("email") email: String
    ): Response<Map<String, Boolean>> // { "bloqueado": true/false }
}



