package com.example.huertohogar_mobil.data

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.huertohogar_mobil.MainActivity
import com.example.huertohogar_mobil.R
import com.example.huertohogar_mobil.model.EstadoMensaje
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.Solicitud
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.model.CarritoItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import com.example.huertohogar_mobil.model.Amistad
import com.example.huertohogar_mobil.model.Producto
import java.nio.charset.StandardCharsets

@Singleton
class P2pManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val socialDao: SocialDao,
    private val userDao: UserDao,
    private val solicitudDao: SolicitudDao,
    private val carritoDao: CarritoDao,
    private val productoDao: ProductoDao
) {
    private val TAG = "HuertoP2P"
    private val SERVICE_TYPE = "_huerto_chat._tcp."
    private var serviceName = "HuertoUser"
    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    
    private val discoveredUsers = ConcurrentHashMap<String, InetAddress>()
    private val discoveredServicesPorts = ConcurrentHashMap<String, Int>()
    
    private var serverSocket: ServerSocket? = null
    private var localPort = 0 
    
    private var isInitialized = false
    var currentUserEmail: String? = null
        private set
    
    private val pendingResolutionQueue = ConcurrentLinkedQueue<NsdServiceInfo>()
    private val isResolving = AtomicBoolean(false)

    private val _connectedPeers = MutableStateFlow<Set<String>>(emptySet())
    val connectedPeers: StateFlow<Set<String>> = _connectedPeers.asStateFlow()

    fun initialize(userEmail: String) {
        if (isInitialized) return
        currentUserEmail = userEmail
        
        try {
            val safeName = userEmail.replace("@", "-at-").replace(".", "-dot-")
            serviceName = "Huerto-$safeName"
            
            nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
            
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            multicastLock = wifiManager.createMulticastLock("HuertoP2PLock").apply { setReferenceCounted(true) }
            
            CoroutineScope(Dispatchers.IO).launch {
                startServer()
                isInitialized = true
                withContext(Dispatchers.Main) { resume() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar P2P", e)
        }
    }

    fun pause() {
        try {
            multicastLock?.takeIf { it.isHeld }?.release()
            discoveryListener?.let { nsdManager?.stopServiceDiscovery(it) }
            registrationListener?.let { nsdManager?.unregisterService(it) }
            discoveryListener = null
            registrationListener = null
        } catch (e: Exception) {
            Log.e(TAG, "Error al pausar P2P", e)
        }
    }

    fun resume() {
        if (!isInitialized || localPort == 0) return
        CoroutineScope(Dispatchers.Main).launch {
            try {
                multicastLock?.takeIf { !it.isHeld }?.acquire()
                if (registrationListener == null) registerService(localPort)
                if (discoveryListener == null) startDiscovery()
            } catch (e: Exception) {
                Log.e(TAG, "Error al reanudar P2P", e)
            }
        }
    }

    private fun startServer() {
        try {
            serverSocket = ServerSocket(0).also { 
                localPort = it.localPort 
            }
            CoroutineScope(Dispatchers.IO).launch {
                while (serverSocket?.isClosed == false) {
                    try {
                        serverSocket?.accept()?.let { handleIncomingMessage(it) }
                    } catch (e: Exception) {
                        if (serverSocket?.isClosed == false) {
                            Log.e(TAG, "Error aceptando conexión", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando servidor", e)
        }
    }

    private fun handleIncomingMessage(socket: Socket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val jsonStr = reader.readLine() ?: return@launch
                
                val json = JSONObject(jsonStr)
                val type = json.optString("type", "CHAT")
                val senderEmail = json.getString("senderEmail")
                val senderName = json.optString("senderName", "Usuario P2P")
                val receiverEmail = json.getString("receiverEmail")
                
                var sender = userDao.getUserByEmail(senderEmail)
                // Si es un usuario desconocido pero valido, lo registramos temporalmente para que funcione el chat
                if (sender == null) {
                    val newUser = User(name = senderName, email = senderEmail, passwordHash = "p2p_guest")
                    userDao.insertUser(newUser)
                    sender = userDao.getUserByEmail(senderEmail)
                }
                
                if (sender != null) {
                    when (type) {
                        "CHAT" -> {
                            val content = json.getString("content")
                            val receiver = userDao.getUserByEmail(receiverEmail)
                            if (receiver != null) {
                                val msgObj = MensajeChat(remitenteId = sender.id, destinatarioId = receiver.id, contenido = content, estado = EstadoMensaje.RECIBIDO)
                                socialDao.insertMensaje(msgObj)
                                showNewMessageNotification(sender, content)
                            }
                        }
                        "FRIEND_REQUEST" -> {
                             handleFriendRequest(senderName, senderEmail, receiverEmail)
                        }
                        "REQUEST_ACCEPTED" -> {
                             handleRequestAccepted(sender, receiverEmail)
                        }
                        "SYNC_REQUEST" -> {
                            handleSyncRequest(sender, receiverEmail)
                        }
                        "SYNC_RESPONSE" -> {
                            val chats = json.optJSONArray("chats")
                            val cart = json.optJSONArray("cart")
                            handleSyncResponse(sender, chats, cart)
                        }
                        "ADMIN_SYNC_DATA" -> {
                            handleAdminSyncData(json.optJSONArray("users"), json.optJSONArray("products"), senderEmail)
                        }
                        "UPSERT_PRODUCT" -> {
                             val productJson = json.getJSONObject("product")
                             handleProductUpsert(productJson)
                        }
                        "DELETE_PRODUCT" -> {
                             val productId = json.getString("productId")
                             handleProductDelete(productId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error procesando mensaje entrante", e)
            } finally {
                socket.close()
            }
        }
    }

    private suspend fun handleFriendRequest(senderName: String, senderEmail: String, receiverEmail: String) {
        Log.d(TAG, "🔔 Procesando solicitud de amistad: De $senderEmail Para $receiverEmail")
        val existing = solicitudDao.getSolicitud(senderEmail, receiverEmail)
        if (existing == null) {
            val solicitud = Solicitud(
                senderName = senderName,
                senderEmail = senderEmail,
                receiverEmail = receiverEmail,
                estado = "PENDIENTE"
            )
            solicitudDao.insertSolicitud(solicitud)
            showNotification("Nueva solicitud de amistad", "$senderName quiere ser tu amigo")
            Log.d(TAG, "✅ Solicitud guardada en BD")
        } else {
            Log.d(TAG, "⚠️ Solicitud ya existente o procesada")
        }
    }

    private suspend fun handleRequestAccepted(sender: User, myEmail: String) {
        val me = userDao.getUserByEmail(myEmail) ?: return
        
        // Verificar si ya somos amigos
        // La query en SocialDao es algo compleja, pero podemos insertar y usar IGNORE
        socialDao.agregarAmigo(Amistad(me.id, sender.id))
        socialDao.agregarAmigo(Amistad(sender.id, me.id))
        
        showNotification("Solicitud Aceptada", "${sender.name} aceptó tu solicitud de amistad")
    }

    private suspend fun handleSyncRequest(requester: User, myEmail: String) {
         val me = userDao.getUserByEmail(myEmail) ?: return
         
         // Verificamos si soy yo mismo en otro dispositivo
         val isSelf = requester.email == me.email

         val chatsArray = JSONArray()
         
         if (isSelf) {
             // Si soy yo, sincronizo TODO el historial de chats (Backup)
             val usersMap = userDao.getAllUsersSync().associateBy { it.id }
             val allMessages = socialDao.getAllMensajesSync()
             
             allMessages.forEach { msg ->
                 val senderUser = usersMap[msg.remitenteId]
                 val receiverUser = usersMap[msg.destinatarioId]
                 
                 if (senderUser != null && receiverUser != null) {
                     val msgJson = JSONObject().apply {
                         put("content", msg.contenido)
                         put("timestamp", msg.timestamp)
                         put("senderEmail", senderUser.email)
                         put("senderName", senderUser.name)
                         put("receiverEmail", receiverUser.email)
                         put("receiverName", receiverUser.name)
                         put("isFullSync", true)
                     }
                     chatsArray.put(msgJson)
                 }
             }
         } else {
             // Si es un amigo, solo sincronizo NUESTRA conversacion
             val mensajes = socialDao.getConversacionSync(me.id, requester.id)
             mensajes.forEach { msg ->
                 val msgJson = JSONObject().apply {
                     put("content", msg.contenido)
                     put("timestamp", msg.timestamp)
                     put("isFromMe", msg.remitenteId == me.id)
                 }
                 chatsArray.put(msgJson)
             }
         }
         
         val cartArray = JSONArray()
         if (isSelf) {
             // Enviar items del carrito
             val items = carritoDao.getCarritoSync()
             items.forEach { item ->
                 val itemJson = JSONObject().apply {
                     put("productId", item.productoId)
                     put("qty", item.cantidad)
                 }
                 cartArray.put(itemJson)
             }
         }
         
         val responseJson = JSONObject().apply {
             put("type", "SYNC_RESPONSE")
             put("senderName", me.name)
             put("senderEmail", me.email)
             put("receiverEmail", requester.email)
             put("chats", chatsArray)
             if (cartArray.length() > 0) {
                 put("cart", cartArray)
             }
         }
         
         sendMessageJson(requester.email, responseJson)
    }

    private suspend fun handleSyncResponse(sender: User, chats: JSONArray?, cart: JSONArray?) {
        val me = currentUserEmail?.let { userDao.getUserByEmail(it) } ?: return

        // Procesar carrito
        if (cart != null && cart.length() > 0) {
            for (i in 0 until cart.length()) {
                val itemJson = cart.getJSONObject(i)
                val pId = itemJson.getString("productId")
                val qty = itemJson.getInt("qty")
                carritoDao.insertItem(CarritoItem(pId, qty))
            }
        }

        // Procesar chats
        if (chats != null) {
            for (i in 0 until chats.length()) {
                val chatJson = chats.getJSONObject(i)
                
                val isFullSync = chatJson.optBoolean("isFullSync", false)
                val content = chatJson.getString("content")
                val timestamp = chatJson.getLong("timestamp")
                
                var remitenteId = 0
                var destinatarioId = 0
                
                if (isFullSync) {
                    val senderEmailMsg = chatJson.getString("senderEmail")
                    val senderNameMsg = chatJson.getString("senderName")
                    val receiverEmailMsg = chatJson.getString("receiverEmail")
                    val receiverNameMsg = chatJson.optString("receiverName", "User")
                    
                    remitenteId = resolveUserId(senderEmailMsg, senderNameMsg)
                    destinatarioId = resolveUserId(receiverEmailMsg, receiverNameMsg)
                } else {
                    // Friend Sync Logic
                    remitenteId = if (chatJson.getBoolean("isFromMe")) sender.id else me.id
                    destinatarioId = if (chatJson.getBoolean("isFromMe")) me.id else sender.id
                }
                
                val exists = socialDao.existeMensaje(remitenteId, destinatarioId, timestamp, content)
                if (!exists) {
                     val msg = MensajeChat(
                         remitenteId = remitenteId,
                         destinatarioId = destinatarioId,
                         contenido = content,
                         timestamp = timestamp,
                         estado = EstadoMensaje.LEIDO 
                     )
                     socialDao.insertMensaje(msg)
                }
            }
        }
    }
    
    private suspend fun resolveUserId(email: String, name: String): Int {
        var user = userDao.getUserByEmail(email)
        if (user == null) {
            val newUser = User(name = name, email = email, passwordHash = "imported")
            userDao.insertUser(newUser)
            user = userDao.getUserByEmail(email)
        }
        return user?.id ?: 0
    }
    
    private suspend fun handleAdminSyncData(usersArray: JSONArray?, productsArray: JSONArray?, senderEmail: String) {
        // Sync Usuarios
        if (usersArray != null) {
            val incomingEmails = mutableSetOf<String>()
            for (i in 0 until usersArray.length()) {
                val userJson = usersArray.getJSONObject(i)
                val email = userJson.getString("email")
                val name = userJson.getString("name")
                val role = userJson.getString("role")
                val hash = userJson.getString("passwordHash")
                
                incomingEmails.add(email)
                
                val existing = userDao.getUserByEmail(email)
                if (existing == null) {
                    userDao.insertUser(User(name = name, email = email, role = role, passwordHash = hash))
                    Log.d(TAG, "Sincronizado usuario nuevo: $email")
                } else {
                    // FORCE UPDATE to ensure password persistence regardless of current state
                    val updatedUser = existing.copy(role = role, name = name, passwordHash = hash)
                    userDao.insertUser(updatedUser)
                    Log.d(TAG, "Usuario actualizado (Force Update): $email con hash de longitud ${hash.length}")
                }
            }
            
            // Eliminar usuarios locales SOLO si el sender es ROOT
            if (senderEmail == "root") {
                val localUsers = userDao.getAllUsersSync()
                for (user in localUsers) {
                    if (user.role != "root" && user.email !in incomingEmails) {
                        userDao.deleteUser(user.id)
                        Log.d(TAG, "🗑️ Usuario eliminado por sync de ROOT: ${user.email}")
                    }
                }
            }
        }
        
        // Sync Productos
        if (productsArray != null) {
            val incomingIds = mutableSetOf<String>()
            for (i in 0 until productsArray.length()) {
                val prodJson = productsArray.getJSONObject(i)
                val id = prodJson.getString("id")
                val nombre = prodJson.getString("nombre")
                val precio = prodJson.getInt("precio")
                val unidad = prodJson.getString("unidad")
                val desc = prodJson.getString("descripcion")
                val imgRes = prodJson.getInt("imagenRes")
                val imgUri = prodJson.optString("imagenUri").takeIf { it.isNotEmpty() && it != "null" }
                
                incomingIds.add(id)
                
                val product = Producto(id, nombre, precio, unidad, desc, imgRes, imgUri)
                productoDao.insert(product)
                Log.d(TAG, "Sincronizado producto: $nombre")
            }
            
            // Eliminar productos locales SOLO si el sender es ROOT
            if (senderEmail == "root") {
                val localProducts = productoDao.getAllProductosSync()
                for (prod in localProducts) {
                    if (prod.id !in incomingIds) {
                        productoDao.delete(prod)
                        Log.d(TAG, "🗑️ Producto eliminado por sync de ROOT: ${prod.nombre}")
                    }
                }
            }
        }
    }
    
    private suspend fun handleProductUpsert(prodJson: JSONObject) {
        val id = prodJson.getString("id")
        val nombre = prodJson.getString("nombre")
        val precio = prodJson.getInt("precio")
        val unidad = prodJson.getString("unidad")
        val desc = prodJson.getString("descripcion")
        val imgRes = prodJson.getInt("imagenRes")
        val imgUri = prodJson.optString("imagenUri").takeIf { it.isNotEmpty() && it != "null" }
        
        val product = Producto(id, nombre, precio, unidad, desc, imgRes, imgUri)
        productoDao.insert(product)
        Log.d(TAG, "📦 Producto recibido (Upsert): $nombre")
    }
    
    private suspend fun handleProductDelete(productId: String) {
        val allProducts = productoDao.getAllProductosSync()
        val target = allProducts.find { it.id == productId }
        if (target != null) {
            productoDao.delete(target)
            Log.d(TAG, "🗑️ Producto eliminado (Delete Event): ${target.nombre}")
        }
    }

    private suspend fun sendMessageJson(receiverEmail: String, json: JSONObject): Boolean {
        val targetIp = discoveredUsers[receiverEmail]
        val targetPort = discoveredServicesPorts[receiverEmail]
        if (targetIp == null || targetPort == null) return false

        return withContext(Dispatchers.IO) {
            try {
                Socket(targetIp, targetPort).use { socket ->
                    PrintWriter(socket.getOutputStream(), true).use { writer ->
                        writer.println(json.toString())
                    }
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando JSON P2P", e)
                false
            }
        }
    }

    private fun showNewMessageNotification(sender: User, content: String) {
        showNotification(sender.name, content)
    }
    
    private fun showNotification(title: String, content: String) {
        val builder = NotificationCompat.Builder(context, "HUERTO_CHANNEL_ID")
            .setSmallIcon(R.drawable.icono)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    private fun registerService(port: Int) {
        try {
            val serviceInfo = NsdServiceInfo().apply {
                serviceName = this@P2pManager.serviceName
                serviceType = SERVICE_TYPE
                setPort(port)
                // ADDED: Set attributes for robust email discovery
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    currentUserEmail?.let { email ->
                         setAttribute("email", email)
                    }
                }
            }
            registrationListener = object : NsdManager.RegistrationListener { 
                override fun onServiceRegistered(info: NsdServiceInfo) { serviceName = info.serviceName } 
                override fun onRegistrationFailed(info: NsdServiceInfo, code: Int) { registrationListener = null } 
                override fun onServiceUnregistered(info: NsdServiceInfo) {} 
                override fun onUnregistrationFailed(info: NsdServiceInfo, code: Int) {}
            }
            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error registrando servicio", e)
        }
    }

    private fun startDiscovery() {
         try {
            discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(type: String) {}
                override fun onServiceFound(service: NsdServiceInfo) {
                    if (service.serviceType != SERVICE_TYPE || service.serviceName == serviceName) return
                    pendingResolutionQueue.add(service)
                    processPendingResolutions()
                }
                override fun onServiceLost(service: NsdServiceInfo) {
                     // Check attributes if available, otherwise parse name
                     // Note: serviceLost usually doesn't give attributes. We have to rely on name map or remove all matches?
                     // But simpler: just remove by name parsing as fallback or iterate discoveredUsers
                     
                     val cleanName = service.serviceName.replace(Regex("\\s*\\(\\d+\\)$"), "")
                     val email = cleanName.replace("Huerto-", "").replace("-at-", "@").replace("-dot-", ".")
                     
                     // Also check our map if we stored it differently? 
                     // For now, removing by parsed email is the best bet since we can't get attributes here.
                     
                     discoveredUsers.remove(email)
                     discoveredServicesPorts.remove(email)
                     updateConnectedPeers()
                }
                override fun onDiscoveryStopped(type: String) {}
                override fun onStartDiscoveryFailed(type: String, code: Int) { discoveryListener = null }
                override fun onStopDiscoveryFailed(type: String, code: Int) {}
            }
            nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando descubrimiento", e)
        }
    }

    private fun processPendingResolutions() {
        if (isResolving.getAndSet(true)) return
        val service = pendingResolutionQueue.poll() ?: run { isResolving.set(false); return }
        
        nsdManager?.resolveService(service, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                isResolving.set(false)
                processPendingResolutions()
            }
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                try {
                    // Try to get email from attributes first (Robust method)
                    var email: String? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val attrs = serviceInfo.attributes
                        if (attrs != null && attrs.containsKey("email")) {
                            val bytes = attrs["email"]
                            if (bytes != null) {
                                email = String(bytes, StandardCharsets.UTF_8)
                            }
                        }
                    }
                    
                    // Fallback to name parsing
                    if (email == null) {
                        val cleanName = serviceInfo.serviceName.replace(Regex("\\s*\\(\\d+\\)$"), "")
                        email = cleanName.replace("Huerto-", "").replace("-at-", "@").replace("-dot-", ".")
                    }

                    if (email != null) {
                        serviceInfo.host?.let { 
                            discoveredUsers[email!!] = it
                            discoveredServicesPorts[email!!] = serviceInfo.port
                            updateConnectedPeers()
                            Log.d(TAG, "Peer resuelto: $email @ ${it.hostAddress}:${serviceInfo.port}")
                        }
                    }
                } finally {
                    isResolving.set(false)
                    processPendingResolutions()
                }
            }
        })
    }

    private fun updateConnectedPeers() {
        _connectedPeers.value = discoveredUsers.keys.toSet()
    }

    suspend fun sendMessage(senderName: String, senderEmail: String, receiverEmail: String, content: String, type: String = "CHAT"): Boolean {
        val targetIp = discoveredUsers[receiverEmail]
        val targetPort = discoveredServicesPorts[receiverEmail]
        
        if (targetIp == null || targetPort == null) {
            Log.w(TAG, "❌ Intento de envío fallido a $receiverEmail. Usuario no descubierto.")
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                Socket(targetIp, targetPort).use { socket ->
                    PrintWriter(socket.getOutputStream(), true).use { writer ->
                        val json = JSONObject().apply {
                            put("type", type)
                            put("senderName", senderName)
                            put("senderEmail", senderEmail)
                            put("receiverEmail", receiverEmail)
                            put("content", content)
                            put("timestamp", System.currentTimeMillis())
                        }
                        writer.println(json.toString())
                    }
                }
                Log.d(TAG, "✅ Mensaje enviado a $receiverEmail (Tipo: $type)")
                true
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error enviando mensaje P2P a $receiverEmail", e)
                false
            }
        }
    }
    
    // Nueva función pública para enviar JSON directo (usada por RootViewModel)
    suspend fun sendMessageJsonDirect(receiverEmail: String, json: JSONObject): Boolean {
        return sendMessageJson(receiverEmail, json)
    }
    
    fun tearDown() {
        try {
            pause()
            serverSocket?.close()
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error en tearDown", e)
        }
    }

    fun solicitarSincronizacion(peerEmail: String) {
        val myEmail = currentUserEmail ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
             val myUser = userDao.getUserByEmail(myEmail) 
             if (myUser != null) {
                 sendMessage(
                     senderName = myUser.name,
                     senderEmail = myEmail,
                     receiverEmail = peerEmail,
                     content = "SYNC",
                     type = "SYNC_REQUEST"
                 )
             }
        }
    }
}
