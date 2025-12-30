package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.model.MensajeContacto
import com.example.huertohogar_mobil.network.api.ContactApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteContactRepository @Inject constructor(
    private val api: ContactApi
) {
    suspend fun send(msg: MensajeContacto): MensajeContacto = api.send(msg)
    suspend fun adminList(): List<MensajeContacto> = api.adminList()
    suspend fun adminUpdate(id: String, body: Map<String, Any?>): MensajeContacto = api.adminUpdate(id, body)
    suspend fun adminDelete(id: String): Map<String, String> = api.adminDelete(id)
}

