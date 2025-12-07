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
import com.example.huertohogar_mobil.model.MensajeContacto
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
    private val mensajeRepo: MensajeRepository
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

    fun triggerPanicAction() {
        viewModelScope.launch {
            // 1. Borrar la base de datos de la app
            getApplication<Application>().deleteDatabase("huertohogar_db")

            // Aquí podrías agregar la limpieza de SharedPreferences si las usaras

            // 2. Emitir evento para que la UI pida la desinstalación
            _uninstallRequest.emit(Unit)
        }
    }
    
    // Función para probar notificaciones manualmente
    fun lanzarNotificacionPrueba() {
        val context = getApplication<Application>().applicationContext
        
        // Verificamos permiso antes de enviar
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no hay permiso, no hacemos nada (o logueamos el error)
            return
        }

        val builder = NotificationCompat.Builder(context, "HUERTO_CHANNEL_ID")
            .setSmallIcon(R.drawable.icono) // Asegúrate de que este recurso exista, sino usa ic_launcher_foreground
            .setContentTitle("Nuevo Mensaje de Contacto")
            .setContentText("Tienes nuevos mensajes en tu buzón administrativo.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(1001, builder.build())
        }
    }
}
