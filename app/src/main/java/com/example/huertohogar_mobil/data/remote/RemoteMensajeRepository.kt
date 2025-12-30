package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.network.api.MessagesApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteMensajeRepository @Inject constructor(
    private val api: MessagesApi
) {
    suspend fun send(m: MensajeChat): MensajeChat = api.send(m)
    suspend fun inbox(): List<MensajeChat> = api.inbox()
    suspend fun thread(withEmail: String): List<MensajeChat> = api.thread(withEmail)
}

