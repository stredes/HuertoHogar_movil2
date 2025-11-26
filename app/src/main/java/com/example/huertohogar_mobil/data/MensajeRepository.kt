package com.example.huertohogar_mobil.data

import android.util.Log
import com.example.huertohogar_mobil.model.MensajeContacto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val TAG = "DB_DEBUG_MENSAJES"

class MensajeRepository @Inject constructor(
    private val mensajeDao: MensajeDao
) {
    fun getMensajes(): Flow<List<MensajeContacto>> = mensajeDao.getAllMensajes()
        .onEach { Log.d(TAG, "Recuperados ${it.size} mensajes de contacto") }

    suspend fun enviarMensaje(nombre: String, email: String, texto: String) {
        // Fecha simple actual
        val fecha = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        
        val nuevoMensaje = MensajeContacto(
            nombre = nombre,
            email = email,
            mensaje = texto,
            fecha = fecha
        )
        mensajeDao.insertMensaje(nuevoMensaje)
        Log.d(TAG, "📨 Mensaje enviado y guardado: ${nuevoMensaje.email}")
    }

    suspend fun eliminarMensaje(mensaje: MensajeContacto) {
        mensajeDao.deleteMensaje(mensaje)
        Log.d(TAG, "🗑️ Mensaje eliminado")
    }

    suspend fun toggleRespondido(id: Int, actual: Boolean) {
        mensajeDao.marcarComoRespondido(id, !actual)
        Log.d(TAG, "✅ Estado de respuesta actualizado")
    }
}
