package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.network.api.MessagesApi
import com.example.huertohogar_mobil.network.dto.EnviarMensajeRequest
import com.example.huertohogar_mobil.network.mappers.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteMensajeRepository @Inject constructor(
    private val api: MessagesApi
) {
    suspend fun send(m: MensajeChat): MensajeChat? {
        val request = EnviarMensajeRequest(
            destinatarioEmail = m.destinatarioEmail ?: "",
            contenido = m.contenido,
            tipoContenido = m.tipoContenido
        )
        val resp = api.send(request)
        return if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.toEntity()
        } else null
    }

    suspend fun inbox(): List<MensajeChat>? {
        val resp = api.inbox()
        return if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.map { it.toEntity() }
        } else null
    }

    suspend fun thread(withEmail: String): List<MensajeChat>? {
        val resp = api.thread(withEmail)
        return if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.map { it.toEntity() }
        } else null
    }
}

