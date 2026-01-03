package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API para mensajería y chat entre usuarios
 * Maneja conversaciones, envío de mensajes y multimedia
 */
interface MessagesApi {

    /**
     * Enviar nuevo mensaje
     */
    @POST("messages")
    suspend fun send(@Body body: EnviarMensajeRequest): Response<MensajeChatDto>

    /**
     * Obtener bandeja de entrada (inbox)
     */
    @GET("messages/inbox")
    suspend fun inbox(): Response<List<MensajeChatDto>>

    /**
     * Obtener inbox paginado
     */
    @GET("messages/inbox")
    suspend fun inboxPaginado(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50
    ): Response<MensajesPaginadosResponse>

    /**
     * Obtener conversación con un usuario específico
     */
    @GET("messages/thread")
    suspend fun thread(
        @Query("with") email: String
    ): Response<List<MensajeChatDto>>

    /**
     * Obtener conversación paginada
     */
    @GET("messages/thread")
    suspend fun threadPaginado(
        @Query("with") email: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50,
        @Query("before") before: Long? = null // Timestamp para cargar mensajes anteriores
    ): Response<MensajesPaginadosResponse>

    /**
     * Obtener lista de conversaciones (chats)
     */
    @GET("messages/conversaciones")
    suspend fun conversaciones(): Response<List<ConversacionDto>>

    /**
     * Obtener conversaciones paginadas
     */
    @GET("messages/conversaciones")
    suspend fun conversacionesPaginadas(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<List<ConversacionDto>>

    /**
     * Buscar mensajes
     */
    @GET("messages/search")
    suspend fun buscar(
        @Query("q") query: String,
        @Query("with") email: String? = null
    ): Response<List<MensajeChatDto>>

    /**
     * Marcar mensajes como leídos
     */
    @POST("messages/marcar-leidos")
    suspend fun marcarLeidos(@Body body: MarcarLeidoRequest): Response<SuccessResponse>

    /**
     * Marcar conversación completa como leída
     */
    @POST("messages/marcar-conversacion-leida")
    suspend fun marcarConversacionLeida(
        @Body body: Map<String, String> // { "email": "usuario@example.com" }
    ): Response<SuccessResponse>

    /**
     * Eliminar mensaje
     */
    @DELETE("messages/{mensajeId}")
    suspend fun eliminar(
        @Path("mensajeId") mensajeId: String,
        @Query("paraTodos") paraTodos: Boolean = false
    ): Response<SuccessResponse>

    /**
     * Obtener un mensaje específico
     */
    @GET("messages/{mensajeId}")
    suspend fun get(@Path("mensajeId") mensajeId: String): Response<MensajeChatDto>

    /**
     * Obtener mensajes no leídos
     */
    @GET("messages/no-leidos")
    suspend fun noLeidos(): Response<List<MensajeChatDto>>

    /**
     * Obtener contador de mensajes no leídos
     */
    @GET("messages/count/no-leidos")
    suspend fun countNoLeidos(): Response<Map<String, Int>> // { "count": 5 }

    /**
     * Obtener estadísticas de mensajería
     */
    @GET("messages/estadisticas")
    suspend fun estadisticas(): Response<EstadisticasMensajesDto>

    /**
     * Reportar spam o abuso
     */
    @POST("messages/{mensajeId}/reportar")
    suspend fun reportar(
        @Path("mensajeId") mensajeId: String,
        @Body body: Map<String, String> // { "motivo": "spam" }
    ): Response<SuccessResponse>

    /**
     * Bloquear usuario para mensajes
     */
    @POST("messages/bloquear")
    suspend fun bloquearUsuario(
        @Body body: Map<String, String> // { "email": "usuario@example.com" }
    ): Response<SuccessResponse>

    /**
     * Desbloquear usuario
     */
    @POST("messages/desbloquear")
    suspend fun desbloquearUsuario(
        @Body body: Map<String, String> // { "email": "usuario@example.com" }
    ): Response<SuccessResponse>
}



