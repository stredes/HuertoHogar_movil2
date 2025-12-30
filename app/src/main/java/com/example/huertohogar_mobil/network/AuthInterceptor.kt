package com.example.huertohogar_mobil.network

import com.example.huertohogar_mobil.data.datastore.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val accessToken = runBlocking { tokenDataStore.getAccessToken() }
        val newReq = if (!accessToken.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else original
        return chain.proceed(newReq)
    }
}

