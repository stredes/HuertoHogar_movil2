package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.network.RefreshRequest
import com.example.huertohogar_mobil.network.RefreshResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// DTOs

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String, val rut: String)
data class AuthResponse(val accessToken: String, val refreshToken: String)
data class ProfileResponse(val name: String, val email: String, val role: String)

data class ResetPasswordRequest(val email: String, val newPassword: String)
data class VerifyEmailRequest(val email: String)

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): ProfileResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body req: ResetPasswordRequest): Map<String, String>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body req: VerifyEmailRequest): Map<String, Boolean>

    @POST("auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): RefreshResponse
}

