package com.example.huertohogar_mobil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.SocialRepository
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.Solicitud
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.model.TipoContenido
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SocialViewModel @Inject constructor(
    private val repository: SocialRepository
) : ViewModel() {

    val currentUser = repository.currentUser
    val connectedPeers = repository.connectedPeers
    val searchResults = repository.searchResults
    
    // Lista cruda de amigos (solo BD)
    private val _amigos = repository.getAmigos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lista cruda de chats activos (solo BD)
    private val _activeChats = repository.getActiveChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combinación de AMBOS para mostrar en la UI
    val chatsDisplay: StateFlow<List<User>> = combine(_amigos, _activeChats) { amigos, chats ->
        (amigos + chats).distinctBy { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val amigos: StateFlow<List<User>> = _amigos
    val activeChats: StateFlow<List<User>> = _activeChats

    val solicitudesPendientes: StateFlow<List<Solicitud>> = repository.getSolicitudesPendientes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Nuevo: Contador de mensajes no leídos
    val unreadCounts: StateFlow<Map<Int, Int>> = repository.getUnreadCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _currentChatFriendId = MutableStateFlow<Int?>(null)
    
    val chatMessages: StateFlow<List<MensajeChat>> = _currentChatFriendId
        .filterNotNull()
        .flatMapLatest { friendId -> repository.getConversacion(friendId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatFriendStatus: StateFlow<Boolean> = _currentChatFriendId
        .filterNotNull()
        .flatMapLatest { friendId -> repository.getChatFriendStatus(friendId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setCurrentUser(email: String) {
        viewModelScope.launch {
            repository.setCurrentUser(email)
        }
    }

    fun onPermissionsGranted(email: String) {
        setCurrentUser(email)
    }
    
    fun onAppResume() = repository.onAppResume()
    fun onAppPause() = repository.onAppPause()

    fun buscarPersonas(query: String) {
        viewModelScope.launch {
            repository.buscarPersonas(query)
        }
    }

    fun enviarSolicitudAmistad(destinatario: User) {
        viewModelScope.launch {
            repository.enviarSolicitudAmistad(destinatario)
        }
    }

    fun aceptarSolicitud(solicitud: Solicitud) {
        viewModelScope.launch {
            repository.aceptarSolicitud(solicitud)
        }
    }
    
    fun rechazarSolicitud(solicitud: Solicitud) {
        viewModelScope.launch {
            repository.rechazarSolicitud(solicitud)
        }
    }

    // --- NUEVO MANEJO DE CHAT ACTIVO ---
    
    /**
     * Se llama al entrar a la pantalla de chat.
     * Activa el listener en tiempo real de Firestore para ESTE chat específico.
     */
    fun enterChat(amigoId: Int) {
        _currentChatFriendId.value = amigoId
        repository.subscribeToChatMessages(amigoId)
    }

    /**
     * Se llama al salir de la pantalla de chat.
     * Desactiva el listener específico para ahorrar recursos.
     */
    fun leaveChat() {
        _currentChatFriendId.value = null
        repository.unsubscribeActiveChat()
    }

    // Deprecated: Mantener por compatibilidad si es necesario, pero enterChat es mejor
    fun cargarChat(amigoId: Int) {
        enterChat(amigoId)
    }

    fun enviarMensaje(destinatarioId: Int, texto: String, tipoContenido: String = TipoContenido.TEXTO) {
        if (texto.isBlank() && tipoContenido == TipoContenido.TEXTO) return
        viewModelScope.launch {
            repository.enviarMensaje(destinatarioId, texto, tipoContenido)
        }
    }

    fun solicitarSincronizacionManual(email: String) {
        repository.solicitarSincronizacionManual(email)
    }

    fun adjuntarFoto(uri: String, amigoId: Int) {
        enviarMensaje(amigoId, uri, TipoContenido.IMAGEN)
    }

    fun adjuntarVideo(uri: String, amigoId: Int) {
        enviarMensaje(amigoId, uri, TipoContenido.VIDEO)
    }

    fun adjuntarAudio(uri: String, amigoId: Int) {
        enviarMensaje(amigoId, uri, TipoContenido.AUDIO)
    }

    fun adjuntarUbicacion(lat: Double, lng: Double, amigoId: Int) {
        enviarMensaje(amigoId, "$lat,$lng", TipoContenido.UBICACION)
    }

    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}
