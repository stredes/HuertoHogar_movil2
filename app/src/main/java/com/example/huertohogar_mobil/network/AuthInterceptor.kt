package com.example.huertohogar_mobil.network

import com.example.huertohogar_mobil.data.datastore.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor para agregar token de autenticación a las peticiones HTTP
 * 
 * NOTA: El uso de runBlocking aquí es necesario porque OkHttp Interceptor es síncrono.
 * Alternativas:
 * 1. Usar cache en memoria del token (óptimo)
 * 2. Mantener runBlocking con timeout corto (actual)
 * 
 * Para producción, considerar implementar un cache de token en memoria.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {
    
    /**
     * Intercepta cada petición HTTP y agrega el header Authorization si existe token
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        
        // Obtener token - runBlocking es necesario aquí (limitación de OkHttp)
        val accessToken = runBlocking { 
            tokenDataStore.getAccessToken() 
        }
        
        // Agregar header Authorization solo si hay token
        val newRequest = if (!accessToken.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $accessToken") // Usar header() en vez de addHeader()
                .build()
        } else {
            original
        }
        
        return chain.proceed(newRequest)
    }
}


