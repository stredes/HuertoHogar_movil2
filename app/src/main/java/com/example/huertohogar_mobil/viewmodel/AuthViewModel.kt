package com.example.huertohogar_mobil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.FirebaseRepository
import com.example.huertohogar_mobil.data.SessionManager
import com.example.huertohogar_mobil.data.UserRepository
import com.example.huertohogar_mobil.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                val user = userRepository.getUser(safeEmail)
                if (user != null) {
                    firebaseRepository.registerUser(user)
                }
                
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
            // Ejecutar en IO para no bloquear el hilo principal (mejora de latencia percibida)
            val user = withContext(Dispatchers.IO) {
                userRepository.loginUser(safeEmail, safePassword)
            }
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
            val success = userRepository.resetPassword(email.trim().lowercase(), newPass.trim())
            if (success) {
                val user = userRepository.getUser(email.trim().lowercase())
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
                userRepository.updateUser(user)
                _uiState.update { it.copy(user = user) } // Actualizar estado local
                firebaseRepository.registerUser(user) // Actualizar en Firebase
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
