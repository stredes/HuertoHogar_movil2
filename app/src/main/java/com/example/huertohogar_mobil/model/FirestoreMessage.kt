package com.example.huertohogar_mobil.model

/**
 * Modelo de datos optimizado para la persistencia en Firestore.
 * Se usa para la colección "chats_history" y eventualmente reemplazará
 * el uso de HashMaps crudos en el repositorio.
 */
data class FirestoreMessage(
    val id: String = "",
    val chatId: String = "",
    val senderEmail: String = "",
    val senderName: String = "",
    val receiverEmail: String = "",
    val content: String = "",
    val type: String = "TEXTO",
    val timestamp: Long = 0L,
    val participants: List<String> = emptyList()
)
