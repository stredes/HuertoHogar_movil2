package com.example.huertohogar_mobil.network

import android.util.Log
import com.example.huertohogar_mobil.data.datastore.TokenDataStore
import com.example.huertohogar_mobil.network.api.AuthApi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

data class RefreshRequest(val refreshToken: String)
data class RefreshResponse(val accessToken: String, val refreshToken: String)

@Singleton
class RefreshAuthenticator @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val authApi: AuthApi
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // Evitar loops
        if (responseCount(response) >= 2) return null

        val currentRefresh = runBlocking { tokenDataStore.getRefreshToken() }
            ?: return null

        return try {
            val refreshResp = runBlocking {
                authApi.refresh(RefreshRequest(currentRefresh))
            }
            val newAccess = refreshResp.accessToken
            val newRefresh = refreshResp.refreshToken
            runBlocking { tokenDataStore.saveTokens(newAccess, newRefresh) }

            response.request.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $newAccess")
                .build()
        } catch (e: Exception) {
            Log.e("RefreshAuth", "Refresh failed: ${e.message}")
            runBlocking { tokenDataStore.clear() }
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}

