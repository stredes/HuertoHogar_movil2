package com.example.huertohogar_mobil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.FirebaseRepository
import com.example.huertohogar_mobil.data.SessionManager
import com.example.huertohogar_mobil.data.UserRepository
import com.example.huertohogar_mobil.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseRepository: FirebaseRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Inicializamos el estado verificando síncronamente si hay sesión guardada para mostrar Loading de inmediato
    private val _uiState = MutableStateFlow(
        if (!sessionManager.getUserEmail().isNullOrBlank()) {
            AuthUiState(isLoading = true)
        } else {
            AuthUiState(isLoading = false)
        }
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        val savedEmail = sessionManager.getUserEmail()
        if (!savedEmail.isNullOrBlank()) {
            viewModelScope.launch {
                val user = userRepository.getUser(savedEmail)
                if (user != null) {
                    _uiState.update { it.copy(user = user, isLoading = false) }
                    firebaseRepository.initialize(user.email)
                } else {
                    // Si el usuario guardado no existe (eliminado?), limpiamos sesión
                    sessionManager.clearSession()
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun register(name: String, email: String, passwordHash: String) {
        // Limpiamos los datos: quitamos espacios y normalizamos el email
        val safeName = name.trim()
        val safeEmail = email.trim().lowercase()
        val safePassword = passwordHash.trim()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val success = userRepository.registerUser(safeName, safeEmail, safePassword)
            if (success) {
                // Registrar también en Firebase para sincronización
                // Construimos el usuario explícitamente para asegurar que el hash coincida
                val newUser = User(
                    name = safeName,
                    email = safeEmail,
                    passwordHash = safePassword,
                    role = "user"
                )
                firebaseRepository.registerUser(newUser)
                
                // Intentamos loguear automáticamente con las credenciales limpias
                login(safeEmail, safePassword)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "El usuario ya existe") }
            }
        }
    }

    fun login(email: String, passwordHash: String) {
        val safeEmail = email.trim().lowercase()
        val safePassword = passwordHash.trim()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = userRepository.loginUser(safeEmail, safePassword)
            if (user != null) {
                sessionManager.saveUserSession(safeEmail) // Guardamos sesión
                _uiState.update { it.copy(user = user, isLoading = false) }
                firebaseRepository.initialize(user.email)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Credenciales inválidas") }
            }
        }
    }
    
    fun verifyEmail(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = userRepository.verifyUserExists(email.trim().lowercase())
            onResult(exists)
        }
    }

    fun resetPassword(email: String, newPass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val safeEmail = email.trim().lowercase()
            val safePass = newPass.trim()
            val success = userRepository.resetPassword(safeEmail, safePass)
            if (success) {
                val user = userRepository.getUser(safeEmail)
                if (user != null) {
                    firebaseRepository.registerUser(user)
                }
            }
            onResult(success)
        }
    }
    
    fun updateUserProfile(user: User, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Recuperar el usuario actual de la DB para preservar el passwordHash si viene vacío
                val existingUser = userRepository.getUser(user.email)
                val finalUser = if (existingUser != null && user.passwordHash.isBlank()) {
                    user.copy(passwordHash = existingUser.passwordHash)
                } else {
                    user
                }

                userRepository.updateUser(finalUser)
                _uiState.update { it.copy(user = finalUser) } // Actualizar estado local
                firebaseRepository.registerUser(finalUser) // Actualizar en Firebase
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun logout() {
        sessionManager.clearSession() // Borramos sesión
        _uiState.update { it.copy(user = null) }
        firebaseRepository.cleanup()
    }
    
    fun clearError() {
         _uiState.update { it.copy(error = null) }
    }
}
