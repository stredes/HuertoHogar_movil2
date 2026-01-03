package com.example.huertohogar_mobil.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para funcionalidades sociales (amistades, solicitudes)
 */

/**
 * Solicitud de amistad
 */
data class SolicitudAmistadDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("remitenteId")
    val remitenteId: String,

    @SerializedName("remitenteEmail")
    val remitenteEmail: String,

    @SerializedName("remitenteNombre")
    val remitenteNombre: String,

    @SerializedName("remitenteFotoUrl")
    val remitenteFotoUrl: String?,

    @SerializedName("destinatarioId")
    val destinatarioId: String,

    @SerializedName("destinatarioEmail")
    val destinatarioEmail: String,

    @SerializedName("destinatarioNombre")
    val destinatarioNombre: String,

    @SerializedName("destinatarioFotoUrl")
    val destinatarioFotoUrl: String?,

    @SerializedName("estado")
    val estado: String, // PENDIENTE, ACEPTADA, RECHAZADA, BLOQUEADA

    @SerializedName("mensaje")
    val mensaje: String?,

    @SerializedName("fechaCreacion")
    val fechaCreacion: Long,

    @SerializedName("fechaRespuesta")
    val fechaRespuesta: Long?
)

/**
 * Amistad establecida
 */
data class AmistadDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("usuario1Id")
    val usuario1Id: String,

    @SerializedName("usuario1Email")
    val usuario1Email: String,

    @SerializedName("usuario1Nombre")
    val usuario1Nombre: String,

    @SerializedName("usuario1FotoUrl")
    val usuario1FotoUrl: String?,

    @SerializedName("usuario2Id")
    val usuario2Id: String,

    @SerializedName("usuario2Email")
    val usuario2Email: String,

    @SerializedName("usuario2Nombre")
    val usuario2Nombre: String,

    @SerializedName("usuario2FotoUrl")
    val usuario2FotoUrl: String?,

    @SerializedName("fechaAmistad")
    val fechaAmistad: Long,

    @SerializedName("activo")
    val activo: Boolean = true
)

/**
 * Request para crear solicitud de amistad
 */
data class CrearSolicitudAmistadRequest(
    @SerializedName("destinatarioEmail")
    val destinatarioEmail: String,

    @SerializedName("mensaje")
    val mensaje: String?
)

/**
 * Request para responder solicitud
 */
data class ResponderSolicitudRequest(
    @SerializedName("aceptar")
    val aceptar: Boolean,

    @SerializedName("mensaje")
    val mensaje: String?
)

/**
 * Usuario sugerido para amistad
 */
data class UsuarioSugeridoDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("fotoUrl")
    val fotoUrl: String?,

    @SerializedName("amigosEnComun")
    val amigosEnComun: Int = 0,

    @SerializedName("esVendedor")
    val esVendedor: Boolean = false,

    @SerializedName("productosFavoritos")
    val productosFavoritos: Int = 0
)

/**
 * Estadísticas sociales del usuario
 */
data class EstadisticasSocialesDto(
    @SerializedName("totalAmigos")
    val totalAmigos: Int,

    @SerializedName("solicitudesPendientes")
    val solicitudesPendientes: Int,

    @SerializedName("solicitudesEnviadas")
    val solicitudesEnviadas: Int,

    @SerializedName("amigosOnline")
    val amigosOnline: Int = 0
)

/**
 * Request para buscar usuarios
 */
data class BuscarUsuariosRequest(
    @SerializedName("query")
    val query: String,

    @SerializedName("filtros")
    val filtros: FiltrosBusquedaDto? = null
)

/**
 * Filtros para búsqueda de usuarios
 */
data class FiltrosBusquedaDto(
    @SerializedName("soloVendedores")
    val soloVendedores: Boolean = false,

    @SerializedName("region")
    val region: String? = null,

    @SerializedName("categoria")
    val categoria: String? = null
)

/**
 * Respuesta de búsqueda de usuarios
 */
data class BuscarUsuariosResponse(
    @SerializedName("usuarios")
    val usuarios: List<UsuarioSugeridoDto>,

    @SerializedName("totalResultados")
    val totalResultados: Int
)

/**
 * Request para bloquear/desbloquear usuario
 */
data class BloquearUsuarioRequest(
    @SerializedName("usuarioEmail")
    val usuarioEmail: String,

    @SerializedName("motivo")
    val motivo: String?
)

/**
 * Usuario bloqueado
 */
data class UsuarioBloqueadoDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("usuarioEmail")
    val usuarioEmail: String,

    @SerializedName("usuarioNombre")
    val usuarioNombre: String,

    @SerializedName("fechaBloqueo")
    val fechaBloqueo: Long,

    @SerializedName("motivo")
    val motivo: String?
)

