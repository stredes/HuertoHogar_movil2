package com.example.huertohogar_mobil.data

import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.Solicitud
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.model.UnreadCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SocialRepository {
    val currentUser: StateFlow<User?>
    val connectedPeers: StateFlow<Set<String>>
    val searchResults: StateFlow<List<User>>

    fun getAmigos(): Flow<List<User>>
    fun getActiveChats(): Flow<List<User>>
    fun getSolicitudesPendientes(): Flow<List<Solicitud>>
    fun getConversacion(amigoId: Int): Flow<List<MensajeChat>>
    fun getChatFriendStatus(amigoId: Int): Flow<Boolean>
    
    // Nuevo: Contador de no leídos
    fun getUnreadCounts(): Flow<Map<Int, Int>>

    suspend fun setCurrentUser(email: String)
    suspend fun buscarPersonas(query: String)
    suspend fun enviarSolicitudAmistad(destinatario: User)
    suspend fun aceptarSolicitud(solicitud: Solicitud)
    suspend fun rechazarSolicitud(solicitud: Solicitud)
    suspend fun enviarMensaje(destinatarioId: Int, contenido: String, tipoContenido: String)
    
    // Nuevas funciones para la arquitectura de chat
    fun subscribeToChatMessages(friendId: Int)
    fun unsubscribeActiveChat()
    
    fun solicitarSincronizacionManual(email: String)
    
    fun onAppResume()
    fun onAppPause()
    fun cleanup()
}
