package com.example.huertohogar_mobil.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore para gestión de tokens de autenticación
 *
 * Almacena de forma segura:
 * - Access Token (JWT)
 * - Refresh Token
 * - Tiempo de expiración
 * - Email del usuario
 *
 * Usa DataStore de Android (reemplazo moderno de SharedPreferences)
 */

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val TOKEN_EXPIRATION_KEY = longPreferencesKey("token_expiration")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    /**
     * Guardar tokens de autenticación
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        expiresIn: Long,
        userEmail: String? = null,
        userId: String? = null
    ) {
        context.tokenDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[TOKEN_EXPIRATION_KEY] = System.currentTimeMillis() + (expiresIn * 1000)

            userEmail?.let { preferences[USER_EMAIL_KEY] = it }
            userId?.let { preferences[USER_ID_KEY] = it }
        }
    }

    /**
     * Obtener access token
     */
    suspend fun getAccessToken(): String? {
        val preferences = context.tokenDataStore.data.first()
        return preferences[ACCESS_TOKEN_KEY]
    }

    /**
     * Obtener access token como Flow (reactivo)
     */
    fun getAccessTokenFlow(): Flow<String?> {
        return context.tokenDataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }

    /**
     * Obtener refresh token
     */
    suspend fun getRefreshToken(): String? {
        val preferences = context.tokenDataStore.data.first()
        return preferences[REFRESH_TOKEN_KEY]
    }

    /**
     * Verificar si el token está expirado
     */
    suspend fun isTokenExpired(): Boolean {
        val preferences = context.tokenDataStore.data.first()
        val expiration = preferences[TOKEN_EXPIRATION_KEY] ?: return true
        return System.currentTimeMillis() >= expiration
    }

    /**
     * Verificar si hay sesión activa
     */
    suspend fun hasActiveSession(): Boolean {
        val token = getAccessToken()
        return !token.isNullOrEmpty() && !isTokenExpired()
    }

    /**
     * Obtener email del usuario
     */
    suspend fun getUserEmail(): String? {
        val preferences = context.tokenDataStore.data.first()
        return preferences[USER_EMAIL_KEY]
    }

    /**
     * Obtener ID del usuario
     */
    suspend fun getUserId(): String? {
        val preferences = context.tokenDataStore.data.first()
        return preferences[USER_ID_KEY]
    }

    /**
     * Actualizar solo el access token (después de refresh)
     */
    suspend fun updateAccessToken(accessToken: String, expiresIn: Long) {
        context.tokenDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[TOKEN_EXPIRATION_KEY] = System.currentTimeMillis() + (expiresIn * 1000)
        }
    }

    /**
     * Limpiar todos los tokens (logout)
     */
    suspend fun clearTokens() {
        context.tokenDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(TOKEN_EXPIRATION_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }

    /**
     * Obtener todos los datos de sesión
     */
    suspend fun getSessionData(): SessionData? {
        val preferences = context.tokenDataStore.data.first()
        val accessToken = preferences[ACCESS_TOKEN_KEY]

        return if (accessToken != null) {
            SessionData(
                accessToken = accessToken,
                refreshToken = preferences[REFRESH_TOKEN_KEY],
                expiresAt = preferences[TOKEN_EXPIRATION_KEY] ?: 0L,
                userEmail = preferences[USER_EMAIL_KEY],
                userId = preferences[USER_ID_KEY]
            )
        } else {
            null
        }
    }

    /**
     * Observar estado de sesión como Flow
     */
    fun observeSessionState(): Flow<Boolean> {
        return context.tokenDataStore.data.map { preferences ->
            val token = preferences[ACCESS_TOKEN_KEY]
            val expiration = preferences[TOKEN_EXPIRATION_KEY] ?: 0L
            !token.isNullOrEmpty() && System.currentTimeMillis() < expiration
        }
    }
}

/**
 * Data class para datos de sesión
 */
data class SessionData(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Long,
    val userEmail: String?,
    val userId: String?
) {
    /**
     * Verificar si la sesión está expirada
     */
    fun isExpired(): Boolean {
        return System.currentTimeMillis() >= expiresAt
    }

    /**
     * Tiempo restante hasta expiración (en segundos)
     */
    fun timeUntilExpiration(): Long {
        val remaining = (expiresAt - System.currentTimeMillis()) / 1000
        return maxOf(remaining, 0)
    }
}

