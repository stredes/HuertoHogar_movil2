package com.example.huertohogar_mobil.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.FirebaseRepository
import com.example.huertohogar_mobil.data.P2pManager
import com.example.huertohogar_mobil.data.SocialDao
import com.example.huertohogar_mobil.data.SolicitudDao
import com.example.huertohogar_mobil.data.UserDao
import com.example.huertohogar_mobil.model.Amistad
import com.example.huertohogar_mobil.model.EstadoMensaje
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
    private val socialDao: SocialDao,
    private val userDao: UserDao,
    private val solicitudDao: SolicitudDao,
    private val p2pManager: P2pManager,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val TAG = "SocialVM"
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    val connectedPeers: StateFlow<Set<String>> = p2pManager.connectedPeers

    private val _currentChatFriendId = MutableStateFlow<Int?>(null)

    // Flujo para el estado de conexión del amigo actual en el chat
    val chatFriendStatus: StateFlow<Boolean> = _currentChatFriendId
        .filterNotNull()
        .flatMapLatest { friendId ->
            val friend = userDao.getUserById(friendId)
            if (friend != null) {
                firebaseRepository.observeUserStatus(friend.email)
            } else {
                flowOf(false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    val amigos: StateFlow<List<User>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> socialDao.getAmigos(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Nuevo: Chats Activos (Usuarios con los que se ha hablado, sean amigos o no)
    val activeChats: StateFlow<List<User>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> socialDao.getUsuariosConChat(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val solicitudesPendientes: StateFlow<List<Solicitud>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> solicitudDao.getSolicitudesPendientes(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults = _searchResults.asStateFlow()
    
    val chatMessages: StateFlow<List<MensajeChat>> = combine(_currentUser, _currentChatFriendId) { user, friendId ->
        Pair(user, friendId)
    }.flatMapLatest { (user, friendId) ->
        if (user != null && friendId != null) {
            socialDao.getConversacion(user.id, friendId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            connectedPeers.collect { peers ->
                Log.d(TAG, "👥 Conectados actualizados: $peers")
                val me = _currentUser.value
                if (me != null) {
                    peers.forEach { peerEmail ->
                        if (peerEmail == me.email) {
                            Log.d(TAG, "🔄 Encontrado mi otro dispositivo. Iniciando Sync...")
                            p2pManager.solicitarSincronizacion(peerEmail)
                        }
                        
                        val amigo = userDao.getUserByEmail(peerEmail)
                        if (amigo != null) {
                            reenviarPendientes(me, amigo)
                        }
                    }
                }
            }
        }
    }
    
    private fun reenviarPendientes(remitente: User, destinatario: User) {
        viewModelScope.launch {
            try {
                val pendientes = socialDao.getMensajesPendientes(remitente.id, destinatario.id)
                if (pendientes.isNotEmpty()) {
                    Log.d(TAG, "📨 Encontrados ${pendientes.size} mensajes pendientes para ${destinatario.email}")
                    pendientes.forEach { msg ->
                        var enviado = p2pManager.sendMessage(remitente.name, remitente.email, destinatario.email, msg.contenido)
                        
                        if (!enviado) {
                             enviado = firebaseRepository.sendMessage(remitente, destinatario.email, msg.contenido)
                        }

                        if (enviado) {
                            socialDao.updateEstado(msg.id, EstadoMensaje.ENVIADO)
                        }
                    }
                }
            } catch (e: Exception) {
                 Log.e(TAG, "Error al reenviar pendientes", e)
            }
        }
    }

    fun setCurrentUser(email: String) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                _currentUser.value = user
                p2pManager.initialize(user.email)
                firebaseRepository.initialize(user.email)
            }
        }
    }

    fun onPermissionsGranted(email: String) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                _currentUser.value = user
                p2pManager.initialize(user.email)
                firebaseRepository.initialize(user.email)
            }
        }
    }
    
    fun onAppResume() {
        p2pManager.resume()
    }
    
    fun onAppPause() {
        p2pManager.pause()
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
                else User(id = 0, name = "Usuario Wi-Fi", email = email, passwordHash = "p2p_guest")
            }.filterNotNull()

            _searchResults.value = localResults + p2pResults
        }
    }

    fun enviarSolicitudAmistad(destinatario: User) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (destinatario.id == 0) {
                 val tempUser = destinatario.copy(passwordHash = "p2p_guest")
                 userDao.insertUser(tempUser)
            }
            
            var success = p2pManager.sendMessage(
                senderName = user.name,
                senderEmail = user.email,
                receiverEmail = destinatario.email,
                content = "Hola, quiero ser tu amigo",
                type = "FRIEND_REQUEST"
            )
            
            if (!success) {
                success = firebaseRepository.sendMessage(user, destinatario.email, "Hola, quiero ser tu amigo", "FRIEND_REQUEST")
            }

            if (success) {
                _searchResults.value = _searchResults.value.filter { it.email != destinatario.email }
            } else {
                Log.e(TAG, "Error al enviar solicitud de amistad a ${destinatario.email}")
            }
        }
    }

    fun aceptarSolicitud(solicitud: Solicitud) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            solicitudDao.updateEstado(solicitud.id, "ACEPTADA")

            var senderUser = userDao.getUserByEmail(solicitud.senderEmail)
            if (senderUser == null) {
                val newUser = User(name = solicitud.senderName, email = solicitud.senderEmail, passwordHash = "p2p_friend")
                userDao.insertUser(newUser)
                senderUser = userDao.getUserByEmail(solicitud.senderEmail)
            }

            if (senderUser != null) {
                socialDao.agregarAmigo(Amistad(user.id, senderUser.id))
                socialDao.agregarAmigo(Amistad(senderUser.id, user.id))

                var sent = p2pManager.sendMessage(
                    senderName = user.name,
                    senderEmail = user.email,
                    receiverEmail = solicitud.senderEmail,
                    content = "Solicitud aceptada",
                    type = "REQUEST_ACCEPTED"
                )
                
                if (!sent) {
                     firebaseRepository.sendMessage(user, solicitud.senderEmail, "Solicitud aceptada", "REQUEST_ACCEPTED")
                }
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

    fun enviarMensaje(destinatarioId: Int, texto: String, tipoContenido: String = TipoContenido.TEXTO) {
        val user = _currentUser.value ?: return
        if (texto.isBlank() && tipoContenido == TipoContenido.TEXTO) return
        
        viewModelScope.launch {
            val nuevoMensaje = MensajeChat(
                remitenteId = user.id,
                destinatarioId = destinatarioId,
                contenido = texto,
                tipoContenido = tipoContenido,
                estado = EstadoMensaje.ENVIANDO
            )
            val mensajeId = socialDao.insertMensaje(nuevoMensaje)

            val destinatario = userDao.getUserById(destinatarioId)
            var enviado = false

            if (destinatario != null) {
                enviado = p2pManager.sendMessage(user.name, user.email, destinatario.email, texto, 
                                                if (tipoContenido == TipoContenido.TEXTO) "CHAT" else tipoContenido)
                
                if (!enviado) {
                    enviado = firebaseRepository.sendMessage(user, destinatario.email, texto, 
                                                            if (tipoContenido == TipoContenido.TEXTO) "CHAT" else tipoContenido)
                }
            } else {
                 Log.e(TAG, "No se encontró usuario destinatario con ID $destinatarioId para enviar mensaje")
            }
            
            val estadoFinal = if (enviado) EstadoMensaje.ENVIADO else EstadoMensaje.ERROR
            socialDao.updateEstado(mensajeId, estadoFinal)
        }
    }

    fun solicitarSincronizacionManual(email: String) {
        p2pManager.restartDiscovery()
        p2pManager.solicitarSincronizacion(email)
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
        firebaseRepository.cleanup()
    }
}
