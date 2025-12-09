package com.example.huertohogar_mobil.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.*
import com.example.huertohogar_mobil.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import org.json.JSONObject

private const val TAG = "MarketVM"

data class MarketUiState(
    val productos: List<Producto> = emptyList(), 
    val productosFiltrados: List<Producto> = emptyList(), 
    val carrito: Map<String, Int> = emptyMap(),
    val totalCLP: Int = 0,
    val countCarrito: Int = 0,
    val seleccionado: Producto? = null,
    val query: String = "",
    val admins: List<User> = emptyList(),
    val selectedProviderEmail: String? = null
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repo: ProductoRepository,
    private val carritoDao: CarritoDao,
    private val p2pManager: P2pManager,
    private val firebaseRepository: FirebaseRepository,
    private val userRepository: UserRepository,
    private val socialDao: SocialDao,
    private val sessionManager: SessionManager, // Inyectamos SessionManager para validar email actual
    private val mensajeRepository: MensajeRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _selectedProvider = MutableStateFlow<String?>(null)
    private val _seleccionadoId = MutableStateFlow<String?>(null)

    private val _productosFlow = repo.productos()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val _carritoFlow = carritoDao.getCarrito()
    private val _adminsFlow = userRepository.getAllAdmins()

    @OptIn(FlowPreview::class)
    val ui: StateFlow<MarketUiState> = combine(
        _productosFlow,
        _carritoFlow,
        _adminsFlow,
        combine(_query.debounce(100), _selectedProvider, _seleccionadoId) { q, p, s -> Triple(q, p, s) }
    ) { productos, itemsCarrito, admins, filters ->
        val (query, providerEmail, selId) = filters
        
        val productMap = productos.associateBy { it.id }

        // B. Filtrado optimizado y corregido
        val filteredList = filterProducts(productos, query, providerEmail)

        val carritoMap = itemsCarrito.associate { it.productoId to it.cantidad }
        
        val total = itemsCarrito.sumOf { item ->
            val p = productMap[item.productoId]
            (p?.precioCLP ?: 0) * item.cantidad
        }
        val count = itemsCarrito.sumOf { it.cantidad }

        val seleccionado = selId?.let { productMap[it] }

        MarketUiState(
            productos = productos,
            productosFiltrados = filteredList,
            carrito = carritoMap,
            totalCLP = total,
            countCarrito = count,
            seleccionado = seleccionado,
            query = query,
            admins = admins,
            selectedProviderEmail = providerEmail
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MarketUiState()
    )

    init {
        viewModelScope.launch { repo.ensureSeeded() }
    }

    private fun filterProducts(
        list: List<Producto>, 
        query: String, 
        provider: String?
    ): List<Producto> {
        var result = list
        
        // FIX: Comparación case-insensitive para el filtro de proveedor
        if (!provider.isNullOrBlank()) {
            result = result.filter { 
                it.providerEmail?.equals(provider, ignoreCase = true) == true 
            }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { 
                it.nombre.contains(q, ignoreCase = true)
            }
        }
        return result
    }

    fun setQuery(q: String) { _query.value = q }
    
    fun setProviderFilter(email: String?) { _selectedProvider.value = email }

    fun seleccionar(p: Producto?) { _seleccionadoId.value = p?.id }

    fun agregar(p: Producto, delta: Int = 1) {
        if (delta == 0) return
        viewModelScope.launch {
            // Lógica para no mezclar proveedores
            val itemsEnCarrito = ui.value.carrito.keys
            if (itemsEnCarrito.isNotEmpty() && delta > 0) {
                 val primerProdId = itemsEnCarrito.first()
                 val primerProd = ui.value.productos.find { it.id == primerProdId }
                 val proveedorActual = primerProd?.providerEmail
                 
                 // Si el proveedor del nuevo producto es diferente al que ya está en el carrito
                 if (!p.providerEmail.equals(proveedorActual, ignoreCase = true)) {
                     // Opción: Limpiar carrito anterior o rechazar.
                     // Aquí vamos a limpiar para forzar un solo proveedor
                     // O idealmente preguntar al usuario, pero por simplicidad de ViewModel, limpiamos.
                     // Sin embargo, para mejor UX, deberíamos notificar.
                     // Por ahora, implementamos limpieza automática: "Nuevo proveedor reemplaza carrito"
                     carritoDao.clearCarrito()
                 }
            }

            val currentQty = ui.value.carrito[p.id] ?: 0
            val nuevoQty = (currentQty + delta).coerceAtLeast(0)

            if (nuevoQty > 0) {
                carritoDao.insertItem(CarritoItem(p.id, nuevoQty))
            } else {
                carritoDao.deleteItem(p.id)
            }
        }
    }

    fun quitar(p: Producto) = agregar(p, -1)
    
    fun limpiarCarrito() { viewModelScope.launch { carritoDao.clearCarrito() } }

    fun crearProducto(nombre: String, precio: Int, unidad: String, desc: String, uri: String?, creatorEmail: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            // FIX: Obtención robusta del email del proveedor
            val currentEmail = creatorEmail
                ?: p2pManager.currentUserEmail 
                ?: userRepository.getAllUsersSync().firstOrNull { it.role == "admin" || it.role == "provider" }?.email 
                ?: "admin@huertohogar.com"
                
            // Subida de imagen
            val finalUri = resolveAndUploadImage(uri)

            val nuevo = Producto(
                id = UUID.randomUUID().toString(),
                nombre = nombre,
                precioCLP = precio,
                unidad = unidad,
                descripcion = desc,
                imagenRes = 0,
                imagenUri = finalUri,
                providerEmail = currentEmail
            )
            
            // 1. Guardar localmente siempre
            repo.agregarProducto(nuevo)

            // 2. Sincronizar SIEMPRE
            launch { firebaseRepository.upsertProduct(nuevo.id, nuevo.nombre, nuevo.precioCLP, nuevo.unidad, nuevo.descripcion, nuevo.imagenRes, nuevo.imagenUri, nuevo.providerEmail) }
            launch { notificarUpsertProducto(nuevo) }
        }
    }

    fun editarProducto(id: String, nombre: String, precio: Int, unidad: String, desc: String, uri: String?, originalImgRes: Int, originalProviderEmail: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            // VALIDACIÓN: Verificar si el usuario actual es el dueño del producto
            val currentUserEmail = sessionManager.getUserEmail()
            
            // Permitimos editar si:
            // 1. Es el dueño del producto (providerEmail coincide)
            // 2. O es el usuario "root"
            // 3. O el producto no tiene dueño asignado (caso borde)
            
            // Nota: Podríamos consultar el rol del usuario actual si es necesario ser más estrictos,
            // pero por ahora validamos principalmente la propiedad del producto.
            val isOwner = originalProviderEmail.equals(currentUserEmail, ignoreCase = true)
            val isRoot = currentUserEmail == "root" // Usuario root siempre puede
            
            if (!isOwner && !isRoot && originalProviderEmail != null) {
                Log.w(TAG, "❌ Intento no autorizado de editar producto. Usuario: $currentUserEmail, Dueño: $originalProviderEmail")
                return@launch // Salimos sin hacer cambios
            }

            val finalUri = resolveAndUploadImage(uri)

            val actualizado = Producto(
                id = id,
                nombre = nombre,
                precioCLP = precio,
                unidad = unidad,
                descripcion = desc,
                imagenRes = originalImgRes,
                imagenUri = finalUri,
                providerEmail = originalProviderEmail
            )
            
            repo.actualizarProducto(actualizado)

            // FIX: Sincronizar SIEMPRE
            launch { firebaseRepository.upsertProduct(actualizado.id, actualizado.nombre, actualizado.precioCLP, actualizado.unidad, actualizado.descripcion, actualizado.imagenRes, actualizado.imagenUri, actualizado.providerEmail) }
            launch { notificarUpsertProducto(actualizado) }
        }
    }
    
    private suspend fun resolveAndUploadImage(uri: String?): String? {
        if (uri.isNullOrBlank()) return null
        if (uri.startsWith("http")) return uri
        
        val uriObj = if (uri.startsWith("/")) Uri.fromFile(File(uri)) else Uri.parse(uri)
        
        // REINTENTOS MÁS AGRESIVOS Y ROBUSTOS
        var attempt = 0
        val maxAttempts = 3 // Intentamos 3 veces
        
        while (attempt < maxAttempts) {
            try {
                // Verificar conectividad básica (simulado por el éxito de la función)
                val uploadedUrl = firebaseRepository.uploadProductImage(uriObj)
                if (uploadedUrl != null) {
                    Log.d(TAG, "✅ Imagen subida correctamente en intento ${attempt + 1}: $uploadedUrl")
                    return uploadedUrl
                } else {
                     Log.w(TAG, "⚠️ Intento ${attempt + 1} de subida devolvió null (posible timeout)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción subiendo imagen (intento ${attempt + 1}): ${e.message}")
            }
            
            attempt++
            if (attempt < maxAttempts) {
                // Backoff exponencial simple: 1s, 2s...
                delay(1000L * attempt)
            }
        }
        
        Log.e(TAG, "❌ FALLA CRÍTICA: No se pudo subir imagen tras $maxAttempts intentos. Se retornará null para evitar rutas locales rotas en la nube.")
        return null
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch(Dispatchers.IO) {
            // VALIDACIÓN DE PROPIEDAD
            val currentUserEmail = sessionManager.getUserEmail()
            val isOwner = producto.providerEmail.equals(currentUserEmail, ignoreCase = true)
            val isRoot = currentUserEmail == "root"

            if (!isOwner && !isRoot && producto.providerEmail != null) {
                 Log.w(TAG, "❌ Intento no autorizado de eliminar producto. Usuario: $currentUserEmail, Dueño: ${producto.providerEmail}")
                 return@launch
            }
            
            carritoDao.deleteItem(producto.id)
            repo.eliminarProducto(producto)
            launch { firebaseRepository.deleteProduct(producto.id, producto.providerEmail) }
            launch { notificarDeleteProducto(producto.id) }
        }
    }

    private suspend fun notificarUpsertProducto(producto: Producto) {
        val peers = p2pManager.connectedPeers.value
        if (peers.isEmpty()) return
        
        val senderEmail = p2pManager.currentUserEmail ?: "admin"
        
        val productJson = JSONObject().apply {
            put("id", producto.id)
            put("nombre", producto.nombre)
            put("precio", producto.precioCLP)
            put("unidad", producto.unidad)
            put("descripcion", producto.descripcion)
            put("imagenRes", producto.imagenRes)
            put("imagenUri", producto.imagenUri)
            put("providerEmail", producto.providerEmail)
        }
        
        val payload = JSONObject().apply {
            put("type", "UPSERT_PRODUCT")
            put("senderEmail", senderEmail)
            put("senderName", "Admin")
            put("product", productJson)
        }
        
        peers.forEach { peerEmail ->
            payload.put("receiverEmail", peerEmail)
            p2pManager.sendMessageJsonDirect(peerEmail, payload)
        }
    }

    private suspend fun notificarDeleteProducto(productId: String) {
        val peers = p2pManager.connectedPeers.value
        if (peers.isEmpty()) return
        
        val senderEmail = p2pManager.currentUserEmail ?: "admin"
        val payload = JSONObject().apply {
             put("type", "DELETE_PRODUCT")
             put("senderEmail", senderEmail)
             put("productId", productId)
        }

        peers.forEach { peerEmail ->
             payload.put("receiverEmail", peerEmail)
             p2pManager.sendMessageJsonDirect(peerEmail, payload)
        }
    }

    fun enviarContacto(nombreRemitente: String, emailDestino: String, mensaje: String) {
        val emailRemitente = p2pManager.currentUserEmail ?: return
        
        viewModelScope.launch(Dispatchers.IO) {
            val remitente = userRepository.getUser(emailRemitente) ?: return@launch
            
            val destinatario = userRepository.getUser(emailDestino) ?: run {
                val placeholder = User(
                    name = "Vendedor ($emailDestino)", 
                    email = emailDestino, 
                    passwordHash = "placeholder", 
                    role = "provider"
                )
                userRepository.createUser(placeholder)
                userRepository.getUser(emailDestino)!!
            }
            
            val nuevoMensaje = MensajeChat(
                remitenteId = remitente.id,
                destinatarioId = destinatario.id,
                contenido = mensaje,
                estado = EstadoMensaje.ENVIANDO
            )

            val msgId = socialDao.insertMensaje(nuevoMensaje)
            
            var enviado = p2pManager.sendMessage(
                senderName = remitente.name,
                senderEmail = remitente.email,
                receiverEmail = destinatario.email,
                content = mensaje
            )
            
            if (!enviado) {
                 enviado = firebaseRepository.sendMessage(remitente, destinatario.email, mensaje)
            }
            
            socialDao.updateEstado(msgId, if (enviado) EstadoMensaje.ENVIADO else EstadoMensaje.ERROR)
        }
    }

    fun notificarCompra() {
        viewModelScope.launch {
            val carrito = ui.value.carrito
            val productos = ui.value.productos
            val total = ui.value.totalCLP
            val currentUserEmail = sessionManager.getUserEmail() ?: "Anónimo"

            val sb = StringBuilder()
            sb.append("Nueva compra realizada por: $currentUserEmail\n\n")
            sb.append("Detalle:\n")
            
            carrito.forEach { (id, qty) ->
                val p = productos.find { it.id == id }
                if (p != null) {
                    sb.append("- $qty x ${p.nombre} ($${p.precioCLP})\n")
                }
            }
            sb.append("\nTotal: $$total")

            mensajeRepository.enviarMensaje(
                nombre = "Sistema de Ventas",
                email = currentUserEmail,
                texto = sb.toString()
            )
        }
    }
}
