package com.example.huertohogar_mobil.network

import android.util.Log
import com.example.huertohogar_mobil.data.datastore.TokenDataStore
import com.example.huertohogar_mobil.network.api.AuthApi
import com.example.huertohogar_mobil.network.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

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
                authApi.refresh(RefreshTokenRequest(currentRefresh))
            }

            if (refreshResp.isSuccessful && refreshResp.body() != null) {
                val body = refreshResp.body()!!
                val newAccess = body.accessToken
                val newRefresh = body.refreshToken
                runBlocking { tokenDataStore.saveTokens(newAccess, newRefresh) }

                response.request.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $newAccess")
                    .build()
            } else {
                Log.e("RefreshAuth", "Refresh failed: ${refreshResp.code()}")
                runBlocking { tokenDataStore.clearTokens() }
                null
            }
        } catch (e: Exception) {
            Log.e("RefreshAuth", "Refresh failed: ${e.message}")
            runBlocking { tokenDataStore.clearTokens() }
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

