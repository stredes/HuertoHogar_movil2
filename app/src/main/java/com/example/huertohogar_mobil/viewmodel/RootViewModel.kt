package com.example.huertohogar_mobil.viewmodel

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.*
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
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

    // Estado explícito de sincronización y error
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError = _syncError.asStateFlow()

    // Listas completas (Flows reactivos desde Room)
    val users: StateFlow<List<User>> = userRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val productos: StateFlow<List<Producto>> = productoRepository.productos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val admins: StateFlow<List<User>> = userRepository.getAllAdmins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estadísticas Reactivas (Se actualizan automáticamente con la DB)
    val userCount: StateFlow<Int> = users.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val productCount: StateFlow<Int> = productos.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Historial de Sincronización
    private val _syncHistory = MutableStateFlow<List<String>>(emptyList())
    val syncHistory = _syncHistory.asStateFlow()

    // Últimas sincronizaciones
    private val _lastCloudSync = MutableStateFlow<String?>(null)
    val lastCloudSync: StateFlow<String?> = _lastCloudSync.asStateFlow()
    private val _lastLocalSync = MutableStateFlow<String?>(null)
    val lastLocalSync: StateFlow<String?> = _lastLocalSync.asStateFlow()

    // Auto-sync cloud
    private val _autoSyncCloud = MutableStateFlow(false)
    val autoSyncCloud: StateFlow<Boolean> = _autoSyncCloud.asStateFlow()

    init {
        // Inicializamos P2P y Firebase para el usuario Root
        p2pManager.initialize("root")
        firebaseRepository.initialize("root")
        
        // Sincronización inicial para mostrar timestamps
        viewModelScope.launch {
            addToHistory("Sistema Root inicializado")
            // Realizar sync inicial con la nube para cargar datos
            sincronizarSoloCloud()
        }

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

    override fun onCleared() {
        super.onCleared()
        // FIX: Llamar a tearDown para evitar fugas de memoria y sockets abiertos
        p2pManager.tearDown()
        firebaseRepository.cleanup()
    }

    // Helper para historial
    private fun addToHistory(event: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _syncHistory.value = listOf("[$timestamp] $event") + _syncHistory.value
    }

    private fun stampNow(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    // Validar autorización root
    fun validarRoot(password: String): Boolean {
        return password == "root"
    }
    
    // --- GESTIÓN USUARIOS ---

    fun crearAdmin(nombre: String, email: String, pass: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val safeEmail = email.trim().lowercase()
        val safeName = nombre.trim()
        val safePass = pass.trim()
        
        viewModelScope.launch {
            if (userRepository.createAdmin(safeName, safeEmail, safePass)) {
                val newUser = User(name = safeName, email = safeEmail, passwordHash = safePass, role = "admin")
                firebaseRepository.registerUser(newUser)

                onSuccess()
                addToHistory("Creado nuevo admin: $safeEmail")

                // Sincronizar con nube y red
                sincronizarSoloCloud()
            } else {
                onError()
            }
        }
    }
    
    fun guardarUsuario(user: User, isNew: Boolean, onSuccess: () -> Unit, onError: () -> Unit) {
        val safeUser = user.copy(
            name = user.name.trim(),
            email = user.email.trim().lowercase(),
            passwordHash = user.passwordHash.trim()
        )
        
        if (!isNew && safeUser.role != "root") {
             val existing = users.value.find { it.id == safeUser.id }
             if (existing?.role == "root") {
                 onError() 
                 addToHistory("Intento fallido de modificar rol de ROOT.")
                 return
             }
        }
        
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
                firebaseRepository.registerUser(safeUser)
                // Sincronizar con la nube para reflejar cambios inmediatamente
                sincronizarSoloCloud()
            }
        }
    }

    fun eliminarUsuario(userId: Int) {
        val userToDelete = users.value.find { it.id == userId }
        if (userToDelete != null) {
            if (userToDelete.role == "root" || userToDelete.email == "root") {
                addToHistory("⚠️ Acción bloqueada: No se puede eliminar al usuario ROOT.")
                return
            }
        }
        
        viewModelScope.launch {
            userRepository.deleteUser(userId)

            // Eliminar también de Firebase inmediatamente
            if (userToDelete != null) {
                firebaseRepository.deleteUser(userToDelete.email)
            }

            addToHistory("Usuario eliminado: ${userToDelete?.email ?: "ID: $userId"}")

            // Sincronización completa para reflejar cambios
            sincronizarSoloCloud()
        }
    }
    
    fun eliminarTodosUsuariosNoRoot() {
        viewModelScope.launch {
            userRepository.nukeUsers()
            addToHistory("Eliminados todos los usuarios no-root")
            sincronizarDatosConAdmins()
        }
    }
    
    // --- GESTIÓN PRODUCTOS ---
    
    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
             productoRepository.eliminarProducto(producto)
             firebaseRepository.deleteProduct(producto.id, producto.providerEmail)
             addToHistory("Producto eliminado: ${producto.nombre}")

             // Sincronizar con la nube para reflejar cambios
             sincronizarSoloCloud()
        }
    }

    // --- SINCRONIZACION ---
    fun sincronizarDatosConAdmins() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            try {
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
                    if (u.role == "root") return@forEach

                    val json = JSONObject().apply {
                        put("name", u.name)
                        put("email", u.email)
                        put("role", u.role)
                        // FIX SEGURIDAD: NO enviar passwordHash por P2P en texto plano.
                    }
                    usersArray.put(json)

                    // Backup a la nube
                    firebaseRepository.registerUser(u)
                }

                val currentProducts = productoRepository.getAllProductosSync()
                val productsArray = JSONArray()
                currentProducts.forEach { p ->
                    var productToSend = p

                    // INTENTO DE REPARACIÓN DE IMAGEN LOCAL ANTES DE SUBIR
                    // Si la imagen es local (no empieza con http), intentamos subirla a Firebase Storage
                    val uri = p.imagenUri
                    if (!uri.isNullOrBlank() && !uri.startsWith("http")) {
                        var uploadSuccess = false
                        try {
                            var fileUri: Uri? = null

                            if (uri.startsWith("/")) {
                                val file = File(uri)
                                if (file.exists()) fileUri = Uri.fromFile(file)
                                else Log.e("RootViewModel", "Archivo local no encontrado: $uri")
                            }
                            else if (uri.startsWith("file://")) {
                                val path = uri.removePrefix("file://")
                                val file = File(path)
                                if (file.exists()) fileUri = Uri.fromFile(file)
                                else Log.e("RootViewModel", "Archivo local (file://) no encontrado: $path")
                            }
                            else {
                                // Reemplazo de Uri.parse por extensión KTX
                                fileUri = uri.toUri()
                            }

                            if (fileUri != null) {
                                val cloudUrl = firebaseRepository.uploadProductImage(fileUri)

                                if (cloudUrl != null) {
                                     // Actualizamos localmente para tener la URL remota y no re-subir
                                     productToSend = p.copy(imagenUri = cloudUrl)
                                     productoRepository.actualizarProducto(productToSend)
                                     Log.d("RootViewModel", "Imagen local reparada y subida: $cloudUrl")
                                     uploadSuccess = true
                                } else {
                                    Log.w("RootViewModel", "Fallo al subir imagen local: $uri")
                            }
                        }
                        } catch (e: Exception) {
                            Log.e("RootViewModel", "Error al procesar imagen: $uri", e)
                        }

                        // LÓGICA STRICTA: Si sigue siendo local (falló subida), la eliminamos del objeto a enviar
                        // para NO contaminar la nube ni a otros usuarios.
                        if (!uploadSuccess) {
                            Log.e("RootViewModel", "⚠️ SANEAMIENTO: Eliminando referencia local $uri antes de sincronizar.")
                            productToSend = productToSend.copy(imagenUri = null)
                        }
                    }

                    val json = JSONObject().apply {
                        put("id", productToSend.id)
                        put("nombre", productToSend.nombre)
                        put("precio", productToSend.precioCLP)
                        put("unidad", productToSend.unidad)
                        put("descripcion", productToSend.descripcion)
                        put("imagenRes", productToSend.imagenRes)
                        put("imagenUri", productToSend.imagenUri)
                        put("providerEmail", productToSend.providerEmail)
                    }
                    productsArray.put(json)

                    // Sincronización Cloud: Root respalda TODOS los productos en la nube.
                    firebaseRepository.upsertProduct(
                        productToSend.id,
                        productToSend.nombre,
                        productToSend.precioCLP,
                        productToSend.unidad,
                        productToSend.descripcion,
                        productToSend.imagenRes,
                        productToSend.imagenUri,
                        productToSend.providerEmail
                    )
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

                addToHistory("Sincronización en la nube completada.")
                _lastLocalSync.value = stampNow()
             } catch (e: Exception) {
                 _syncError.value = e.message
                 addToHistory("Error en sincronización: ${e.message}")
                 Log.e("RootViewModel", "Sync error", e)
                // Aún registramos un timestamp para visibilidad de intento
                _lastLocalSync.value = _lastLocalSync.value ?: stampNow()
             } finally {
                 _isSyncing.value = false
             }
         }
     }

     // --- SINCRONIZACION CLOUD ---
     fun sincronizarSoloCloud() {
         viewModelScope.launch {
             _isSyncing.value = true
             _syncError.value = null
             addToHistory("Iniciando sincronización SOLO nube...")
             try {
                 val currentUsers = userRepository.getAllUsersSync()
                 val currentProducts = productoRepository.getAllProductosSync()

                 // Sincronización completa bidireccional (incluye eliminaciones)
                 firebaseRepository.syncAllUsers(currentUsers)
                 firebaseRepository.syncAllProducts(currentProducts)

                 val stamp = stampNow()
                 _lastCloudSync.value = stamp
                 addToHistory("Sincronización con la nube completada a ${'$'}stamp.")
                 addToHistory("${currentUsers.size} usuarios y ${currentProducts.size} productos sincronizados")
             } catch (e: Exception) {
                 _syncError.value = e.message
                 addToHistory("Error en sincronización nube: ${e.message}")
                 Log.e("RootViewModel", "Cloud sync error", e)
                // Aún marcamos el intento
                _lastCloudSync.value = _lastCloudSync.value ?: stampNow()
             } finally {
                 _isSyncing.value = false
             }
         }
     }

    fun setAutoSyncCloud(enabled: Boolean) {
        _autoSyncCloud.value = enabled
        addToHistory(if (enabled) "Auto-sync nube ACTIVADO" else "Auto-sync nube DESACTIVADO")
        if (enabled) {
            // Trigger inmediato para que no quede atrasado
            sincronizarSoloCloud()
        }
    }
}
