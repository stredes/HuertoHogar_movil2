# 🚀 Guía de Implementación Rápida - Networking HuertoHogar

## ✅ Checklist de Implementación

### 1️⃣ Archivos Creados

#### APIs (Retrofit Interfaces)
- ✅ `network/api/ProductsApi.kt` - Gestión de productos
- ✅ `network/api/AuthApi.kt` - Autenticación y usuarios
- ✅ `network/api/PedidosApi.kt` - Gestión de pedidos
- ✅ `network/api/MessagesApi.kt` - Mensajería y chat
- ✅ `network/api/SocialApi.kt` - Funcionalidades sociales

#### DTOs (Data Transfer Objects)
- ✅ `network/dto/NetworkResult.kt` - Manejo de estados
- ✅ `network/dto/ProductoDto.kt` - DTOs de productos
- ✅ `network/dto/AuthDto.kt` - DTOs de autenticación
- ✅ `network/dto/PedidoDto.kt` - DTOs de pedidos
- ✅ `network/dto/MensajeDto.kt` - DTOs de mensajes
- ✅ `network/dto/SocialDto.kt` - DTOs sociales

#### Mappers
- ✅ `network/mappers/ProductoMappers.kt` - Conversión Producto ↔ ProductoDto
- ✅ `network/mappers/UserMappers.kt` - Conversión User ↔ UsuarioDto

#### Repositorios
- ✅ `data/repository/ProductosNetworkRepository.kt` - Repository de productos
- ✅ `data/repository/AuthNetworkRepository.kt` - Repository de autenticación

#### Utilidades
- ✅ `network/utils/NetworkUtils.kt` - Utilidades de red
- ✅ `data/datastore/TokenDataStore.kt` - Gestión de tokens

#### ViewModels (Ejemplos)
- ✅ `viewmodel/ProductosNetworkViewModel.kt` - ViewModel ejemplo

#### Documentación
- ✅ `NETWORKING_ARCHITECTURE.md` - Documentación completa

---

## 🔧 Pasos de Configuración

### Paso 1: Configurar Base URL

En `app/build.gradle.kts`, las siguientes líneas ya están agregadas:

```kotlin
buildConfigField("String", "BASE_URL", "\"https://api.tu-backend.com\"")
buildConfigField("String", "API_VERSION", "\"v1\"")
buildFeatures {
    buildConfig = true  // ✅ Ya está habilitado
}
```

**Acción requerida:** Cambia `https://api.tu-backend.com` por la URL real de tu backend.

### Paso 2: Sincronizar Gradle

Las dependencias ya están en el `build.gradle.kts`:
- ✅ Retrofit 2.9.0
- ✅ OkHttp 4.12.0
- ✅ Gson
- ✅ DataStore Preferences
- ✅ Coroutines
- ✅ Hilt

**Acción:** Ejecuta `./gradlew build` o sincroniza en Android Studio.

### Paso 3: Actualizar NetworkModule (si es necesario)

El archivo `network/NetworkModule.kt` ya existe y está configurado. Verifica que esté usando `TokenDataStore`:

```kotlin
@Provides
@Singleton
fun provideRefreshAuthenticator(
    tokenDataStore: TokenDataStore,  // ✅ Usar el nuevo DataStore
    authApi: AuthApi
): RefreshAuthenticator = RefreshAuthenticator(tokenDataStore, authApi)
```

---

## 💻 Ejemplos de Uso Inmediato

### 1. Login de Usuario

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthNetworkRepository
) : ViewModel() {
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is NetworkResult.Success -> {
                    // Usuario autenticado
                    val authResponse = result.data
                    // Navegar a home
                }
                is NetworkResult.Error -> {
                    // Mostrar error
                    showError(result.message)
                }
                else -> {}
            }
        }
    }
}
```

### 2. Listar Productos

```kotlin
@HiltViewModel
class ProductosViewModel @Inject constructor(
    private val productosRepository: ProductosNetworkRepository
) : ViewModel() {
    
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos = _productos.asStateFlow()
    
    fun cargarProductos() {
        viewModelScope.launch {
            productosRepository.getProductos().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _productos.value = result.data
                    }
                    is NetworkResult.Error -> {
                        // Manejar error
                    }
                    else -> {}
                }
            }
        }
    }
}
```

### 3. Composable de UI

```kotlin
@Composable
fun ProductosScreen(
    viewModel: ProductosViewModel = hiltViewModel()
) {
    val productos by viewModel.productos.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }
    
    LazyColumn {
        items(productos) { producto ->
            ProductoCard(producto)
        }
    }
}
```

---

## 🎯 Tareas Pendientes

### Prioridad Alta
- [ ] Actualizar `BASE_URL` en `build.gradle.kts` con la URL real del backend
- [ ] Actualizar `AuthInterceptor.kt` para usar `TokenDataStore`
- [ ] Actualizar `RefreshAuthenticator.kt` para usar `TokenDataStore`
- [ ] Probar login y obtención de tokens

### Prioridad Media
- [ ] Crear ViewModels para Pedidos, Mensajes y Social
- [ ] Implementar manejo de errores en UI
- [ ] Agregar indicadores de carga
- [ ] Implementar pull-to-refresh

### Prioridad Baja
- [ ] Agregar paginación en UI
- [ ] Implementar caché con expiración
- [ ] Agregar tests unitarios
- [ ] Optimizar sincronización offline

---

## 🔍 Verificación Rápida

### Verificar que todo compile:

```bash
cd /home/gian/StudioProjects/HuertoHogar_movil2
./gradlew build
```

### Verificar APIs creadas:

```bash
find app/src/main/java -name "*Api.kt" -type f
```

Deberías ver:
- ProductsApi.kt ✅
- AuthApi.kt ✅
- PedidosApi.kt ✅
- MessagesApi.kt ✅
- SocialApi.kt ✅

---

## 📖 Flujo de Datos

```
┌─────────────┐
│   UI/View   │ (Composables)
└──────┬──────┘
       │ collectAsState()
       ↓
┌─────────────┐
│  ViewModel  │ (StateFlow)
└──────┬──────┘
       │ viewModelScope.launch
       ↓
┌─────────────┐
│ Repository  │ (Lógica de negocio)
└──────┬──────┘
       │
   ┌───┴───┐
   ↓       ↓
┌──────┐ ┌──────┐
│ Room │ │ API  │ (Retrofit)
└──────┘ └──────┘
 Local    Remote
```

---

## 🚨 Problemas Comunes y Soluciones

### Error: "Cannot access BuildConfig"
**Solución:** Asegúrate de que `buildFeatures { buildConfig = true }` esté en tu `build.gradle.kts`

### Error: "Unresolved reference: TokenDataStore"
**Solución:** Verifica que el archivo `TokenDataStore.kt` exista y sincroniza Gradle

### Error de compilación en mappers
**Solución:** Verifica que las entidades Room y DTOs coincidan en nombres de campos

### La app no se conecta al backend
**Solución:** 
1. Verifica la `BASE_URL`
2. Asegúrate de tener permisos de internet en `AndroidManifest.xml`
3. Para desarrollo local, usa la IP de tu máquina, no `localhost`

---

## 📱 Probar la Implementación

### Test Manual 1: Login
1. Ejecuta la app
2. Ingresa credenciales de prueba
3. Verifica que se guarde el token en DataStore
4. Verifica que las llamadas posteriores incluyan el token

### Test Manual 2: Productos
1. Abre la pantalla de productos
2. Verifica que se carguen desde caché (offline)
3. Pull to refresh para actualizar desde servidor
4. Verifica que se actualice la caché

### Test Manual 3: Modo Offline
1. Carga datos con conexión
2. Activa modo avión
3. Cierra y abre la app
4. Verifica que los datos persistan

---

## 🎓 Recursos de Aprendizaje

1. **Documentación Completa:** `NETWORKING_ARCHITECTURE.md`
2. **Ejemplos de ViewModels:** `viewmodel/ProductosNetworkViewModel.kt`
3. **Ejemplos de Repositorios:** `data/repository/`
4. **DTOs de Referencia:** `network/dto/`

---

## 🤝 Próximos Pasos

1. **Implementar las pantallas UI** usando los ViewModels de ejemplo
2. **Crear repositorios** para Pedidos, Mensajes y Social (siguiendo el patrón de ProductosRepository)
3. **Agregar manejo de errores** en las pantallas
4. **Implementar refresh tokens** automático
5. **Agregar sincronización en background**

---

## 📞 Soporte

Si tienes dudas sobre la implementación, revisa:
- Documentación completa en `NETWORKING_ARCHITECTURE.md`
- Ejemplos de código en `viewmodel/` y `data/repository/`
- Comentarios en el código fuente

**¡Todo está listo para comenzar a usar la arquitectura de networking!** 🎉

---

*Última actualización: 2 de Enero, 2026*

