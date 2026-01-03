package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.network.RefreshRequest
import com.example.huertohogar_mobil.network.RefreshResponse
import com.example.huertohogar_mobil.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API para autenticación y gestión de usuarios
 * Maneja login, registro, tokens y perfil de usuario
 */
interface AuthApi {

    /**
     * Iniciar sesión con email y contraseña
     */
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequestDto): Response<AuthResponseDto>

    /**
     * Login simple (compatible con código existente)
     */
    @POST("auth/login")
    suspend fun loginSimple(@Body req: LoginRequest): Response<AuthResponse>

    /**
     * Registrar nuevo usuario
     */
    @POST("auth/register")
    suspend fun register(@Body req: RegistroRequestDto): Response<AuthResponseDto>

    /**
     * Registro simple (compatible con código existente)
     */
    @POST("auth/register")
    suspend fun registerSimple(@Body req: RegisterRequest): Response<AuthResponse>

    /**
     * Obtener perfil del usuario actual (requiere token)
     */
    @GET("auth/me")
    suspend fun me(): Response<ProfileResponseDto>

    /**
     * Actualizar perfil del usuario
     */
    @PUT("auth/me")
    suspend fun actualizarPerfil(@Body req: ActualizarPerfilRequest): Response<ProfileResponseDto>

    /**
     * Cambiar contraseña
     */
    @POST("auth/cambiar-password")
    suspend fun cambiarPassword(@Body req: CambiarPasswordRequest): Response<SuccessResponse>

    /**
     * Recuperar contraseña (envía email)
     */
    @POST("auth/recuperar-password")
    suspend fun recuperarPassword(@Body req: RecuperarPasswordRequest): Response<SuccessResponse>

    /**
     * Resetear contraseña con token
     */
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body req: ResetPasswordRequest): Response<SuccessResponse>

    /**
     * Verificar email del usuario
     */
    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body req: VerifyEmailRequest): Response<Map<String, Boolean>>

    /**
     * Reenviar email de verificación
     */
    @POST("auth/reenviar-verificacion")
    suspend fun reenviarVerificacion(): Response<SuccessResponse>

    /**
     * Refrescar token de acceso
     */
    @POST("auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): Response<RefreshResponse>

    /**
     * Cerrar sesión
     */
    @POST("auth/logout")
    suspend fun logout(): Response<SuccessResponse>

    /**
     * Cerrar todas las sesiones del usuario
     */
    @POST("auth/logout-all")
    suspend fun logoutAll(): Response<SuccessResponse>

    /**
     * Actualizar token FCM para notificaciones push
     */
    @POST("auth/fcm-token")
    suspend fun updateFcmToken(@Body body: Map<String, String>): Response<SuccessResponse>
}

// DTOs simples para compatibilidad con código existente
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String, val rut: String)
data class AuthResponse(val accessToken: String, val refreshToken: String)
data class ProfileResponse(val name: String, val email: String, val role: String)
data class ResetPasswordRequest(val email: String, val newPassword: String)
data class VerifyEmailRequest(val email: String)


