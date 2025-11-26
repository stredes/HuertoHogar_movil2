package com.example.huertohogar_mobil.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.huertohogar_mobil.data.HuertoHogarDatabase

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    // Conectamos con la base de datos
    private val database = HuertoHogarDatabase.getDatabase(application)
    private val usuarioDao = database.usuarioDao()

    // Función para validar dominios (Tu lógica de login.js portada a Kotlin)
    private fun esCorreoValido(correo: String): Boolean {
        val correoMin = correo.lowercase()
        return correoMin.endsWith("@duocuc.cl") ||
                correoMin.endsWith("@profesor.duoc.cl") ||
                correoMin.endsWith("@gmail.com")
    }

    // Lógica de Login asíncrona
    suspend fun login(correo: String, contrasena: String): Boolean {
        // 1. Validamos formato del correo
        if (!esCorreoValido(correo)) return false

        // 2. Buscamos el usuario en la BD
        val usuarioEncontrado = usuarioDao.login(correo, contrasena)

        // 3. Retornamos true si existe, false si no
        return usuarioEncontrado != null
    }
}