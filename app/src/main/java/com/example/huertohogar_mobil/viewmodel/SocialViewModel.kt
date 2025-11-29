package com.example.huertohogar_mobil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.P2pManager
import com.example.huertohogar_mobil.data.SocialDao
import com.example.huertohogar_mobil.data.SolicitudDao
import com.example.huertohogar_mobil.data.UserDao
import com.example.huertohogar_mobil.model.Amistad
import com.example.huertohogar_mobil.model.EstadoMensaje
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.Solicitud
import com.example.huertohogar_mobil.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialDao: SocialDao,
    private val userDao: UserDao,
    private val solicitudDao: SolicitudDao,
    private val p2pManager: P2pManager
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    val connectedPeers: StateFlow<Set<String>> = p2pManager.connectedPeers

    val amigos: StateFlow<List<User>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> socialDao.getAmigos(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val solicitudesPendientes: StateFlow<List<Solicitud>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> solicitudDao.getSolicitudesPendientes(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // Chat handling logic fixed to avoid race conditions
    private val _currentChatFriendId = MutableStateFlow<Int?>(null)
    
    val chatMessages: StateFlow<List<MensajeChat>> = combine(_currentUser, _currentChatFriendId) { user, friendId ->
        Pair(user, friendId)
    }.flatMapLatest { (user, friendId) ->
        if (user != null && friendId != null) {
            socialDao.getConversacion(user.id, friendId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCurrentUser(email: String) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                _currentUser.value = user
                p2pManager.initialize(user.email)
            }
        }
    }

    fun buscarPersonas(query: String) {
        val user = _currentUser.value ?: return
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val localResults = socialDao.buscarUsuarios(user.id, query)
            val p2pEmails = p2pManager.connectedPeers.value.filter { 
                it.contains(query, ignoreCase = true) && it != user.email
            }
            
            val p2pResults = p2pEmails.map { email ->
                if (localResults.any { it.email == email }) null
                else User(id = -1, name = "Usuario Wi-Fi", email = email, passwordHash = "")
            }.filterNotNull()

            _searchResults.value = localResults + p2pResults
        }
    }

    fun enviarSolicitudAmistad(destinatario: User) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            // Enviar solicitud P2P
            p2pManager.sendMessage(
                senderName = user.name,
                senderEmail = user.email,
                receiverEmail = destinatario.email,
                content = "Hola, quiero ser tu amigo",
                type = "FRIEND_REQUEST"
            )
            // Podríamos guardar una copia local de "Solicitud enviada" si quisiéramos
            // Por ahora solo limpiamos búsqueda
            _searchResults.value = _searchResults.value.filter { it.email != destinatario.email }
        }
    }

    fun aceptarSolicitud(solicitud: Solicitud) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            // 1. Marcar solicitud como aceptada
            solicitudDao.updateEstado(solicitud.id, "ACEPTADA")

            // 2. Crear amistad local
            // Necesitamos el ID del usuario sender. Si no existe (era P2P puro), lo creamos.
            var senderUser = userDao.getUserByEmail(solicitud.senderEmail)
            if (senderUser == null) {
                val newUser = User(name = solicitud.senderName, email = solicitud.senderEmail, passwordHash = "p2p_friend")
                userDao.insertUser(newUser)
                senderUser = userDao.getUserByEmail(solicitud.senderEmail)
            }

            if (senderUser != null) {
                socialDao.agregarAmigo(Amistad(user.id, senderUser.id))
                socialDao.agregarAmigo(Amistad(senderUser.id, user.id))

                // 3. Notificar al sender que aceptamos
                p2pManager.sendMessage(
                    senderName = user.name,
                    senderEmail = user.email,
                    receiverEmail = solicitud.senderEmail,
                    content = "Solicitud aceptada",
                    type = "REQUEST_ACCEPTED"
                )
            }
        }
    }
    
    fun rechazarSolicitud(solicitud: Solicitud) {
         viewModelScope.launch {
             solicitudDao.updateEstado(solicitud.id, "RECHAZADA")
         }
    }

    fun cargarChat(amigoId: Int) {
        _currentChatFriendId.value = amigoId
    }

    fun enviarMensaje(destinatarioId: Int, texto: String) {
        val user = _currentUser.value ?: return
        if (texto.isBlank()) return
        
        viewModelScope.launch {
            val nuevoMensaje = MensajeChat(
                remitenteId = user.id,
                destinatarioId = destinatarioId,
                contenido = texto,
                estado = EstadoMensaje.ENVIANDO
            )
            val mensajeId = socialDao.insertMensaje(nuevoMensaje)

            val destinatario = userDao.getUserById(destinatarioId)
            var enviado = false
            
            if (destinatario != null) {
                enviado = p2pManager.sendMessage(user.name, user.email, destinatario.email, texto)
            }
            
            val estadoFinal = if (enviado) EstadoMensaje.ENVIADO else EstadoMensaje.ERROR
            socialDao.updateEstado(mensajeId, estadoFinal)
        }
    }

    override fun onCleared() {
        super.onCleared()
        p2pManager.tearDown()
    }
}
