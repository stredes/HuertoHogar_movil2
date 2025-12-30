package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.model.MensajeContacto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ContactApi {
    @POST("contact") suspend fun send(@Body body: MensajeContacto): MensajeContacto
    @GET("contact/admin") suspend fun adminList(): List<MensajeContacto>
    @PATCH("contact/admin/{id}") suspend fun adminUpdate(@Path("id") id: String, @Body body: Map<String, Any?>): MensajeContacto
    @DELETE("contact/admin/{id}") suspend fun adminDelete(@Path("id") id: String): Map<String, String>
}

