package com.example.huertohogar_mobil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, passwordHash: String) {
        // Limpiamos los datos: quitamos espacios y normalizamos el email
        val safeName = name.trim()
        val safeEmail = email.trim().lowercase()
        val safePassword = passwordHash.trim()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val success = userRepository.registerUser(safeName, safeEmail, safePassword)
            if (success) {
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
                _uiState.update { it.copy(user = user, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Credenciales inválidas") }
            }
        }
    }

    fun logout() {
        _uiState.update { it.copy(user = null) }
    }
    
    fun clearError() {
         _uiState.update { it.copy(error = null) }
    }
}
