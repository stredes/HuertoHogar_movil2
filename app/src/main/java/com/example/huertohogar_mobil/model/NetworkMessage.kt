package com.example.huertohogar_mobil.model

// Removemos la dependencia de kotlinx.serialization ya que estamos usando JSONObject
// y esta librería no estaba configurada en el proyecto.

data class NetworkMessage(
    val senderEmail: String,
    val content: String,
    val timestamp: Long
)
