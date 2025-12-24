package com.example.huertohogar_mobil.data

import android.util.Log
import com.example.huertohogar_mobil.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepositoryImpl @Inject constructor(
    private val socialDao: SocialDao,
    private val userDao: UserDao,
    private val firebaseRepository: FirebaseRepository,
    private val p2pManager: P2pManager,
    private val notificationRouter: NotificationRouter
) : SocialRepository {

    private val TAG = "SocialRepository"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser = _currentUser.asStateFlow()

    override val connectedPeers: StateFlow<Set<String>> = p2pManager.connectedPeers

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    override val searchResults = _searchResults.asStateFlow()

    override fun getAmigos(): Flow<List<User>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> socialDao.getAmigos(user.id) }

    override fun getActiveChats(): Flow<List<User>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> socialDao.getUsuariosConChat(user.id) }

    override fun getSolicitudesPendientes(): Flow<List<Solicitud>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> socialDao.getSolicitudesPendientes(user.email) }

    override fun getConversacion(amigoId: Int): Flow<List<MensajeChat>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> socialDao.getConversacion(user.id, amigoId) }

    override fun getChatFriendStatus(amigoId: Int): Flow<Boolean> = flow {
        try {
            val friend = userDao.getUserById(amigoId)
            if (friend != null) {
                emitAll(firebaseRepository.observeUserStatus(friend.email))
            } else {
                emit(false)
            }
        } catch (e: Exception) {
            emit(false)
        }
    }

    override fun getUnreadCounts(): Flow<Map<Int, Int>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> 
            socialDao.getUnreadCounts(user.id)
                .map { list -> list.associate { it.remitenteId to it.total } }
        }

    override suspend fun setCurrentUser(email: String) {
        var user = userDao.getUserByEmail(email)
        
        // FIX: Si el usuario no existe en local (instalación limpia), intentamos obtenerlo de Firebase
        if (user == null) {
            Log.w(TAG, "Usuario $email no encontrado localmente. Intentando recuperar de nube...")
            user = firebaseRepository.getUserDirectly(email)
            if (user != null) {
                userDao.insertUser(user)
                // Volvemos a leer para asegurar consistencia (ID, etc)
                user = userDao.getUserByEmail(email)
            }
        }

        if (user != null) {
            _currentUser.value = user
            p2pManager.initialize(user.email)
            
            // Inicializar siempre Firebase, es clave para recibir mensajes
            firebaseRepository.initialize(user.email)
            
            monitorPeersForSync(user)
        } else {
            // Caso extremo: No hay user local ni en nube (¿error de auth?)
            Log.e(TAG, "No se pudo cargar perfil completo de $email, pero iniciando listeners...")
            firebaseRepository.initialize(email)
        }
    }
    
    private fun monitorPeersForSync(me: User) {
        scope.launch {
            connectedPeers.collect { peers ->
                peers.forEach { peerEmail ->
                    if (peerEmail == me.email) {
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

    private suspend fun reenviarPendientes(remitente: User, destinatario: User) {
        try {
            val pendientes = socialDao.getMensajesPendientes(remitente.id, destinatario.id)
            if (pendientes.isNotEmpty()) {
                pendientes.forEach { msg ->
                    val type = if (msg.tipoContenido == TipoContenido.TEXTO) "CHAT" else msg.tipoContenido
                    
                    // PROTOCOLO: Siempre intentamos enviar por Firebase PRIMERO para garantizar historial
                    // y luego intentamos P2P si está disponible para velocidad local
                    // FIX: Pasar timestamp local para evitar duplicados en listener
                    var enviadoCloud = firebaseRepository.sendMessage(
                        remitente, 
                        destinatario.email, 
                        msg.contenido, 
                        type,
                        msg.timestamp
                    )
                    
                    // Si falla Cloud (offline), intentamos P2P
                    var enviadoP2P = false
                    if (!enviadoCloud) {
                        // FIX: Pasar timestamp original para evitar duplicados en receptor
                        enviadoP2P = p2pManager.sendMessage(
                            remitente.name, 
                            remitente.email, 
                            destinatario.email, 
                            msg.contenido, 
                            type, 
                            msg.timestamp
                        )
                    }

                    if (enviadoCloud || enviadoP2P) {
                        socialDao.updateEstado(msg.id, EstadoMensaje.ENVIADO)
                    }
                }
            }
        } catch (e: Exception) {
             Log.e(TAG, "Error reenviando pendientes", e)
        }
    }

    override suspend fun buscarPersonas(query: String) {
        val user = _currentUser.value ?: return
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        val localResults = socialDao.buscarUsuarios(user.id, query)
        
        // P2P Search secundario
        val p2pEmails = p2pManager.connectedPeers.value.filter { 
            it.contains(query, ignoreCase = true) && it != user.email
        }
        
        val p2pResults = p2pEmails.map { email ->
            if (localResults.any { it.email == email }) null
            else User(id = 0, name = "Usuario Wi-Fi", email = email, passwordHash = "p2p_guest")
        }.filterNotNull()

        _searchResults.value = localResults + p2pResults
    }

    override suspend fun enviarSolicitudAmistad(destinatario: User) {
        val user = _currentUser.value ?: return
        
        var destinatarioFinal = destinatario
        if (destinatario.id == 0) {
             val tempUser = destinatario.copy(passwordHash = "p2p_guest")
             userDao.insertUser(tempUser)
             userDao.getUserByEmail(destinatario.email)?.let { destinatarioFinal = it }
        }
        
        Log.d(TAG, "🔍 Intentando enviar solicitud de ${user.email} a ${destinatarioFinal.email}")

        // ✨ VALIDACIÓN CENTRALIZADA con NotificationRouter
        val canSend = notificationRouter.canSendNotification(
            NotificationRouter.NotificationType.FRIEND_REQUEST,
            user.email,
            destinatarioFinal.email
        )

        if (!canSend) {
            Log.d(TAG, "🚫 NotificationRouter bloqueó la solicitud")
            return
        }

        Log.d(TAG, "✅ NotificationRouter aprobó el envío")

        // PROTOCOLO FIREBASE FIRST: Prioridad Cloud para persistencia
        var success = firebaseRepository.sendMessage(user, destinatarioFinal.email, "Hola, quiero ser tu amigo", "FRIEND_REQUEST")
        
        if (!success) {
            // Fallback P2P
            success = p2pManager.sendMessage(
                senderName = user.name,
                senderEmail = user.email,
                receiverEmail = destinatarioFinal.email,
                content = "Hola, quiero ser tu amigo",
                type = "FRIEND_REQUEST"
            )
        }

        if (success) {
            Log.d(TAG, "✅ Solicitud enviada exitosamente")
            _searchResults.value = _searchResults.value.filter { it.email != destinatarioFinal.email }
        } else {
             Log.e(TAG, "❌ Fallo al enviar solicitud de amistad")
        }
    }

    override suspend fun aceptarSolicitud(solicitud: Solicitud) {
        val user = _currentUser.value ?: return
        
        Log.d(TAG, "✅ Aceptando solicitud de ${solicitud.senderEmail}")

        // 1. Marcar solicitud como ACEPTADA
        socialDao.updateEstadoSolicitud(solicitud.id, "ACEPTADA")
        
        var senderUser = userDao.getUserByEmail(solicitud.senderEmail)
        if (senderUser == null) {
            val newUser = User(name = solicitud.senderName, email = solicitud.senderEmail, passwordHash = "p2p_friend")
            userDao.insertUser(newUser)
            senderUser = userDao.getUserByEmail(solicitud.senderEmail)
        }

        if (senderUser != null) {
            // 2. VERIFICAR QUE NO SEAN YA AMIGOS (por si acaso)
            val yaAmigos = socialDao.esAmigo(user.id, senderUser.id)
            if (yaAmigos) {
                Log.d(TAG, "⚠️ Ya eran amigos, solo limpiando solicitudes...")
                // Limpiar solicitudes duplicadas
                socialDao.deleteSolicitud(solicitud.id)
                socialDao.getSolicitud(user.email, senderUser.email)?.let { socialDao.deleteSolicitud(it.id) }
                socialDao.getSolicitud(senderUser.email, user.email)?.let { socialDao.deleteSolicitud(it.id) }
                return
            }

            // 3. Guardar localmente la amistad (bidireccional)
            socialDao.agregarAmigo(Amistad(user.id, senderUser.id))
            socialDao.agregarAmigo(Amistad(senderUser.id, user.id))
            Log.d(TAG, "✅ Amistad creada localmente")

            // 4. Guardar en Nube (Persistencia)
            firebaseRepository.addFriendInCloud(user.email, senderUser.email)
            Log.d(TAG, "✅ Amistad guardada en la nube")

            // 5. Crear Mensaje de Sistema
            val welcomeMsg = MensajeChat(
                remitenteId = senderUser.id,
                destinatarioId = user.id,
                contenido = "¡Ahora son amigos! Saluda 👋",
                tipoContenido = TipoContenido.TEXTO,
                timestamp = System.currentTimeMillis(),
                estado = EstadoMensaje.RECIBIDO
            )
            socialDao.insertMensaje(welcomeMsg)

            // 6. LIMPIAR TODAS LAS SOLICITUDES ENTRE ESTOS USUARIOS (en ambas direcciones)
            // Esto previene que queden solicitudes residuales que causen duplicados
            Log.d(TAG, "🧹 Limpiando solicitudes entre ${user.email} y ${senderUser.email}")
            socialDao.deleteSolicitud(solicitud.id)
            socialDao.getSolicitud(user.email, senderUser.email)?.let {
                Log.d(TAG, "🧹 Eliminando solicitud inversa ${it.id}")
                socialDao.deleteSolicitud(it.id)
            }
            socialDao.getSolicitud(senderUser.email, user.email)?.let {
                if (it.id != solicitud.id) {
                    Log.d(TAG, "🧹 Eliminando solicitud duplicada ${it.id}")
                    socialDao.deleteSolicitud(it.id)
                }
            }

            // 7. Avisar al otro (FIREBASE FIRST)
            var sent = firebaseRepository.sendMessage(user, solicitud.senderEmail, "Solicitud aceptada", "REQUEST_ACCEPTED")
            
            if (!sent) {
                 p2pManager.sendMessage(
                    senderName = user.name,
                    senderEmail = user.email,
                    receiverEmail = solicitud.senderEmail,
                    content = "Solicitud aceptada",
                    type = "REQUEST_ACCEPTED"
                )
            }

            Log.d(TAG, "✅ Proceso de aceptación completado")
        }
    }

    override suspend fun rechazarSolicitud(solicitud: Solicitud) {
        socialDao.updateEstadoSolicitud(solicitud.id, "RECHAZADA")
    }

    override suspend fun enviarMensaje(destinatarioId: Int, contenido: String, tipoContenido: String) {
        val user = _currentUser.value ?: return
        
        val nuevoMensaje = MensajeChat(
            remitenteId = user.id,
            destinatarioId = destinatarioId,
            contenido = contenido,
            tipoContenido = tipoContenido,
            estado = EstadoMensaje.ENVIANDO
        )
        val mensajeId = socialDao.insertMensaje(nuevoMensaje)

        val destinatario = userDao.getUserById(destinatarioId)
        var enviado = false

        if (destinatario != null) {
            val type = if (tipoContenido == TipoContenido.TEXTO) "CHAT" else tipoContenido
            
            // PROTOCOLO FIREBASE FIRST:
            // 1. Intentamos persistir en la nube (Garantiza historial completo y backup)
            // FIX: Usamos el mismo timestamp que guardamos localmente para que el listener pueda de-duplicar
            enviado = firebaseRepository.sendMessage(
                user, 
                destinatario.email, 
                contenido, 
                type,
                nuevoMensaje.timestamp
            )
            
            // 2. Si hay error en nube (offline), intentamos entrega directa P2P
            if (!enviado) {
                Log.d(TAG, "Fallo envío Cloud, intentando P2P...")
                // FIX: Pasar timestamp explícito para de-duplicación
                enviado = p2pManager.sendMessage(
                    user.name, 
                    user.email, 
                    destinatario.email, 
                    contenido, 
                    type,
                    nuevoMensaje.timestamp
                )
            } else {
                // Opcional: Si el Cloud fue exitoso pero quieres velocidad instantánea P2P,
                // puedes enviar también por P2P como "notificación rápida",
                // pero Firebase Firestore ya es muy rápido.
            }
        }
        
        val estadoFinal = if (enviado) EstadoMensaje.ENVIADO else EstadoMensaje.ERROR
        socialDao.updateEstado(mensajeId, estadoFinal)
    }
    
    override fun subscribeToChatMessages(friendId: Int) {
        scope.launch {
            val friend = userDao.getUserById(friendId)
            if (friend != null) {
                // Suscripción al chat en la nube
                firebaseRepository.subscribeToChatMessages(friend.email)
                
                // IMPORTANTE: Marcar mensajes como leídos localmente al entrar
                val me = _currentUser.value
                if (me != null) {
                    socialDao.markAsRead(friendId, me.id)
                }
            } else {
                Log.e(TAG, "No se puede suscribir al chat: Usuario $friendId no encontrado")
            }
        }
    }

    override fun unsubscribeActiveChat() {
        firebaseRepository.unsubscribeActiveChat()
    }

    override fun solicitarSincronizacionManual(email: String) {
        p2pManager.restartDiscovery()
        p2pManager.solicitarSincronizacion(email)
        // También podríamos forzar un fetch de Firebase si fuera necesario
    }

    override fun onAppResume() {
        p2pManager.resume()
    }

    override fun onAppPause() {
        p2pManager.pause()
    }

    override fun cleanup() {
        firebaseRepository.cleanup()
        p2pManager.pause()
    }
}
