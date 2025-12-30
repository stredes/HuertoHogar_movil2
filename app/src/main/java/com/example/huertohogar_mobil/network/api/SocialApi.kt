package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.model.Solicitud
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SocialApi {
    @POST("social/requests") suspend fun createRequest(@Body body: Map<String, String>): Solicitud
    @GET("social/requests/incoming") suspend fun incoming(): List<Solicitud>
    @GET("social/requests/outgoing") suspend fun outgoing(): List<Solicitud>
    @POST("social/requests/{id}/accept") suspend fun accept(@Path("id") id: String): Solicitud
    @POST("social/requests/{id}/reject") suspend fun reject(@Path("id") id: String): Solicitud
    @GET("social/friends") suspend fun friends(): List<Solicitud>
}

