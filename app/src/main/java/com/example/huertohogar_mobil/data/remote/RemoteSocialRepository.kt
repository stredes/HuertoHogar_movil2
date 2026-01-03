package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.data.SocialRepository
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.Solicitud
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.model.UnreadCount
import com.example.huertohogar_mobil.network.api.SocialApi
import com.example.huertohogar_mobil.network.dto.CrearSolicitudAmistadRequest
import com.example.huertohogar_mobil.network.mappers.toEntity
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
        val resp = api.friends()
        if (resp.isSuccessful && resp.body() != null) {
            val solicitudes = resp.body()!!.map { it.toEntity() }
            // Mapear Solicitud a User usando senderName y senderEmail
            val users = solicitudes.map {
                User(name = it.senderName, email = it.senderEmail)
            }
            emit(users)
        } else {
            emit(emptyList())
        }
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
    suspend fun createRequest(email: String): Solicitud? {
        val request = CrearSolicitudAmistadRequest(email)
        val resp = api.createRequest(request)
        return if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.toEntity()
        } else null
    }

    suspend fun incoming(): List<Solicitud>? {
        val resp = api.incoming()
        return if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.map { it.toEntity() }
        } else null
    }

    suspend fun outgoing(): List<Solicitud>? {
        val resp = api.outgoing()
        return if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.map { it.toEntity() }
        } else null
    }

    suspend fun accept(id: String): Solicitud? {
        val resp = api.accept(id)
        return if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.toEntity()
        } else null
    }

    suspend fun reject(id: String): Solicitud? {
        val resp = api.reject(id)
        return if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.toEntity()
        } else null
    }

    suspend fun friends(): List<User>? {
        val resp = api.friends()
        return if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.map {
                val solicitud = it.toEntity()
                User(name = solicitud.senderName, email = solicitud.senderEmail)
            }
        } else null
    }
}
