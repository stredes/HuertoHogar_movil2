package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.data.SessionManager
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.network.api.AuthApi
import com.example.huertohogar_mobil.network.dto.LoginRequestDto
import com.example.huertohogar_mobil.network.dto.RegistroRequestDto
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
        val resp = api.login(LoginRequestDto(email, password))
        if (resp.isSuccessful && resp.body() != null) {
            val authResp = resp.body()!!
            sessionManager.saveTokens(authResp.accessToken, authResp.refreshToken)
            val profile = api.me()
            if (profile.isSuccessful && profile.body() != null) {
                val prof = profile.body()!!
                sessionManager.saveUserSession(prof.email, prof.name, prof.role == "provider")
                User(name = prof.name, email = prof.email, passwordHash = "", role = prof.role)
            } else null
        } else null
    }

    suspend fun register(name: String, email: String, password: String, rut: String): User? = withContext(Dispatchers.IO) {
        val resp = api.register(RegistroRequestDto(name, email, password, rut))
        if (resp.isSuccessful && resp.body() != null) {
            val authResp = resp.body()!!
            sessionManager.saveTokens(authResp.accessToken, authResp.refreshToken)
            val profile = api.me()
            if (profile.isSuccessful && profile.body() != null) {
                val prof = profile.body()!!
                sessionManager.saveUserSession(prof.email, prof.name, prof.role == "provider")
                User(name = prof.name, email = prof.email, passwordHash = "", role = prof.role, rut = rut)
            } else null
        } else null
    }

    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        val refresh = sessionManager.getRefreshToken() ?: return@withContext false
        val newTokens = api.refresh(com.example.huertohogar_mobil.network.dto.RefreshTokenRequest(refresh))
        if (newTokens.isSuccessful && newTokens.body() != null) {
            val tokens = newTokens.body()!!
            sessionManager.saveTokens(tokens.accessToken, tokens.refreshToken)
            true
        } else false
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
