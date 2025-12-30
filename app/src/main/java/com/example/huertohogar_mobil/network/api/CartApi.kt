package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.model.CarritoItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CartApi {
    @GET("cart") suspend fun getCart(): List<CarritoItem>
    @POST("cart/items") suspend fun addItem(@Body body: Map<String, Any>): List<CarritoItem>
    @DELETE("cart/items/{productId}") suspend fun removeItem(@Path("productId") productId: String): List<CarritoItem>
    @POST("cart/clear") suspend fun clear(): Map<String, String>
}

