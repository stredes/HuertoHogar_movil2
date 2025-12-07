package com.example.huertohogar_mobil.data

import android.util.Log
import com.example.huertohogar_mobil.model.EstadoMensaje
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.Solicitud
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.model.Amistad
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    private val socialDao: SocialDao,
    private val userDao: UserDao,
    private val solicitudDao: SolicitudDao
) {
    private val TAG = "FirebaseRepo"
    private var db: FirebaseFirestore? = null
    private var messageListener: ListenerRegistration? = null
    private var currentUserEmail: String? = null
    
    init {
        try {
            val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                db = FirebaseFirestore.getInstance()
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                db?.firestoreSettings = settings
                
                Log.d(TAG, "🔥 Firebase Firestore inicializado correctamente conectado a: ${FirebaseApp.getInstance().options.projectId}")
            }
        } catch (e: Exception) {
             Log.e(TAG, "Error initializing Firestore: ${e.message}")
        }
    }

    fun initialize(email: String) {
        currentUserEmail = email
        if (db == null) {
            try {
                db = FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo obtener instancia de Firestore", e)
                return
            }
        }
        
        Log.d(TAG, "Iniciando sincronización para usuario: $email")
        syncUsers()
        startListeningForMessages(email)
        registerUserOnline(email)
    }

    private fun registerUserOnline(email: String) {
        if (db == null) return
        val data = hashMapOf("lastSeen" to System.currentTimeMillis())
        db!!.collection("users").document(email).set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "User online status updated") }
            .addOnFailureListener { Log.e(TAG, "Error updating lastSeen", it) }
    }

    private fun syncUsers() {
        if (db == null) return
        
        try {
            db!!.collection("users").addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        for (doc in snapshots) {
                            val email = doc.id
                            val emailField = doc.getString("email")
                            val finalEmail = emailField ?: email
                            
                            if (finalEmail == currentUserEmail) continue 

                            val name = doc.getString("name") ?: "Usuario"
                            val role = doc.getString("role") ?: "user"
                            
                            val existing = userDao.getUserByEmail(finalEmail)
                            if (existing == null) {
                                userDao.insertUser(User(name = name, email = finalEmail, passwordHash = "firebase", role = role))
                                Log.d(TAG, "Usuario sincronizado desde nube: $finalEmail")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
             Log.e(TAG, "Error in syncUsers", e)
        }
    }

    private fun startListeningForMessages(myEmail: String) {
        if (messageListener != null || db == null) return

        try {
            Log.d(TAG, "Escuchando mensajes para: $myEmail")
            messageListener = db!!.collection("messages")
                .whereEqualTo("receiverEmail", myEmail)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(TAG, "Message listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            for (doc in snapshots.documentChanges) {
                                if (doc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    val data = doc.document.data
                                    val senderEmail = data["senderEmail"] as? String ?: continue
                                    val content = data["content"] as? String ?: ""
                                    val timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis()
                                    val type = data["type"] as? String ?: "CHAT"
                                    val senderName = data["senderName"] as? String ?: "Desconocido"

                                    var sender = userDao.getUserByEmail(senderEmail)
                                    if (sender == null) {
                                        userDao.insertUser(User(name = senderName, email = senderEmail, passwordHash = "firebase"))
                                        sender = userDao.getUserByEmail(senderEmail)
                                    }
                                    
                                    val me = userDao.getUserByEmail(myEmail)

                                    if (sender != null && me != null) {
                                        handleIncomingMessage(type, sender, me, content, timestamp)
                                    }
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
             Log.e(TAG, "Error in startListeningForMessages", e)
        }
    }

    private suspend fun handleIncomingMessage(type: String, sender: User, me: User, content: String, timestamp: Long) {
        when (type) {
            "CHAT", "IMAGEN", "AUDIO", "VIDEO", "UBICACION" -> {
                if (!socialDao.existeMensaje(sender.id, me.id, timestamp, content)) {
                    val msg = MensajeChat(
                        remitenteId = sender.id,
                        destinatarioId = me.id,
                        contenido = content,
                        timestamp = timestamp,
                        estado = EstadoMensaje.RECIBIDO,
                        tipoContenido = if (type == "CHAT") "TEXTO" else type
                    )
                    socialDao.insertMensaje(msg)
                    Log.d(TAG, "Mensaje recibido de nube: $content")
                }
            }
            "FRIEND_REQUEST" -> {
                val existing = solicitudDao.getSolicitud(sender.email, me.email)
                if (existing == null) {
                    val solicitud = Solicitud(
                        senderName = sender.name,
                        senderEmail = sender.email,
                        receiverEmail = me.email,
                        estado = "PENDIENTE"
                    )
                    solicitudDao.insertSolicitud(solicitud)
                    Log.d(TAG, "Solicitud de amistad recibida de nube")
                }
            }
            "REQUEST_ACCEPTED" -> {
                socialDao.agregarAmigo(Amistad(me.id, sender.id))
                socialDao.agregarAmigo(Amistad(sender.id, me.id))
                Log.d(TAG, "Solicitud aceptada recibida de nube")
            }
        }
    }

    suspend fun sendMessage(sender: User, receiverEmail: String, content: String, type: String = "CHAT"): Boolean {
        if (db == null) return false
        return try {
            val msgMap = hashMapOf(
                "senderEmail" to sender.email,
                "senderName" to sender.name,
                "receiverEmail" to receiverEmail,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "type" to type
            )
            
            db!!.collection("messages").add(msgMap).await()
            Log.d(TAG, "Mensaje enviado a nube para $receiverEmail")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando mensaje a Firebase", e)
            false
        }
    }

    fun registerUser(user: User) {
        if (db == null) return
        val userMap = hashMapOf(
            "name" to user.name,
            "email" to user.email,
            "role" to user.role,
            "lastSeen" to System.currentTimeMillis()
        )
        db!!.collection("users").document(user.email).set(userMap, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "User registered in cloud: ${user.email}") }
            .addOnFailureListener { Log.e(TAG, "Error registering user in Firebase", it) }
    }
    
    fun upsertProduct(id: String, nombre: String, precio: Int, unidad: String, desc: String, imgRes: Int, uri: String?) {
        if (db == null) return
        val productMap = hashMapOf(
            "id" to id,
            "nombre" to nombre,
            "precio" to precio,
            "unidad" to unidad,
            "descripcion" to desc,
            "imagenRes" to imgRes,
            "imagenUri" to (uri ?: ""),
            "timestamp" to System.currentTimeMillis()
        )
        
        db!!.collection("products").document(id).set(productMap)
            .addOnSuccessListener { Log.d(TAG, "Producto subido a nube: $nombre") }
            .addOnFailureListener { Log.e(TAG, "Error subiendo producto", it) }
    }
    
    fun deleteProduct(id: String) {
        if (db == null) return
        db!!.collection("products").document(id).delete()
            .addOnSuccessListener { Log.d(TAG, "Producto eliminado de nube: $id") }
    }

    fun observeUserStatus(email: String): Flow<Boolean> = callbackFlow {
        if (db == null) {
            trySend(false)
            awaitClose { }
            return@callbackFlow
        }

        val docRef = db!!.collection("users").document(email)
        val listener = docRef.addSnapshotListener { snapshot, _ ->
            val lastSeen = snapshot?.getLong("lastSeen")
            val isOnline = if (lastSeen != null) {
                val minutesSinceLastSeen = (System.currentTimeMillis() - lastSeen) / 60000
                minutesSinceLastSeen < 5 // Consider online if seen in the last 5 minutes
            } else {
                false
            }
            trySend(isOnline)
        }

        awaitClose { listener.remove() }
    }

    fun cleanup() {
        messageListener?.remove()
        messageListener = null
    }
}
