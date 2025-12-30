package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.data.SessionManager
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.network.api.AuthApi
import com.example.huertohogar_mobil.network.api.AuthResponse
import com.example.huertohogar_mobil.network.api.LoginRequest
import com.example.huertohogar_mobil.network.api.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAuthRepository @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): User? = withContext(Dispatchers.IO) {
        val resp: AuthResponse = api.login(LoginRequest(email, password))
        sessionManager.saveTokens(resp.accessToken, resp.refreshToken)
        val profile = api.me()
        sessionManager.saveUserSession(profile.email, profile.name, profile.role == "provider")
        User(name = profile.name, email = profile.email, passwordHash = "", role = profile.role)
    }

    suspend fun register(name: String, email: String, password: String, rut: String): User? = withContext(Dispatchers.IO) {
        val resp = api.register(RegisterRequest(name, email, password, rut))
        sessionManager.saveTokens(resp.accessToken, resp.refreshToken)
        val profile = api.me()
        sessionManager.saveUserSession(profile.email, profile.name, profile.role == "provider")
        User(name = profile.name, email = profile.email, passwordHash = "", role = profile.role, rut = rut)
    }

    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        val refresh = sessionManager.getRefreshToken() ?: return@withContext false
        val newTokens = api.refresh(com.example.huertohogar_mobil.network.RefreshRequest(refresh))
        sessionManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
        true
    }

    fun logout() {
        sessionManager.clearSession()
    }
}

