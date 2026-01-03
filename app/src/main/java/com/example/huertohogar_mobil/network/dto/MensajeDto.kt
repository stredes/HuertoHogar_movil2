package com.example.huertohogar_mobil.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para Mensajería y Chat
 */

/**
 * Mensaje del servidor
 */
data class MensajeChatDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("remitenteId")
    val remitenteId: String,

    @SerializedName("remitenteEmail")
    val remitenteEmail: String,

    @SerializedName("remitenteNombre")
    val remitenteNombre: String,

    @SerializedName("destinatarioId")
    val destinatarioId: String,

    @SerializedName("destinatarioEmail")
    val destinatarioEmail: String,

    @SerializedName("destinatarioNombre")
    val destinatarioNombre: String,

    @SerializedName("contenido")
    val contenido: String,

    @SerializedName("tipoContenido")
    val tipoContenido: String = "TEXTO", // TEXTO, IMAGEN, AUDIO, VIDEO, UBICACION, ARCHIVO

    @SerializedName("mediaUrl")
    val mediaUrl: String? = null,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("estado")
    val estado: String = "ENVIADO", // ENVIANDO, ENVIADO, RECIBIDO, LEIDO, ERROR

    @SerializedName("leido")
    val leido: Boolean = false,

    @SerializedName("fechaLectura")
    val fechaLectura: Long? = null,

    @SerializedName("metadata")
    val metadata: Map<String, Any>? = null
)

/**
 * Request para enviar mensaje
 */
data class EnviarMensajeRequest(
    @SerializedName("destinatarioEmail")
    val destinatarioEmail: String,

    @SerializedName("contenido")
    val contenido: String,

    @SerializedName("tipoContenido")
    val tipoContenido: String = "TEXTO",

    @SerializedName("mediaUrl")
    val mediaUrl: String? = null,

    @SerializedName("metadata")
    val metadata: Map<String, Any>? = null
)

/**
 * Conversación/Chat con otro usuario
 */
data class ConversacionDto(
    @SerializedName("usuarioId")
    val usuarioId: String,

    @SerializedName("usuarioEmail")
    val usuarioEmail: String,

    @SerializedName("usuarioNombre")
    val usuarioNombre: String,

    @SerializedName("usuarioFotoUrl")
    val usuarioFotoUrl: String?,

    @SerializedName("ultimoMensaje")
    val ultimoMensaje: MensajeChatDto?,

    @SerializedName("mensajesNoLeidos")
    val mensajesNoLeidos: Int = 0,

    @SerializedName("fechaUltimaActividad")
    val fechaUltimaActividad: Long,

    @SerializedName("activo")
    val activo: Boolean = true
)

/**
 * Request para marcar mensajes como leídos
 */
data class MarcarLeidoRequest(
    @SerializedName("mensajesIds")
    val mensajesIds: List<String>
)

/**
 * Request para eliminar mensaje
 */
data class EliminarMensajeRequest(
    @SerializedName("mensajeId")
    val mensajeId: String,

    @SerializedName("eliminarParaTodos")
    val eliminarParaTodos: Boolean = false
)

/**
 * Respuesta de mensajes paginados
 */
data class MensajesPaginadosResponse(
    @SerializedName("mensajes")
    val mensajes: List<MensajeChatDto>,

    @SerializedName("totalMensajes")
    val totalMensajes: Int,

    @SerializedName("paginaActual")
    val paginaActual: Int,

    @SerializedName("totalPaginas")
    val totalPaginas: Int,

    @SerializedName("tieneSiguiente")
    val tieneSiguiente: Boolean
)

/**
 * Estadísticas de mensajería
 */
data class EstadisticasMensajesDto(
    @SerializedName("totalConversaciones")
    val totalConversaciones: Int,

    @SerializedName("mensajesNoLeidos")
    val mensajesNoLeidos: Int,

    @SerializedName("mensajesEnviados")
    val mensajesEnviados: Int,

    @SerializedName("mensajesRecibidos")
    val mensajesRecibidos: Int
)

