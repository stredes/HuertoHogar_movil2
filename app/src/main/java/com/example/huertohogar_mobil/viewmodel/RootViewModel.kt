package com.example.huertohogar_mobil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.huertohogar_mobil.data.P2pManager
import com.example.huertohogar_mobil.data.ProductoRepository
import com.example.huertohogar_mobil.data.UserRepository
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.json.JSONObject
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class RootViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val productoRepository: ProductoRepository,
    private val p2pManager: P2pManager
) : ViewModel() {

    // Listas completas
    val users: StateFlow<List<User>> = userRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val productos: StateFlow<List<Producto>> = productoRepository.productos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val admins: StateFlow<List<User>> = userRepository.getAllAdmins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estadísticas
    private val _userCount = MutableStateFlow(0)
    val userCount = _userCount.asStateFlow()
    
    private val _productCount = MutableStateFlow(0)
    val productCount = _productCount.asStateFlow()

    // Historial de Sincronización
    private val _syncHistory = MutableStateFlow<List<String>>(emptyList())
    val syncHistory = _syncHistory.asStateFlow()

    init {
        // Inicializamos P2P para el usuario Root
        p2pManager.initialize("root")
        
        refreshStats()
        
        // Observamos nuevos peers para sincronizar automáticamente
        viewModelScope.launch {
            p2pManager.connectedPeers.collect { peers ->
                if (peers.isNotEmpty()) {
                    val msg = "Peers detectados: ${peers.size}. Iniciando auto-sync..."
                    Log.d("RootViewModel", msg)
                    addToHistory(msg)
                    sincronizarDatosConAdmins()
                }
            }
        }
    }
    
    fun refreshStats() {
        viewModelScope.launch {
            _userCount.value = userRepository.getUserCount()
            _productCount.value = productoRepository.getProductCount()
        }
    }

    override fun onCleared() {
        super.onCleared()
        p2pManager.tearDown()
    }

    // Helper para historial
    private fun addToHistory(event: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _syncHistory.value = listOf("[$timestamp] $event") + _syncHistory.value
    }

    // Validar autorización root
    fun validarRoot(password: String): Boolean {
        return password == "root"
    }
    
    // --- GESTIÓN USUARIOS ---

    fun crearAdmin(nombre: String, email: String, pass: String, onSuccess: () -> Unit, onError: () -> Unit) {
        // Normalizamos el email para evitar errores de login
        val safeEmail = email.trim().lowercase()
        val safeName = nombre.trim()
        val safePass = pass.trim()
        
        viewModelScope.launch {
            if (userRepository.createAdmin(safeName, safeEmail, safePass)) {
                onSuccess()
                refreshStats()
                addToHistory("Creado nuevo admin: $safeEmail")
                // Sincronizar cambios automáticamente
                sincronizarDatosConAdmins()
            } else {
                onError()
            }
        }
    }
    
    fun guardarUsuario(user: User, isNew: Boolean, onSuccess: () -> Unit, onError: () -> Unit) {
        // Normalizamos datos antes de guardar
        val safeUser = user.copy(
            name = user.name.trim(),
            email = user.email.trim().lowercase(),
            passwordHash = user.passwordHash.trim()
        )
        
        viewModelScope.launch {
            var success = false
            if (isNew) {
                if (userRepository.createUser(safeUser)) {
                    onSuccess()
                    success = true
                    addToHistory("Usuario creado: ${safeUser.email}")
                } else {
                    onError()
                }
            } else {
                userRepository.updateUser(safeUser)
                onSuccess()
                success = true
                addToHistory("Usuario actualizado: ${safeUser.email}")
            }
            refreshStats()
            if (success) {
                sincronizarDatosConAdmins()
            }
        }
    }

    fun eliminarUsuario(userId: Int) {
        viewModelScope.launch {
            userRepository.deleteUser(userId)
            refreshStats()
            addToHistory("Usuario eliminado (ID: $userId)")
            sincronizarDatosConAdmins()
        }
    }
    
    fun eliminarTodosUsuariosNoRoot() {
        viewModelScope.launch {
            userRepository.nukeUsers()
            refreshStats()
            addToHistory("Eliminados todos los usuarios no-root")
            sincronizarDatosConAdmins()
        }
    }
    
    // --- GESTIÓN PRODUCTOS ---
    
    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
             productoRepository.eliminarProducto(producto)
             refreshStats()
             addToHistory("Producto eliminado: ${producto.nombre}")
             sincronizarDatosConAdmins()
        }
    }

    // --- SINCRONIZACION ---
    fun sincronizarDatosConAdmins() {
        viewModelScope.launch {
            // Obtenemos lista de peers conectados
            val peers = p2pManager.connectedPeers.value
            Log.d("RootViewModel", "Iniciando Sincronización. Peers encontrados: ${peers.size}")
            
            if (peers.isEmpty()) {
                addToHistory("Intento de sync fallido: No hay dispositivos conectados.")
                return@launch
            }

            addToHistory("Iniciando envío de datos a ${peers.size} dispositivos...")

            // Obtenemos todos los usuarios actuales (incluyendo admins creados localmente)
            val currentUsers = userRepository.getAllUsersSync() // Metodo sync que agregamos antes a UserDao
            
            // Preparamos payload de usuarios
            val usersArray = JSONArray()
            currentUsers.forEach { u ->
                val json = JSONObject().apply {
                    put("name", u.name)
                    put("email", u.email)
                    put("role", u.role)
                    put("passwordHash", u.passwordHash)
                }
                usersArray.put(json)
            }
            
            // Obtenemos todos los productos
            val currentProducts = productoRepository.getAllProductosSync()
            val productsArray = JSONArray()
            currentProducts.forEach { p ->
                val json = JSONObject().apply {
                    put("id", p.id)
                    put("nombre", p.nombre)
                    put("precio", p.precioCLP)
                    put("unidad", p.unidad)
                    put("descripcion", p.descripcion)
                    put("imagenRes", p.imagenRes)
                    put("imagenUri", p.imagenUri)
                }
                productsArray.put(json)
            }
            
            // Enviamos a todos los peers conectados
            peers.forEach { peerEmail ->
                val payload = JSONObject().apply {
                    put("type", "ADMIN_SYNC_DATA")
                    put("senderEmail", "root") // El root actúa como sender
                    put("senderName", "Super Usuario")
                    put("receiverEmail", peerEmail)
                    put("users", usersArray)
                    put("products", productsArray)
                }
                
                Log.d("RootViewModel", "Enviando datos a: $peerEmail")
                p2pManager.sendMessageJsonDirect(peerEmail, payload)
                addToHistory("Datos enviados a $peerEmail")
            }
        }
    }
}
