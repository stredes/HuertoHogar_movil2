package com.example.huertohogar_mobil.data

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.example.huertohogar_mobil.model.EstadoMensaje
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.Solicitud
import com.example.huertohogar_mobil.model.User
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class P2pManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val socialDao: SocialDao,
    private val userDao: UserDao,
    private val solicitudDao: SolicitudDao
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

    private val _connectedPeers = MutableStateFlow<Set<String>>(emptySet())
    val connectedPeers: StateFlow<Set<String>> = _connectedPeers.asStateFlow()

    fun initialize(userEmail: String) {
        val safeName = userEmail.replace("@", "-at-").replace(".", "-dot-")
        serviceName = "Huerto-$safeName"
        
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        
        // Adquirir MulticastLock para permitir descubrimiento en la red
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        multicastLock = wifiManager.createMulticastLock("HuertoP2PLock").apply {
            setReferenceCounted(true)
            acquire()
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            startServer()
            if (localPort != 0) {
                withContext(Dispatchers.Main) {
                    registerService(localPort)
                    startDiscovery()
                }
            } else {
                Log.e(TAG, "No se pudo iniciar el servidor socket, abortando P2P.")
            }
        }
    }

    private fun startServer() {
        try {
            serverSocket = ServerSocket(0)
            localPort = serverSocket!!.localPort
            Log.d(TAG, "Servidor socket iniciado en puerto: $localPort")

            CoroutineScope(Dispatchers.IO).launch {
                while (serverSocket != null && !serverSocket!!.isClosed) {
                    try {
                        val client = serverSocket!!.accept()
                        handleIncomingMessage(client)
                    } catch (e: Exception) {
                        if (!serverSocket!!.isClosed) {
                            Log.e(TAG, "Error aceptando conexión", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando servidor socket", e)
            localPort = 0
        }
    }

    private fun handleIncomingMessage(socket: Socket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val jsonStr = reader.readLine()
                
                if (jsonStr != null) {
                    Log.d(TAG, "Mensaje recibido: $jsonStr")
                    val json = JSONObject(jsonStr)
                    val type = json.optString("type", "CHAT")
                    val senderEmail = json.getString("senderEmail")
                    val senderName = json.optString("senderName", "Usuario P2P") 
                    val receiverEmail = json.optString("receiverEmail", "")
                    
                    // Asegurar que el remitente existe en BD
                    var sender = userDao.getUserByEmail(senderEmail)
                    if (sender == null) {
                        val newUser = User(
                            name = senderName,
                            email = senderEmail,
                            passwordHash = "p2p_guest"
                        )
                        userDao.insertUser(newUser)
                        sender = userDao.getUserByEmail(senderEmail)
                        Log.d(TAG, "Usuario desconocido $senderEmail registrado automáticamente.")
                    }
                    
                    if (type == "CHAT") {
                        val content = json.getString("content")
                        if (sender != null) {
                             val receiver = userDao.getUserByEmail(receiverEmail)
                             if (receiver != null) {
                                 val msgObj = MensajeChat(
                                     remitenteId = sender.id,
                                     destinatarioId = receiver.id,
                                     contenido = content,
                                     estado = EstadoMensaje.RECIBIDO
                                 )
                                 socialDao.insertMensaje(msgObj)
                                 Log.d(TAG, "Mensaje CHAT guardado en BD local")
                             }
                        }
                    } else if (type == "FRIEND_REQUEST") {
                        val solicitud = Solicitud(
                            senderName = senderName,
                            senderEmail = senderEmail,
                            receiverEmail = receiverEmail,
                            estado = "PENDIENTE"
                        )
                        solicitudDao.insertSolicitud(solicitud)
                        Log.d(TAG, "Solicitud de amistad recibida y guardada")
                    } else if (type == "REQUEST_ACCEPTED") {
                        // Si aceptaron mi solicitud, agregamos la amistad
                        if (sender != null) {
                            val me = userDao.getUserByEmail(receiverEmail) // Yo
                            if (me != null) {
                                val amistad = com.example.huertohogar_mobil.model.Amistad(me.id, sender.id)
                                socialDao.agregarAmigo(amistad)
                                Log.d(TAG, "Solicitud aceptada, amistad creada")
                            }
                        }
                    }
                }
                socket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error procesando mensaje entrante", e)
            }
        }
    }

    private fun registerService(port: Int) {
        try {
            val serviceInfo = NsdServiceInfo().apply {
                serviceName = this@P2pManager.serviceName
                serviceType = SERVICE_TYPE
                setPort(port)
            }

            registrationListener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
                    serviceName = NsdServiceInfo.serviceName
                    Log.d(TAG, "Servicio registrado: $serviceName")
                }
                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.e(TAG, "Fallo registro servicio: $errorCode")
                }
                override fun onServiceUnregistered(arg0: NsdServiceInfo) {}
                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
            }

            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al registrar servicio NSD", e)
        }
    }

    private fun startDiscovery() {
        try {
            discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {
                    Log.d(TAG, "Descubrimiento iniciado")
                }
                override fun onServiceFound(service: NsdServiceInfo) {
                    if (service.serviceType != SERVICE_TYPE) return
                    if (service.serviceName == serviceName) return 

                    Log.d(TAG, "Servicio encontrado: ${service.serviceName}")
                    try {
                        nsdManager?.resolveService(service, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                                Log.e(TAG, "Fallo resolución: $errorCode")
                            }
                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                Log.d(TAG, "Servicio resuelto: ${serviceInfo.serviceName} -> ${serviceInfo.host}:${serviceInfo.port}")
                                
                                val rawName = serviceInfo.serviceName.substringBefore(" (")
                                
                                val email = rawName
                                    .replace("Huerto-", "")
                                    .replace("-at-", "@")
                                    .replace("-dot-", ".")
                                
                                discoveredUsers[email] = serviceInfo.host
                                discoveredServicesPorts[email] = serviceInfo.port
                                updateConnectedPeers()
                            }
                        })
                    } catch (e: Exception) {
                         Log.e(TAG, "Error resolving service", e)
                    }
                }
                override fun onServiceLost(service: NsdServiceInfo) {
                    Log.e(TAG, "Servicio perdido: ${service.serviceName}")
                    val rawName = service.serviceName.substringBefore(" (")
                    val email = rawName
                        .replace("Huerto-", "")
                        .replace("-at-", "@")
                        .replace("-dot-", ".")
                    discoveredUsers.remove(email)
                    discoveredServicesPorts.remove(email)
                    updateConnectedPeers()
                }
                override fun onDiscoveryStopped(serviceType: String) {}
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    nsdManager?.stopServiceDiscovery(this)
                }
                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
            }

            nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
             Log.e(TAG, "Excepción al iniciar discovery", e)
        }
    }
    
    private fun updateConnectedPeers() {
        _connectedPeers.value = discoveredUsers.keys.toSet()
    }
    
    suspend fun sendMessage(senderName: String, senderEmail: String, receiverEmail: String, content: String, type: String = "CHAT"): Boolean {
        val targetIp = discoveredUsers[receiverEmail]
        val targetPort = discoveredServicesPorts[receiverEmail]
        
        if (targetIp != null && targetPort != null) {
            return withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "Intentando conectar a $targetIp:$targetPort para enviar $type")
                    val socket = Socket(targetIp, targetPort)
                    val writer = PrintWriter(socket.getOutputStream(), true)
                    
                    val json = JSONObject()
                    json.put("type", type)
                    json.put("senderName", senderName) 
                    json.put("senderEmail", senderEmail)
                    json.put("receiverEmail", receiverEmail)
                    json.put("content", content)
                    json.put("timestamp", System.currentTimeMillis())
                    
                    writer.println(json.toString())
                    socket.close()
                    Log.d(TAG, "Mensaje $type enviado P2P")
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Error enviando mensaje P2P", e)
                    false
                }
            }
        } else {
            Log.w(TAG, "Usuario $receiverEmail no encontrado en la red local")
            return false
        }
    }
    
    fun tearDown() {
        try {
            multicastLock?.release() // Liberar lock
            if (registrationListener != null) nsdManager?.unregisterService(registrationListener)
            if (discoveryListener != null) nsdManager?.stopServiceDiscovery(discoveryListener)
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error cerrando P2P", e)
        }
    }
}
