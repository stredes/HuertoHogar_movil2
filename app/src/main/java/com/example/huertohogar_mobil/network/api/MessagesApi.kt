package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.model.MensajeChat
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MessagesApi {
    @POST("messages") suspend fun send(@Body body: MensajeChat): MensajeChat
    @GET("messages/inbox") suspend fun inbox(): List<MensajeChat>
    @GET("messages/thread") suspend fun thread(@Query("with") email: String): List<MensajeChat>
}

