# Arquitectura de Networking - HuertoHogar

## 📋 Índice

1. [Visión General](#visión-general)
2. [Estructura del Proyecto](#estructura-del-proyecto)
3. [APIs Disponibles](#apis-disponibles)
4. [Uso de Repositorios](#uso-de-repositorios)
5. [Manejo de Errores](#manejo-de-errores)
6. [Ejemplos de Uso](#ejemplos-de-uso)
7. [Mejores Prácticas](#mejores-prácticas)

---

## 🎯 Visión General

Esta arquitectura implementa un sistema de networking robusto y escalable para HuertoHogar usando:

- **Retrofit 2.9.0**: Cliente HTTP type-safe
- **OkHttp 4.12.0**: Cliente HTTP con interceptores
- **Gson**: Serialización JSON
- **Coroutines**: Concurrencia asíncrona
- **Hilt**: Inyección de dependencias
- **Room**: Persistencia local y modo offline
- **MVVM**: Arquitectura con separación de responsabilidades

### Principios de Diseño

✅ **Offline-First**: Los datos se cargan primero desde caché local  
✅ **Single Source of Truth**: Room es la fuente única de verdad  
✅ **Separación de DTOs**: Las entidades de red están separadas de las de Room  
✅ **Manejo Robusto de Errores**: Usando sealed classes  
✅ **Type-Safe**: Uso extensivo de tipos y data classes  

---

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/huertohogar_mobil/
├── network/
│   ├── api/                    # Interfaces Retrofit
│   │   ├── AuthApi.kt
│   │   ├── ProductsApi.kt
│   │   ├── PedidosApi.kt
│   │   ├── MessagesApi.kt
│   │   └── SocialApi.kt
│   ├── dto/                    # Data Transfer Objects
│   │   ├── NetworkResult.kt
│   │   ├── ProductoDto.kt
│   │   ├── AuthDto.kt
│   │   ├── PedidoDto.kt
│   │   ├── MensajeDto.kt
│   │   └── SocialDto.kt
│   ├── mappers/                # Conversión DTO ↔ Entity
│   │   ├── ProductoMappers.kt
│   │   └── UserMappers.kt
│   ├── utils/                  # Utilidades
│   │   └── NetworkUtils.kt
│   ├── AuthInterceptor.kt      # Interceptor para tokens
│   ├── RefreshAuthenticator.kt # Auto-refresh de tokens
│   └── NetworkModule.kt        # Configuración Hilt
├── data/
│   └── repository/             # Repositorios
│       ├── ProductosNetworkRepository.kt
│       └── AuthNetworkRepository.kt
└── viewmodel/                  # ViewModels
    └── ProductosNetworkViewModel.kt
```

---

## 🌐 APIs Disponibles

### 1. ProductsApi

Gestión completa de productos del marketplace.

**Endpoints principales:**

```kotlin
// Listar productos
suspend fun list(): Response<List<ProductoDto>>

// Listar con paginación
suspend fun listPaginado(page: Int, size: Int, sort: String?, order: String?): Response<ProductosPaginadosResponse>

// Buscar producto por ID
suspend fun get(id: String): Response<ProductoDto>

// Buscar productos
suspend fun buscar(query: String, page: Int, size: Int): Response<ProductosPaginadosResponse>

// Filtrar por categoría
suspend fun porCategoria(categoria: String, page: Int, size: Int): Response<ProductosPaginadosResponse>

// Productos destacados
suspend fun destacados(limit: Int): Response<List<ProductoDto>>

// Crear producto
suspend fun create(producto: CrearProductoRequest): Response<ProductoDto>

// Actualizar producto
suspend fun update(id: String, producto: ActualizarProductoRequest): Response<ProductoDto>

// Eliminar producto
suspend fun delete(id: String): Response<DeleteProductoResponse>
```

### 2. AuthApi

Autenticación y gestión de usuarios.

**Endpoints principales:**

```kotlin
// Login
suspend fun login(req: LoginRequestDto): Response<AuthResponseDto>

// Registro
suspend fun register(req: RegistroRequestDto): Response<AuthResponseDto>

// Perfil actual
suspend fun me(): Response<ProfileResponseDto>

// Actualizar perfil
suspend fun actualizarPerfil(req: ActualizarPerfilRequest): Response<ProfileResponseDto>

// Cambiar contraseña
suspend fun cambiarPassword(req: CambiarPasswordRequest): Response<SuccessResponse>

// Recuperar contraseña
suspend fun recuperarPassword(req: RecuperarPasswordRequest): Response<SuccessResponse>

// Refresh token
suspend fun refresh(req: RefreshRequest): Response<RefreshResponse>

// Logout
suspend fun logout(): Response<SuccessResponse>
```

### 3. PedidosApi

Gestión de pedidos y órdenes.

**Endpoints principales:**

```kotlin
// Crear pedido
suspend fun crear(pedido: CrearPedidoRequest): Response<PedidoDto>

// Mis pedidos
suspend fun misPedidos(): Response<List<PedidoDto>>

// Pedidos como proveedor
suspend fun pedidosProveedor(): Response<List<PedidoDto>>

// Obtener pedido por ID
suspend fun get(pedidoId: String): Response<PedidoDto>

// Actualizar estado
suspend fun actualizarEstado(pedidoId: String, body: ActualizarEstadoRequest): Response<PedidoDto>

// Confirmar pedido
suspend fun confirmar(pedidoId: String): Response<PedidoDto>

// Marcar como entregado
suspend fun entregado(pedidoId: String): Response<PedidoDto>

// Cancelar pedido
suspend fun cancelar(pedidoId: String, body: CancelarPedidoRequest): Response<PedidoDto>

// Notificaciones
suspend fun notificaciones(page: Int, size: Int, soloNoLeidas: Boolean): Response<List<NotificacionPedidoDto>>
```

### 4. MessagesApi

Mensajería y chat entre usuarios.

**Endpoints principales:**

```kotlin
// Enviar mensaje
suspend fun send(body: EnviarMensajeRequest): Response<MensajeChatDto>

// Inbox
suspend fun inbox(): Response<List<MensajeChatDto>>

// Conversación con usuario
suspend fun thread(email: String): Response<List<MensajeChatDto>>

// Lista de conversaciones
suspend fun conversaciones(): Response<List<ConversacionDto>>

// Buscar mensajes
suspend fun buscar(query: String, email: String?): Response<List<MensajeChatDto>>

// Marcar como leídos
suspend fun marcarLeidos(body: MarcarLeidoRequest): Response<SuccessResponse>

// Contador de no leídos
suspend fun countNoLeidos(): Response<Map<String, Int>>
```

### 5. SocialApi

Funcionalidades sociales y amistades.

**Endpoints principales:**

```kotlin
// Enviar solicitud de amistad
suspend fun createRequest(body: CrearSolicitudAmistadRequest): Response<SolicitudAmistadDto>

// Solicitudes recibidas
suspend fun incoming(): Response<List<SolicitudAmistadDto>>

// Solicitudes enviadas
suspend fun outgoing(): Response<List<SolicitudAmistadDto>>

// Aceptar solicitud
suspend fun accept(id: String): Response<SolicitudAmistadDto>

// Rechazar solicitud
suspend fun reject(id: String, body: Map<String, String>?): Response<SolicitudAmistadDto>

// Lista de amigos
suspend fun friends(): Response<List<AmistadDto>>

// Buscar usuarios
suspend fun buscarUsuarios(body: BuscarUsuariosRequest): Response<BuscarUsuariosResponse>

// Usuarios sugeridos
suspend fun sugerencias(limit: Int): Response<List<UsuarioSugeridoDto>>

// Estadísticas sociales
suspend fun estadisticas(): Response<EstadisticasSocialesDto>
```

---

## 🗄️ Uso de Repositorios

### Patrón Repository

Los repositorios actúan como mediadores entre la fuente de datos local (Room) y remota (API).

**Ejemplo: ProductosRepository**

```kotlin
@Singleton
class ProductosRepository @Inject constructor(
    private val productsApi: ProductsApi,
    private val productoDao: ProductoDao
) {
    // Cache-first con actualización en background
    fun getProductos(forceRefresh: Boolean = false): Flow<NetworkResult<List<Producto>>> = flow {
        emit(NetworkResult.Loading)
        
        // Emitir datos locales primero
        if (!forceRefresh) {
            val localData = productoDao.getAll()
            if (localData.isNotEmpty()) {
                emit(NetworkResult.Success(localData))
            }
        }
        
        // Obtener del servidor y actualizar caché
        val response = productsApi.list()
        if (response.isSuccessful) {
            val productos = response.body()!!.toEntityList()
            productoDao.deleteAll()
            productoDao.insertAll(productos)
            emit(NetworkResult.Success(productos))
        } else {
            // Fallback a caché local
            val localData = productoDao.getAll()
            if (localData.isNotEmpty()) {
                emit(NetworkResult.Success(localData))
            } else {
                emit(NetworkResult.Error("Error: ${response.code()}"))
            }
        }
    }.flowOn(Dispatchers.IO)
}
```

---

## ⚠️ Manejo de Errores

### NetworkResult Sealed Class

```kotlin
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int?, val exception: Throwable?) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
    data object Idle : NetworkResult<Nothing>()
}
```

### Uso en ViewModels

```kotlin
viewModelScope.launch {
    productosRepository.getProductos().collect { result ->
        when (result) {
            is NetworkResult.Loading -> {
                _isLoading.value = true
            }
            is NetworkResult.Success -> {
                _productos.value = result.data
                _isLoading.value = false
            }
            is NetworkResult.Error -> {
                _errorMessage.value = result.message
                _isLoading.value = false
            }
            is NetworkResult.Idle -> {
                _isLoading.value = false
            }
        }
    }
}
```

---

## 💡 Ejemplos de Uso

### 1. Usar en Composable (UI)

```kotlin
@Composable
fun ProductosScreen(
    viewModel: ProductosNetworkViewModel = hiltViewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }
    
    when {
        isLoading -> {
            CircularProgressIndicator()
        }
        errorMessage != null -> {
            Text("Error: $errorMessage")
        }
        productos.isNotEmpty() -> {
            LazyColumn {
                items(productos) { producto ->
                    ProductoItem(producto)
                }
            }
        }
    }
}
```

### 2. Login

```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is NetworkResult.Success -> {
                    // Navegar a home
                    _authState.value = AuthState.Authenticated(result.data.usuario)
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }
        }
    }
}
```

### 3. Buscar Productos

```kotlin
fun buscarProductos(query: String) {
    viewModelScope.launch {
        when (val result = productosRepository.buscarProductos(query)) {
            is NetworkResult.Success -> {
                _productos.value = result.data.productos.map { it.toEntity() }
            }
            is NetworkResult.Error -> {
                _errorMessage.value = result.message
            }
            else -> {}
        }
    }
}
```

---

## 🎯 Mejores Prácticas

### 1. **Siempre usar Coroutines**
```kotlin
// ✅ Correcto
viewModelScope.launch {
    val result = repository.getData()
}

// ❌ Incorrecto (bloqueante)
val result = runBlocking {
    repository.getData()
}
```

### 2. **Manejar todos los estados**
```kotlin
when (result) {
    is NetworkResult.Loading -> showLoading()
    is NetworkResult.Success -> showData(result.data)
    is NetworkResult.Error -> showError(result.message)
    is NetworkResult.Idle -> showIdle()
}
```

### 3. **Usar Flow para datos reactivos**
```kotlin
// En Repository
fun getProductos(): Flow<List<Producto>> = productoDao.getAllFlow()

// En ViewModel
val productos = repository.getProductos()
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
```

### 4. **Cache-First Strategy**
```kotlin
// Siempre emitir caché local primero
emit(localData)
// Luego actualizar desde servidor
val serverData = fetchFromServer()
updateCache(serverData)
emit(serverData)
```

### 5. **Separar DTOs de Entities**
```kotlin
// DTO (de la API)
data class ProductoDto(...)

// Entity (para Room)
@Entity
data class Producto(...)

// Mapper
fun ProductoDto.toEntity(): Producto
```

### 6. **Usar Hilt para Inyección**
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel()
```

### 7. **Manejo de Errores Específicos**
```kotlin
catch (e: UnknownHostException) {
    NetworkResult.Error("Sin conexión a internet")
}
catch (e: SocketTimeoutException) {
    NetworkResult.Error("Tiempo de espera agotado")
}
```

---

## 🔧 Configuración

### Base URL

Configurar en `build.gradle.kts`:
```kotlin
buildConfigField("String", "BASE_URL", "\"https://api.tu-backend.com\"")
buildConfigField("String", "API_VERSION", "\"v1\"")
```

### Interceptor de Autenticación

Automáticamente agrega el token Bearer a las peticiones:
```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenDataStore.getAccessToken()
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
```

---

## 📚 Recursos Adicionales

- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

---

**Creado para HuertoHogar Marketplace** 🌱  
*Arquitectura de Networking Profesional - Android Kotlin*

