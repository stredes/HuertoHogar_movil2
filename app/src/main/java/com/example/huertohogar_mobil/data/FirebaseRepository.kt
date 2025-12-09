package com.example.huertohogar_mobil.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.huertohogar_mobil.MainActivity
import com.example.huertohogar_mobil.R
import com.example.huertohogar_mobil.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val socialDao: SocialDao,
    private val userDao: UserDao,
    private val solicitudDao: SolicitudDao,
    private val productoDao: ProductoDao,
    private val mensajeDao: MensajeDao
) {

    companion object {
        private const val TAG = "FirebaseRepo"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_PRODUCTS = "products"
        private const val COLLECTION_MESSAGES = "messages"
        private const val MSG_CHANNEL_ID = "HUERTO_MESSAGES_CHANNEL"
    }

    private var db: FirebaseFirestore? = null
    private var storage: FirebaseStorage? = null

    private var messageListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null
    private var productListener: ListenerRegistration? = null

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val processedMessageIds = Collections.synchronizedSet(mutableSetOf<String>())
    private var lastSyncTimestamp: Long = 0
    
    private var currentEmail: String? = null

    init {
        try {
            val app = FirebaseApp.getInstance()
            if (FirebaseApp.getApps(app.applicationContext).isNotEmpty()) {
                db = FirebaseFirestore.getInstance().apply {
                    @Suppress("DEPRECATION")
                    firestoreSettings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()
                }
                storage = FirebaseStorage.getInstance()
                createNotificationChannel()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mensajes y Alertas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(MSG_CHANNEL_ID, name, importance).apply {
                description = "Notificaciones de la nube"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun initialize(email: String) {
        currentEmail = email
        ioScope.launch {
            lastSyncTimestamp = socialDao.getLastMessageTimestamp() ?: (System.currentTimeMillis() - 86400000) // 24h atrás como fallback

            cleanup()
            processedMessageIds.clear()
            
            startListeningForMessages(email)
            syncUsers(email)
            syncProducts()
            registerUserOnline(email)
        }
    }

    private fun registerUserOnline(email: String) {
        if (email == "root") return
        db?.collection(COLLECTION_USERS)?.document(email)?.set(hashMapOf("lastSeen" to System.currentTimeMillis()), SetOptions.merge())
    }

    private fun syncUsers(myEmail: String) {
        userListener = db?.collection(COLLECTION_USERS)?.addSnapshotListener { snapshots, _ ->
            snapshots?.documentChanges?.forEach { change ->
                val doc = change.document
                if (doc.id == myEmail || doc.id == "root") return@forEach
                
                ioScope.launch {
                    val passwordHash = doc.getString("passwordHash") ?: ""
                    
                    val user = User(
                        id = 0, 
                        name = doc.getString("name") ?: "-", 
                        email = doc.id, 
                        passwordHash = passwordHash, 
                        role = doc.getString("role") ?: "user"
                    )
                    
                    val existing = userDao.getUserByEmail(doc.id)
                    if (existing != null) {
                        val finalPass = if (existing.passwordHash == "synced" && passwordHash.isNotEmpty()) passwordHash else existing.passwordHash
                        userDao.updateUserByEmail(user.name, user.email, finalPass, user.role)
                    } else {
                        userDao.insertUser(user)
                    }
                }
            }
        }
    }

    private fun syncProducts() {
        // Sincronización completa: escuchamos TODOS los productos
        productListener = db?.collection(COLLECTION_PRODUCTS)?.addSnapshotListener { snapshots, _ ->
            snapshots?.documentChanges?.forEach { change ->
                ioScope.launch {
                    val doc = change.document
                    when (change.type) {
                        DocumentChange.Type.REMOVED -> productoDao.getProductoByIdSync(doc.id)?.let { productoDao.delete(it) }
                        else -> {
                            val p = doc.toObject(Producto::class.java).copy(id = doc.id)
                            productoDao.insert(p)
                        }
                    }
                }
            }
        }
    }

    private fun startListeningForMessages(myEmail: String) {
        messageListener = db?.collection(COLLECTION_MESSAGES)
            ?.whereEqualTo("receiverEmail", myEmail)
            ?.addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Message listen failed.", e)
                    return@addSnapshotListener
                }
                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val timestamp = change.document.getLong("timestamp") ?: 0L
                        if (timestamp > lastSyncTimestamp) {
                            ioScope.launch { processIncomingMessage(change.document, myEmail) }
                        }
                    }
                }
            }
    }

    private suspend fun processIncomingMessage(doc: DocumentSnapshot, myEmail: String) {
        val docId = doc.id
        if (!processedMessageIds.add(docId)) return

        val data = doc.data ?: return
        val senderEmail = data["senderEmail"] as? String ?: return
        val senderName = data["senderName"] as? String ?: senderEmail
        val content = data["content"] as? String ?: ""
        val type = data["type"] as? String ?: "CHAT"
        val timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis()

        if (timestamp > lastSyncTimestamp) lastSyncTimestamp = timestamp

        var sender = userDao.getUserByEmail(senderEmail)
        if (sender == null) {
            userDao.insertUser(User(name = senderName, email = senderEmail, passwordHash = "firebase_sender"))
            sender = userDao.getUserByEmail(senderEmail)
        }

        val me = userDao.getUserByEmail(myEmail)
        if (sender == null || me == null) return

        when (type) {
            "CHAT", "IMAGEN", "AUDIO", "VIDEO", "UBICACION" -> {
                showNotification("Nuevo mensaje de ${sender.name}", content)
                if (!socialDao.existeMensaje(sender.id, me.id, timestamp, content)) {
                    val msg = MensajeChat(0L, sender.id, me.id, content, if (type == "CHAT") "TEXTO" else type, timestamp, EstadoMensaje.RECIBIDO)
                    socialDao.insertMensaje(msg)
                }
            }
            "FRIEND_REQUEST" -> {
                val existingRequest = solicitudDao.getSolicitud(sender.email, me.email)
                if (existingRequest == null || existingRequest.estado == "RECHAZADA") {
                    val solicitud = Solicitud(0, sender.name, sender.email, me.email, timestamp, "PENDIENTE")
                    solicitudDao.insertSolicitud(solicitud)
                    showNotification("Solicitud de Amistad", "${sender.name} quiere ser tu amigo")
                }
            }
            "REQUEST_ACCEPTED" -> {
                 if (!socialDao.esAmigo(me.id, sender.id)) {
                    socialDao.agregarAmigo(Amistad(me.id, sender.id))
                    socialDao.agregarAmigo(Amistad(sender.id, me.id))
                    showNotification("Amistad Aceptada", "${sender.name} aceptó tu solicitud")
                }
                solicitudDao.getSolicitud(me.email, sender.email)?.let { 
                    solicitudDao.deleteSolicitud(it.id)
                }
            }
            "CONTACT_FORM" -> {
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
                mensajeDao.insertMensaje(MensajeContacto(0, senderName, senderEmail, content, fecha, false))
                showNotification("Nuevo Contacto", "$senderName te ha contactado")
            }
        }
    }

    private fun showNotification(title: String, content: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, MSG_CHANNEL_ID)
            .setSmallIcon(R.drawable.icono)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
    }

    suspend fun uploadProductImage(uri: Uri): String? {
        return try {
            val ref = storage?.reference?.child("product_images/${UUID.randomUUID()}.jpg")
            ref?.putFile(uri)?.await()
            ref?.downloadUrl?.await().toString()
        } catch (e: Exception) { null }
    }

    suspend fun sendMessage(sender: User, receiverEmail: String, content: String, type: String = "CHAT"): Boolean {
        return try {
            db?.collection(COLLECTION_MESSAGES)?.add(hashMapOf(
                "senderEmail" to sender.email, "senderName" to sender.name, "receiverEmail" to receiverEmail,
                "content" to content, "timestamp" to System.currentTimeMillis(), "type" to type
            ))?.await()
            true
        } catch (e: Exception) { false }
    }

    fun registerUser(user: User) {
        if (user.role == "root") return
        db?.collection(COLLECTION_USERS)?.document(user.email)?.set(user, SetOptions.merge())
    }
    
    fun upsertProduct(id: String, nombre: String, precio: Int, unidad: String, desc: String, imgRes: Int, uri: String?, providerEmail: String?) {
        if (currentEmail != "root" && providerEmail != currentEmail) {
            Log.w(TAG, "Seguridad: $currentEmail intentó modificar producto de $providerEmail. Bloqueado.")
            return
        }

        val map = hashMapOf(
            "nombre" to nombre, 
            "precioCLP" to precio, 
            "unidad" to unidad, 
            "descripcion" to desc, 
            "imagenRes" to imgRes, 
            "imagenUri" to (uri ?: ""), 
            "providerEmail" to (providerEmail ?: ""), 
            "timestamp" to System.currentTimeMillis()
        )
        db?.collection(COLLECTION_PRODUCTS)?.document(id)?.set(map)
    }
    
    fun deleteProduct(id: String, ownerEmail: String?) {
        if (currentEmail != "root" && ownerEmail != currentEmail) {
            Log.w(TAG, "Seguridad: $currentEmail intentó eliminar producto de $ownerEmail. Bloqueado.")
            return
        }
        db?.collection(COLLECTION_PRODUCTS)?.document(id)?.delete()
    }

    fun cleanup() {
        messageListener?.remove()
        userListener?.remove()
        productListener?.remove()
    }

    fun observeUserStatus(email: String): Flow<Boolean> = callbackFlow {
        val listener = db?.collection(COLLECTION_USERS)?.document(email)
            ?.addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val lastSeen = snapshot.getLong("lastSeen") ?: 0L
                    val isOnline = (System.currentTimeMillis() - lastSeen) < 120000
                    trySend(isOnline)
                } else {
                    trySend(false)
                }
            }
        awaitClose { listener?.remove() }
    }
}
