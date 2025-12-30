package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.model.Producto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ProductsApi {
    @GET("products") suspend fun list(): List<Producto>
    @GET("products/{id}") suspend fun get(@Path("id") id: String): Producto
    @POST("products") suspend fun create(@Body producto: Producto): Producto
    @PUT("products/{id}") suspend fun update(@Path("id") id: String, @Body producto: Map<String, Any?>): Producto
    @DELETE("products/{id}") suspend fun delete(@Path("id") id: String): Map<String, String>

    @Multipart
    @POST("uploads/products")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Map<String, String> // { url }
}

