package com.example.huertohogar_mobil.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.R
import com.example.huertohogar_mobil.data.MensajeRepository
import com.example.huertohogar_mobil.data.P2pManager
import com.example.huertohogar_mobil.data.SocialDao
import com.example.huertohogar_mobil.data.UserRepository
import com.example.huertohogar_mobil.model.EstadoMensaje
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.MensajeContacto
import com.example.huertohogar_mobil.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    application: Application,
    private val mensajeRepo: MensajeRepository,
    private val p2pManager: P2pManager,
    private val socialDao: SocialDao,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    private val _uninstallRequest = MutableSharedFlow<Unit>()
    val uninstallRequest: SharedFlow<Unit> = _uninstallRequest.asSharedFlow()

    val mensajes: StateFlow<List<MensajeContacto>> = mensajeRepo.getMensajes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun marcarComoRespondido(id: Int, actual: Boolean) {
        viewModelScope.launch {
            mensajeRepo.toggleRespondido(id, actual)
        }
    }

    fun eliminarMensaje(mensaje: MensajeContacto) {
        viewModelScope.launch {
            mensajeRepo.eliminarMensaje(mensaje)
        }
    }

    // Nueva función para aceptar y notificar al usuario mediante Chat
    fun aceptarSolicitud(mensaje: MensajeContacto) {
        viewModelScope.launch {
            // 1. Marcar como respondido visualmente en el buzón admin
            if (!mensaje.respondido) {
                mensajeRepo.toggleRespondido(mensaje.id, false)
            }

            // 2. Enviar mensaje al usuario solicitando método de pago
            val adminEmail = p2pManager.currentUserEmail ?: "admin@huertohogar.com"
            val userEmail = mensaje.email
            val content = "¡Hola! Tu solicitud ha sido ACEPTADA. Por favor, indícanos tu método de pago preferido:\n\n1. Efectivo\n2. Transferencia"

            // Obtener o crear usuarios para el chat
            val adminUser = userRepository.getUser(adminEmail)
            var targetUser = userRepository.getUser(userEmail)

            // Si el usuario no existe en BD local (ej: contacto anónimo), creamos un placeholder
            if (targetUser == null) {
                val newUser = User(
                    name = mensaje.nombre, 
                    email = userEmail, 
                    passwordHash = "guest_contact", 
                    role = "user"
                )
                userRepository.createUser(newUser)
                targetUser = userRepository.getUser(userEmail)
            }

            if (adminUser != null && targetUser != null) {
                // a) Guardar mensaje en la BD local (para que aparezca en el chat del Admin)
                val chatMsg = MensajeChat(
                    remitenteId = adminUser.id,
                    destinatarioId = targetUser.id,
                    contenido = content,
                    estado = EstadoMensaje.ENVIANDO
                )
                val msgId = socialDao.insertMensaje(chatMsg)

                // b) Enviar vía P2P (para que llegue al usuario real)
                val sent = p2pManager.sendMessage(
                    senderName = adminUser.name,
                    senderEmail = adminUser.email,
                    receiverEmail = targetUser.email,
                    content = content
                )
                
                socialDao.updateEstado(msgId, if (sent) EstadoMensaje.ENVIADO else EstadoMensaje.ERROR)
            }

            // 3. Notificación local para confirmar al Admin que se procesó
            enviarNotificacionDePago(mensaje.email)
        }
    }

    private fun enviarNotificacionDePago(userEmail: String) {
         val context = getApplication<Application>().applicationContext
         val channelId = "HUERTO_CHANNEL_ID"
         
         if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
             val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.icono)
                .setContentTitle("Solicitud Procesada")
                .setContentText("Se envió solicitud de pago a $userEmail")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

             with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
             }
         }
    }

    fun triggerPanicAction() {
        viewModelScope.launch {
            // 1. Borrar la base de datos de la app
            getApplication<Application>().deleteDatabase("huertohogar_db")

            // 2. Emitir evento para que la UI pida la desinstalación
            _uninstallRequest.emit(Unit)
        }
    }
    
    // Función para probar notificaciones manualmente
    fun lanzarNotificacionPrueba() {
        val context = getApplication<Application>().applicationContext
        
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val builder = NotificationCompat.Builder(context, "HUERTO_CHANNEL_ID")
            .setSmallIcon(R.drawable.icono)
            .setContentTitle("Nuevo Mensaje de Contacto")
            .setContentText("Tienes nuevos mensajes en tu buzón administrativo.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(1001, builder.build())
        }
    }
}
