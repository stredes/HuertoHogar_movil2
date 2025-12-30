package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.data.SocialRepository
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.Solicitud
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.model.UnreadCount
import com.example.huertohogar_mobil.network.api.SocialApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteSocialRepository @Inject constructor(
    private val api: SocialApi
) : SocialRepository {
    override val currentUser: StateFlow<User?> = MutableStateFlow(null)
    override val connectedPeers: StateFlow<Set<String>> = MutableStateFlow(emptySet())
    override val searchResults: StateFlow<List<User>> = MutableStateFlow(emptyList())

    override fun getAmigos(): Flow<List<User>> = flow {
        val solicitudes = api.friends()
        // Mapear Solicitud a User usando senderName y senderEmail
        val users = solicitudes.map {
            User(name = it.senderName, email = it.senderEmail)
        }
        emit(users)
    }
    override fun getActiveChats(): Flow<List<User>> = flow { emit(emptyList()) }
    override fun getSolicitudesPendientes(): Flow<List<Solicitud>> = flow { emit(emptyList()) }
    override fun getConversacion(amigoId: Int): Flow<List<MensajeChat>> = flow { emit(emptyList()) }
    override fun getChatFriendStatus(amigoId: Int): Flow<Boolean> = flow { emit(false) }
    override fun getUnreadCounts(): Flow<Map<Int, Int>> = flow { emit(emptyMap()) }
    override suspend fun setCurrentUser(email: String) { /* Implementación vacía */ }
    override suspend fun buscarPersonas(query: String) { /* Implementación vacía */ }
    override suspend fun enviarSolicitudAmistad(destinatario: User) { /* Implementación vacía */ }
    override suspend fun aceptarSolicitud(solicitud: Solicitud) { /* Implementación vacía */ }
    override suspend fun rechazarSolicitud(solicitud: Solicitud) { /* Implementación vacía */ }
    override suspend fun eliminarAmigo(amigo: User) { /* Implementación vacía */ }
    override suspend fun enviarMensaje(destinatarioId: Int, contenido: String, tipoContenido: String) { /* Implementación vacía */ }
    override fun subscribeToChatMessages(friendId: Int) { /* Implementación vacía */ }
    override fun unsubscribeActiveChat() { /* Implementación vacía */ }
    override fun solicitarSincronizacionManual(email: String) { /* Implementación vacía */ }
    override fun onAppResume() { /* Implementación vacía */ }
    override fun onAppPause() { /* Implementación vacía */ }
    override fun cleanup() { /* Implementación vacía */ }

    // Métodos existentes
    suspend fun createRequest(email: String): Solicitud = api.createRequest(mapOf("email" to email))
    suspend fun incoming(): List<Solicitud> = api.incoming()
    suspend fun outgoing(): List<Solicitud> = api.outgoing()
    suspend fun accept(id: String): Solicitud = api.accept(id)
    suspend fun reject(id: String): Solicitud = api.reject(id)
    suspend fun friends(): List<Solicitud> = api.friends()
}
