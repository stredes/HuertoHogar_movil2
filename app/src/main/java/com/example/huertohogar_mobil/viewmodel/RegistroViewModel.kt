package com.example.huertohogar_mobil.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.HuertoHogarDatabase
import com.example.huertohogar_mobil.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.huertohogar_mobil.data.UsuarioDao

class RegistroViewModel(application: Application) : AndroidViewModel(application) {
    private val usuarioDao = HuertoHogarDatabase.getDatabase(application).usuarioDao()

    suspend fun register(nombre: String, correo: String, contrasena: String): Boolean {
        if (!correo.endsWith("@duocuc.cl")) {
            return false
        }
        val existingUser = usuarioDao.getUsuarioPorCorreo(correo)
        if (existingUser != null) {
            return false // El correo ya está en uso
        }

        val nuevoUsuario = Usuario(nombre = nombre, correo = correo, contrasena = contrasena)
        usuarioDao.insertar(nuevoUsuario)
        return true
    }
}