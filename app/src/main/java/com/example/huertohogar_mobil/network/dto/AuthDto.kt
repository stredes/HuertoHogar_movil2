package com.example.huertohogar_mobil.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para Autenticación y Usuarios
 */

/**
 * Request de login extendido
 */
data class LoginRequestDto(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("deviceInfo")
    val deviceInfo: DeviceInfoDto? = null,

    @SerializedName("fcmToken")
    val fcmToken: String? = null
)

/**
 * Request de registro extendido
 */
data class RegistroRequestDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("rut")
    val rut: String,

    @SerializedName("telefono")
    val telefono: String?,

    @SerializedName("direccion")
    val direccion: String?,

    @SerializedName("role")
    val role: String = "user", // user, vendedor, admin

    @SerializedName("deviceInfo")
    val deviceInfo: DeviceInfoDto? = null
)

/**
 * Información del dispositivo
 */
data class DeviceInfoDto(
    @SerializedName("deviceId")
    val deviceId: String,

    @SerializedName("deviceName")
    val deviceName: String,

    @SerializedName("osVersion")
    val osVersion: String,

    @SerializedName("appVersion")
    val appVersion: String
)

/**
 * Respuesta de autenticación
 */
data class AuthResponseDto(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("tokenType")
    val tokenType: String = "Bearer",

    @SerializedName("expiresIn")
    val expiresIn: Long, // Segundos hasta expiración

    @SerializedName("usuario")
    val usuario: UsuarioDto
)

/**
 * Usuario completo
 */
data class UsuarioDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("rut")
    val rut: String?,

    @SerializedName("telefono")
    val telefono: String?,

    @SerializedName("direccion")
    val direccion: String?,

    @SerializedName("fotoPerfilUrl")
    val fotoPerfilUrl: String?,

    @SerializedName("role")
    val role: String,

    @SerializedName("emailVerificado")
    val emailVerificado: Boolean = false,

    @SerializedName("activo")
    val activo: Boolean = true,

    @SerializedName("fechaRegistro")
    val fechaRegistro: Long,

    @SerializedName("ultimaActividad")
    val ultimaActividad: Long?
)

/**
 * Perfil de usuario (sin datos sensibles)
 */
data class ProfileResponseDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("rut")
    val rut: String?,

    @SerializedName("fotoPerfilUrl")
    val fotoPerfilUrl: String?
)

/**
 * Request para cambiar contraseña
 */
data class CambiarPasswordRequest(
    @SerializedName("passwordActual")
    val passwordActual: String,

    @SerializedName("passwordNuevo")
    val passwordNuevo: String
)

/**
 * Request para recuperar contraseña
 */
data class RecuperarPasswordRequest(
    @SerializedName("email")
    val email: String
)

/**
 * Request para actualizar perfil
 */
data class ActualizarPerfilRequest(
    @SerializedName("name")
    val name: String?,

    @SerializedName("telefono")
    val telefono: String?,

    @SerializedName("direccion")
    val direccion: String?,

    @SerializedName("fotoPerfilUrl")
    val fotoPerfilUrl: String?
)

/**
 * Respuesta genérica de éxito
 */
data class SuccessResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)

