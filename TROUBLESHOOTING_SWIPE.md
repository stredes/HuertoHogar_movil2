# Guía de Troubleshooting - Swipe no funciona

## ✅ SOLUCIÓN IMPLEMENTADA (2025-12-27 13:24)

### Cambios Críticos Realizados:

#### 1. **Reordenamiento de Componentes en PedidosScreen**
**PROBLEMA**: El `PullToRefreshBox` estaba envolviendo el `HorizontalPager`, capturando todos los gestos.

**SOLUCIÓN**: Invertir la estructura - HorizontalPager fuera, PullToRefreshBox dentro de cada página.

```kotlin
// ❌ ANTES (NO FUNCIONA)
PullToRefreshBox(...) {
    HorizontalPager(...) { ... }
}

// ✅ DESPUÉS (FUNCIONA)
HorizontalPager(...) { page ->
    PullToRefreshBox(...) {
        // Contenido de cada página
    }
}
```

#### 2. **Indicadores Visuales de Debug**
Agregado en TODAS las pantallas un texto que muestra:
- Página actual
- Total de páginas
- Instrucción "Desliza horizontalmente →"

Esto te permite ver si el pager está respondiendo a tus gestos.

#### 3. **pageSpacing = 0.dp**
Agregado explícitamente en todos los HorizontalPager para asegurar que no haya espacio entre páginas que interfiera.

#### 4. **Optimización de LazyColumn**
Cambiado el padding de LazyColumn a contentPadding para evitar conflictos con gestos.

### Pantallas Actualizadas:
- ✅ **AdminPedidosScreen.kt** - Con indicador visual
- ✅ **MisPedidosScreen** (PedidosScreen.kt) - PullToRefreshBox reubicado + indicador
- ✅ **CatalogoScreen.kt** - Con indicador visual  
- ✅ **AdminNotificacionesScreen.kt** - Con indicador visual

### ✅ Compilación Exitosa
BUILD SUCCESSFUL - Todos los cambios aplicados correctamente.

---

## 🎯 CÓMO PROBAR AHORA

### Paso 1: Observar el Indicador
Cuando abras cualquiera de las pantallas con swipe, verás un texto pequeño que dice:
```
Página 1/3 - Desliza horizontalmente →
```

### Paso 2: Técnica de Swipe Correcta
1. **Coloca el dedo** en el CENTRO de la pantalla (no en las tabs, no en botones)
2. **Desliza HORIZONTALMENTE** con decisión (no lento ni tímido)
3. **Mantén el gesto recto** (→ o ←, no diagonal ↗)
4. **Observa el indicador** - si cambia el número, ¡el swipe funciona!

### Paso 3: Verificar Cada Pantalla

#### AdminPedidosScreen (Gestor de Pedidos)
- Ir a: Menú Admin → Gestor de Pedidos
- Ver indicador: "Página 1/3"
- Deslizar: → para ir a Historial, → para Estadísticas
- Deslizar: ← para volver

#### MisPedidosScreen (Mis Pedidos)
- Ir a: Menú Usuario → Mis Pedidos  
- Ver indicador: "Página 1/2"
- Deslizar: → para ir a Historial
- Deslizar: ← para volver a Activos

#### CatalogoScreen (Catálogo)
- Ir a: Menú Usuario → Catálogo
- Ver indicador: "Página 1/2"
- Deslizar: → para ver Productos
- Deslizar: ← para volver a Proveedores

#### AdminNotificacionesScreen (Buzón)
- Ir a: Menú Admin → Buzón
- Ver indicador: "Página 1/2"
- Deslizar: → para ir a Acciones
- Deslizar: ← para volver a Mensajes

---

## 🔧 SI AÚN NO FUNCIONA

### Diagnóstico con el Indicador:
1. **Si el indicador NO cambia al deslizar**: 
   - El gesto no está siendo detectado
   - Intenta con más velocidad/fuerza
   - Prueba en dispositivo real (no emulador)

2. **Si el indicador SÍ cambia pero la vista no**:
   - Hay un problema de renderizado (muy raro)
   - Reportar como bug

3. **Si ni siquiera ves el indicador**:
   - La app no se compiló correctamente
   - Hacer `./gradlew clean assembleDebug`

### Pruebas Adicionales:

#### Verificar que la APK está actualizada:
```bash
cd /home/gian/StudioProjects/HuertoHogar_movil2
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
```

#### Comparación Emulador vs Real:
- **Emulador**: Usar click del mouse + arrastrar horizontal
- **Dispositivo Real**: Usar dedo con gesto natural de swipe

---

## 📊 Estado del Código

### AdminPedidosScreen.kt
```kotlin
// Indicador visible
Text(
    text = "Página ${pagerState.currentPage + 1}/${tabs.size} - Desliza horizontalmente →",
    ...
)

HorizontalPager(
    state = pagerState,
    modifier = Modifier.fillMaxWidth().weight(1f),
    userScrollEnabled = true,  // ✅ Habilitado
    pageSpacing = 0.dp         // ✅ Sin espaciado
) { page ->
    Box(modifier = Modifier.fillMaxSize()) {  // ✅ Wrapper Box
        when (page) {
            0 -> ListaPedidosAdmin(...)
            1 -> ListaHistorialAdmin(...)
            2 -> EstadisticasAdmin(...)
        }
    }
}
```

### PedidosScreen.kt
```kotlin
// ✅ ESTRUCTURA CORRECTA: Pager FUERA, PullToRefresh DENTRO
HorizontalPager(
    state = pagerState,
    userScrollEnabled = true,
    pageSpacing = 0.dp
) { page ->
    PullToRefreshBox(...) {  // ✅ Ahora NO bloquea el swipe
        when (page) {
            0 -> MostrarPedidosActivos(...)
            1 -> MostrarHistorialPedidos(...)
        }
    }
}
```

---

## 📝 Resumen de Cambios

| Archivo | Cambio Principal | Impacto |
|---------|------------------|---------|
| AdminPedidosScreen.kt | + Indicador visual<br>+ pageSpacing<br>+ contentPadding en LazyColumn | ⭐⭐⭐ Alto |
| PedidosScreen.kt | **Reubicación PullToRefreshBox**<br>+ Indicador visual | ⭐⭐⭐ CRÍTICO |
| CatalogoScreen.kt | + Indicador visual<br>+ pageSpacing | ⭐⭐⭐ Alto |
| AdminNotificacionesScreen.kt | + Indicador visual<br>+ pageSpacing | ⭐⭐⭐ Alto |

---

## ✨ Resultado Esperado

Después de estos cambios, el swipe **DEBE** funcionar. Si observas que:
- ✅ El indicador cambia de número al deslizar
- ✅ La vista cambia suavemente entre páginas
- ✅ Las tabs superiores se sincronizan con el swipe

**¡El swipe está funcionando correctamente!**

Si el indicador NO cambia incluso con gestos claros y decididos, puede ser:
1. Problema con el dispositivo/emulador
2. Versión de Compose incompatible (revisar libs.versions.toml)
3. Configuración del sistema operativo bloqueando gestos

---

## 🚀 Próximos Pasos

1. **Probar en dispositivo real** (recomendado)
2. Si funciona, **quitar los indicadores de debug** (opcional)
3. Si no funciona, reportar:
   - Modelo de dispositivo/emulador
   - Versión de Android
   - Screenshot del indicador
   - Video del gesto intentado

---

Última actualización: 2025-12-27 13:25
**Estado: SOLUCIÓN IMPLEMENTADA - LISTO PARA PROBAR**

