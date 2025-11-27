package com.example.huertohogar_mobil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.SocialDao
import com.example.huertohogar_mobil.data.UserDao
import com.example.huertohogar_mobil.model.Amistad
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialDao: SocialDao,
    private val userDao: UserDao
) : ViewModel() {

    // Usuario logueado (idealmente vendría de una sesión persistente, aquí lo simularemos o pasaremos)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    // Lista de amigos del usuario actual
    val amigos: StateFlow<List<User>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> socialDao.getAmigos(user.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Resultados de búsqueda
    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // Chat activo
    private val _chatMessages = MutableStateFlow<List<MensajeChat>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    fun setCurrentUser(email: String) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            _currentUser.value = user
        }
    }

    fun buscarPersonas(query: String) {
        val user = _currentUser.value ?: return
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _searchResults.value = socialDao.buscarUsuarios(user.id, query)
        }
    }

    fun agregarAmigo(amigo: User) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            socialDao.agregarAmigo(Amistad(user.id, amigo.id))
            // También creamos la amistad inversa para que sea bidireccional
            socialDao.agregarAmigo(Amistad(amigo.id, user.id))
            // Limpiamos la búsqueda
            _searchResults.value = _searchResults.value.filter { it.id != amigo.id }
        }
    }

    // Cargar conversación con un amigo específico
    fun cargarChat(amigoId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            socialDao.getConversacion(user.id, amigoId).collect { msgs ->
                _chatMessages.value = msgs
            }
        }
    }

    fun enviarMensaje(destinatarioId: Int, texto: String) {
        val user = _currentUser.value ?: return
        if (texto.isBlank()) return
        
        viewModelScope.launch {
            val nuevoMensaje = MensajeChat(
                remitenteId = user.id,
                destinatarioId = destinatarioId,
                contenido = texto
            )
            socialDao.enviarMensaje(nuevoMensaje)
        }
    }
}
