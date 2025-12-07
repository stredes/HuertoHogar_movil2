package com.example.huertohogar_mobil.viewmodel

import android.util.Log
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
    private val p2pManager: P2pManager
) : ViewModel() {

    private val TAG = "SocialVM"
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

    init {
        viewModelScope.launch {
            connectedPeers.collect { peers ->
                Log.d(TAG, "👥 Conectados actualizados: $peers")
                val me = _currentUser.value
                if (me != null) {
                    peers.forEach { peerEmail ->
                        // Auto-Sincronizar con mis otros dispositivos (si me encuentro a mi mismo)
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
                        val enviado = p2pManager.sendMessage(remitente.name, remitente.email, destinatario.email, msg.contenido)
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
            }
        }
    }

    fun onPermissionsGranted(email: String) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                _currentUser.value = user
                p2pManager.initialize(user.email)
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
            // Buscamos usuarios en la BD local (incluyendo administradores y usuarios)
            val localResults = socialDao.buscarUsuarios(user.id, query)
            
            // Buscamos usuarios en la red P2P (filtrando el usuario actual)
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
            // Fix: Asegurarse de que el destinatario existe en la BD local antes de enviar la solicitud si no existía
            if (destinatario.id == 0) { // Si es un usuario "Wi-Fi" (no guardado)
                 // Guardar usuario temporalmente para poder referenciarlo
                 val tempUser = destinatario.copy(passwordHash = "p2p_guest")
                 userDao.insertUser(tempUser)
            }
            
            // Intentar enviar la solicitud
            val success = p2pManager.sendMessage(
                senderName = user.name,
                senderEmail = user.email,
                receiverEmail = destinatario.email,
                content = "Hola, quiero ser tu amigo",
                type = "FRIEND_REQUEST"
            )

            if (success) {
                // Feedback visual: Si se envía correctamente, podemos quitarlo de la lista o marcarlo.
                // Por ahora lo quitamos para simular "enviado".
                _searchResults.value = _searchResults.value.filter { it.email != destinatario.email }
            } else {
                Log.e(TAG, "Error al enviar solicitud de amistad a ${destinatario.email}")
                // Aquí se podría mostrar un mensaje de error en la UI si tuviéramos un estado de UI para errores
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
                // Fix: Añadir lógica para evitar duplicados en la tabla de amistad si ya existe
                socialDao.agregarAmigo(Amistad(user.id, senderUser.id))
                socialDao.agregarAmigo(Amistad(senderUser.id, user.id))

                // Enviar confirmación al solicitante
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

    fun enviarMensaje(destinatarioId: Int, texto: String, tipoContenido: String = TipoContenido.TEXTO) {
        val user = _currentUser.value ?: return
        // No permitir enviar vacío si es texto, pero sí si es otro tipo (aunque el texto sea vacío, la lógica puede ir en el tipo)
        if (texto.isBlank() && tipoContenido == TipoContenido.TEXTO) return
        
        viewModelScope.launch {
            // 1. Guardamos el mensaje localmente con estado ENVIANDO
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

            // 2. Intentamos enviar el mensaje
            if (destinatario != null) {
                // Aquí deberíamos ajustar sendMessage para soportar tipos de contenido si P2PManager lo soportara
                // Por ahora enviamos texto plano, pero podríamos serializar JSON si P2PManager lo permite.
                // Asumimos que el contenido ya es una representación enviada (texto o URI/Path)
                enviado = p2pManager.sendMessage(user.name, user.email, destinatario.email, texto)
            } 
            
            // 3. Actualizamos el estado final
            val estadoFinal = if (enviado) EstadoMensaje.ENVIADO else EstadoMensaje.ERROR
            socialDao.updateEstado(mensajeId, estadoFinal)
        }
    }

    fun solicitarSincronizacionManual(email: String) {
        p2pManager.solicitarSincronizacion(email)
    }

    // Funciones placeholders para adjuntos
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
        p2pManager.tearDown()
    }
}
