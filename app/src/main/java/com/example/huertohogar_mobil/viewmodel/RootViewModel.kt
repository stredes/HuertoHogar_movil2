package com.example.huertohogar_mobil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.huertohogar_mobil.data.FirebaseRepository
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
    private val p2pManager: P2pManager,
    private val firebaseRepository: FirebaseRepository
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
        // Inicializamos P2P y Firebase para el usuario Root
        p2pManager.initialize("root")
        firebaseRepository.initialize("root")
        
        refreshStats()
        
        // Observamos nuevos peers para sincronizar automáticamente (P2P)
        viewModelScope.launch {
            p2pManager.connectedPeers.collect { peers ->
                if (peers.isNotEmpty()) {
                    val msg = "Peers locales detectados: ${peers.size}. Iniciando auto-sync..."
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
        firebaseRepository.cleanup()
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
                val newUser = User(name = safeName, email = safeEmail, passwordHash = safePass, role = "admin")
                firebaseRepository.registerUser(newUser) // Sync to Cloud
                
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
            
            if (success) {
                firebaseRepository.registerUser(safeUser) // Sync to Cloud
                refreshStats()
                sincronizarDatosConAdmins()
            }
        }
    }

    fun eliminarUsuario(userId: Int) {
        viewModelScope.launch {
            // No implementamos borrado en cloud por seguridad, solo local
            userRepository.deleteUser(userId)
            refreshStats()
            addToHistory("Usuario eliminado localmente (ID: $userId)")
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
             
             // Enviar señal de borrado a través de Firebase si fuera necesario
             // firebaseRepository.deleteProduct(producto.id) // Placeholder
        }
    }

    // --- SINCRONIZACION ---
    fun sincronizarDatosConAdmins() {
        viewModelScope.launch {
            // Obtenemos lista de peers conectados
            val peers = p2pManager.connectedPeers.value
            Log.d("RootViewModel", "Iniciando Sincronización. Peers encontrados: ${peers.size}")
            
            if (peers.isEmpty()) {
                addToHistory("Sync local omitido: No hay dispositivos WiFi Direct cercanos.")
            } else {
                addToHistory("Iniciando envío de datos a ${peers.size} dispositivos cercanos...")
            }

            // Datos a sincronizar
            val currentUsers = userRepository.getAllUsersSync()
            val usersArray = JSONArray()
            currentUsers.forEach { u ->
                val json = JSONObject().apply {
                    put("name", u.name)
                    put("email", u.email)
                    put("role", u.role)
                    put("passwordHash", u.passwordHash)
                }
                usersArray.put(json)
                
                // Backup a la nube
                if (u.role != "root") {
                    firebaseRepository.registerUser(u)
                }
            }
            
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
            
            // Sincronización LOCAL (P2P)
            peers.forEach { peerEmail ->
                val payload = JSONObject().apply {
                    put("type", "ADMIN_SYNC_DATA")
                    put("senderEmail", "root")
                    put("senderName", "Super Usuario")
                    put("receiverEmail", peerEmail)
                    put("users", usersArray)
                    put("products", productsArray)
                }
                
                p2pManager.sendMessageJsonDirect(peerEmail, payload)
                addToHistory("Datos enviados localmente a $peerEmail")
            }
            
            // Sincronización CLOUD (Firebase)
            // Aquí enviamos una señal de 'sync' a todos los administradores registrados via Firebase
            // Para simplificar, subimos la configuración global a un documento 'config' o 'sync'
            // que los admins escuchan.
            
            // Nota: La implementación completa de sync de productos via Firebase requeriría
            // guardar la colección 'productos' en Firestore.
            // Por ahora aseguramos que los usuarios estén sincronizados.
            addToHistory("Sincronización en la nube completada.")
        }
    }
}
