# Cambios de Marca: Huerto Hogar → Red Privada
## Fecha: 2025-12-27
## Resumen
Se ha completado el rebranding de la aplicación de "Huerto Hogar" a "Red Privada".
## Archivos Modificados
### 1. Archivos de Aplicación Principal
- **RedPrivadaApp.kt** (antes HuertoHogarApp.kt)
  - Clase principal: `HuertoHogarApp` → `RedPrivadaApp`
  - TAG de logging: `"HuertoHogarApp"` → `"RedPrivadaApp"`
  - Canal de notificaciones: `"Huerto Hogar Alertas"` → `"Red Privada Alertas"`
  - ID del canal: `"HUERTO_CHANNEL_ID"` → `"REDPRIVADA_CHANNEL_ID"`
### 2. Servicios
- **RedPrivadaMessagingService.kt** (antes HuertoHogarMessagingService.kt)
  - Clase: `HuertoHogarMessagingService` → `RedPrivadaMessagingService`
  - TAG: `"HuertoHogarFCM"` → `"RedPrivadaFCM"`
  - ID de canal: `"HUERTO_MESSAGES_CHANNEL"` → `"REDPRIVADA_MESSAGES_CHANNEL"`
  - Descripción: `"Notificaciones del Huerto"` → `"Notificaciones de Red Privada"`
### 3. Servicios P2P
- **P2pService.kt**
  - Nombre del canal: `"Huerto P2P Background Service"` → `"Red Privada P2P Background Service"`
  - Título de notificación: `"Huerto Hogar Conectado"` → `"Red Privada Conectada"`
- **P2pManager.kt**
  - TAG: `"HuertoP2P"` → `"RedPrivadaP2P"`
  - Nombre de servicio: `"HuertoUser"` → `"RedPrivadaUser"`
  - Prefijo de servicio: `"Huerto-"` → `"RedPrivada-"`
  - Multicast lock: `"HuertoP2PLock"` → `"RedPrivadaP2PLock"`
### 4. Interfaz de Usuario
- **IniciarSesionScreen.kt**
  - Título principal: `"Huerto Hogar"` → `"Red Privada"`
- **CompartirCarritoScreen.kt**
  - Encabezado: `"Mi Lista de Compras HuertoHogar"` → `"Mi Lista de Compras Red Privada"`
- **CatalogoScreen.kt**
  - Título: `"HuertoHogar - Proveedores"` → `"Red Privada - Proveedores"`
  - Proveedor general: `"HuertoHogar General"` → `"Red Privada General"`
### 5. Tema
- **Theme.kt**
  - Función del tema: `HuertoHogarMobilTheme()` → `RedPrivadaMobilTheme()`
- **Color.kt**
  - Comentario: `"Paleta Natural - HuertoHogar"` → `"Paleta Natural - Red Privada"`
- **themes.xml**
  - Estilo: `Theme.HuertoHogar_mobil` → `Theme.RedPrivada_mobil`
- **MainActivity.kt**
  - Import y uso actualizado del nuevo tema
### 6. Configuración
- **AndroidManifest.xml**
  - Referencia a la clase de aplicación: `.HuertoHogarApp` → `.RedPrivadaApp`
  - Tema: `@style/Theme.HuertoHogar_mobil` → `@style/Theme.RedPrivada_mobil`
- **strings.xml**
  - Ya contenía: `app_name = "Red Privada"` ✓
### 7. Archivos Deprecados
- **config/HuertoHogarApplication.kt**
  - Actualizado con referencias a `RedPrivadaApp`
  - TAG: `"RedPrivadaApp"`
  - Log de inicio: `"INICIANDO RED PRIVADA"`
## Lo que NO se cambió (por diseño)
### Nombres de Paquetes
- El package `com.example.huertohogar_mobil` se mantiene
- Los nombres internos de componentes (HuertoButton, HuertoTextField, etc.) se mantienen
- Cambiar el package requeriría refactorización completa y puede causar problemas
### Archivos de Configuración
- `build.gradle.kts`: namespace sigue siendo `com.example.huertohogar_mobil`
- `google-services.json`: configuración de Firebase sin cambios
## Verificación
✅ Todas las referencias visibles al usuario cambiadas
✅ Nombres de servicios y canales actualizados
✅ Tema y estilos renombrados
✅ Archivos de código renombrados
✅ Proyecto limpiado (gradlew clean)
## Notas
- Los archivos de build intermedios se regenerarán automáticamente
- El usuario verá "Red Privada" en toda la UI
- Los nombres técnicos internos mantienen estabilidad del código
## Correcciones Post-Compilación (2025-12-27)
### Problema: Error "WindowSizeClass not provided"
**Error Original:**
```
java.lang.IllegalStateException: WindowSizeClass not provided
at com.example.huertohogar_mobil.ui.theme.WindowSizeClassKt.LocalWindowSizeClass$lambda$0
```
**Causa:**
El tema `RedPrivadaMobilTheme` no proporcionaba el `WindowSizeClass` necesario para que `ResponsiveUtils` funcione correctamente en diferentes tamaños de pantalla.
**Solución:**
1. Agregado import: `androidx.compose.runtime.CompositionLocalProvider`
2. Actualizado `RedPrivadaMobilTheme` para:
   - Calcular `windowSizeClass` usando `rememberWindowSizeClass()`
   - Proveer el `LocalWindowSizeClass` usando `CompositionLocalProvider`
**Código modificado en Theme.kt:**
```kotlin
@Composable
fun RedPrivadaMobilTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) {
        DarkColors
    } else {
        LightColors
    }
    val windowSizeClass = rememberWindowSizeClass()
    CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            content = content
        )
    }
}
```
### Otros Errores Corregidos
1. **MainActivity.kt**
   - Import incorrecto: `HuertoHogarMobilTheme` → `RedPrivadaMobilTheme`
2. **CheckoutScreen.kt**
   - Removido `.align(Alignment.CenterHorizontally)` inválido en `Column`
   - Agregado `horizontalAlignment = Alignment.CenterHorizontally` en parámetros
3. **IniciarSesionScreen.kt**
   - Agregado parámetro faltante `text = "Iniciar Sesión"` en `HuertoButton`
4. **RedPrivadaApp.kt**
   - TAG: `"HuertoHogarApp"` → `"RedPrivadaApp"`
   - Mensaje: `"INICIANDO HUERTOHOGAR"` → `"INICIANDO RED PRIVADA"`
## Estado Final
✅ **BUILD SUCCESSFUL**
✅ Aplicación compilada sin errores
✅ Todos los cambios de rebranding completados
✅ Sistema responsivo funcionando correctamente
✅ Tema actualizado con soporte para WindowSizeClass
## Warnings Menores (No críticos)
- Unchecked casts en FirebaseRepository (inherentes a la API de Firebase)
- Deprecated APIs de Android (por compatibilidad con versiones antiguas)
- Experimental APIs de Coroutines (funcionan correctamente)
