# 🧪 GUÍA DE PRUEBA - FUNCIONALIDAD SWIPE

## 📱 Instrucciones de Prueba

### IMPORTANTE: Observa el Indicador
En **TODAS** las pantallas con swipe, ahora verás un texto que dice:
```
Página 1/3 - Desliza horizontalmente →
```

Este texto es tu guía visual. Si el número cambia cuando deslizas, **el swipe está funcionando**.

---

## 🎯 Prueba 1: Gestor de Pedidos (Admin)

### Acceso:
1. Iniciar sesión como **administrador**
2. Navegar a: **Menú → Gestor de Pedidos**

### Qué verás:
```
┌─────────────────────────────────────┐
│ ← Gestor de Pedidos                 │
├─────────────────────────────────────┤
│ [Gestión] [Historial] [Estadísticas]│  ← Tabs clickeables
├─────────────────────────────────────┤
│ Página 1/3 - Desliza horizontalmente→│  ← INDICADOR
├─────────────────────────────────────┤
│                                     │
│   📋 Lista de pedidos pendientes    │
│                                     │
└─────────────────────────────────────┘
```

### Pruebas:
- [ ] **Swipe derecha** (→): Debería cambiar a "Página 2/3" y mostrar Historial
- [ ] **Swipe derecha** (→): Debería cambiar a "Página 3/3" y mostrar Estadísticas
- [ ] **Swipe izquierda** (←): Debería volver a "Página 2/3"
- [ ] **Swipe izquierda** (←): Debería volver a "Página 1/3"
- [ ] **Click en tab "Estadísticas"**: Debería saltar a "Página 3/3"

### Resultado Esperado:
✅ El número del indicador cambia con cada swipe
✅ La vista cambia suavemente entre páginas
✅ Las tabs superiores se iluminan según la página actual

---

## 🎯 Prueba 2: Mis Pedidos (Usuario)

### Acceso:
1. Iniciar sesión como **usuario**
2. Navegar a: **Menú → Mis Pedidos**

### Qué verás:
```
┌─────────────────────────────────────┐
│ ← Mis Pedidos                       │
├─────────────────────────────────────┤
│ [Activos] [Historial]               │  ← Tabs clickeables
├─────────────────────────────────────┤
│ Página 1/2 - Desliza horizontalmente→│  ← INDICADOR
├─────────────────────────────────────┤
│                                     │
│   ✅ Pedidos activos                │
│   (Pull to refresh funciona aquí)   │
│                                     │
└─────────────────────────────────────┘
```

### Pruebas:
- [ ] **Swipe derecha** (→): Cambiar a "Página 2/2" (Historial)
- [ ] **Swipe izquierda** (←): Volver a "Página 1/2" (Activos)
- [ ] **Pull down en Activos**: Debería refrescar la lista
- [ ] **Pull down en Historial**: Debería refrescar la lista
- [ ] **Swipe mientras hace pull**: El swipe debe tener prioridad

### Resultado Esperado:
✅ El swipe funciona en ambas direcciones
✅ Pull-to-refresh funciona sin interferir con el swipe
✅ El indicador se actualiza correctamente

---

## 🎯 Prueba 3: Catálogo (Usuario)

### Acceso:
1. Iniciar sesión como **usuario**
2. Navegar a: **Menú → Catálogo**

### Qué verás:
```
┌─────────────────────────────────────┐
│ HuertoHogar - Proveedores       🛒0 │
├─────────────────────────────────────┤
│ [Proveedores] [Productos]           │  ← Tabs clickeables
├─────────────────────────────────────┤
│ Página 1/2 - Desliza horizontalmente→│  ← INDICADOR
├─────────────────────────────────────┤
│                                     │
│   👥 Selecciona un proveedor        │
│   [Card Proveedor 1]                │
│   [Card Proveedor 2]                │
│                                     │
└─────────────────────────────────────┘
```

### Pruebas:
- [ ] **Swipe derecha** (→): Cambiar a "Página 2/2" (Productos)
- [ ] **Click en proveedor**: Debería filtrar productos y cambiar a tab Productos
- [ ] **Swipe izquierda** (←): Volver a "Página 1/2" (Proveedores)
- [ ] **Scroll vertical en grid**: Debe funcionar sin interferir con swipe horizontal

### Resultado Esperado:
✅ Grid de proveedores es scrolleable verticalmente
✅ Swipe horizontal funciona independientemente
✅ Al seleccionar proveedor, cambia automáticamente a Productos

---

## 🎯 Prueba 4: Buzón (Admin)

### Acceso:
1. Iniciar sesión como **administrador**
2. Navegar a: **Menú → Buzón**

### Qué verás:
```
┌─────────────────────────────────────┐
│ ← Buzón                             │
├─────────────────────────────────────┤
│ [Mensajes] [Acciones]               │  ← Tabs clickeables
├─────────────────────────────────────┤
│ Página 1/2 - Desliza horizontalmente→│  ← INDICADOR
├─────────────────────────────────────┤
│                                     │
│   📬 Lista de mensajes              │
│   (Pull to refresh funciona aquí)   │
│                                     │
├─────────────────────────────────────┤
│ [Gestión de Pedidos]                │  ← Botón persistente
└─────────────────────────────────────┘
```

### Pruebas:
- [ ] **Swipe derecha** (→): Cambiar a "Página 2/2" (Acciones)
- [ ] **Swipe izquierda** (←): Volver a "Página 1/2" (Mensajes)
- [ ] **Pull down**: Debería refrescar mensajes
- [ ] **Botón "Gestión de Pedidos"**: Debe ser visible en ambas páginas

### Resultado Esperado:
✅ Swipe funciona entre Mensajes y Acciones
✅ Botón inferior es visible siempre
✅ Pull-to-refresh funciona en página Mensajes

---

## 🐛 Troubleshooting

### Si el Indicador NO Cambia:

#### Problema 1: Gesto no detectado
**Síntomas:** El número no cambia al deslizar
**Soluciones:**
1. Desliza con más **velocidad** y **decisión**
2. Asegúrate de que el gesto sea **horizontal** (no diagonal)
3. Empieza el gesto en el **centro de la pantalla** (no en botones)
4. Prueba en **dispositivo real** en lugar de emulador

#### Problema 2: APK no actualizada
**Síntomas:** No ves el indicador "Página X/Y"
**Solución:**
```bash
cd /home/gian/StudioProjects/HuertoHogar_movil2
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
```

#### Problema 3: Conflicto de gestos
**Síntomas:** El scroll vertical funciona pero el swipe horizontal no
**Solución:** Asegúrate de que el gesto inicial sea **más horizontal que vertical** (ángulo < 30° de la horizontal)

### Si el Indicador SÍ Cambia pero la Vista No:
Esto indica un problema de renderizado. Reportar como bug con:
- Modelo de dispositivo
- Versión de Android
- Screenshot del indicador
- Logs de la aplicación

---

## ✅ Checklist Final

Marca cada item después de probarlo:

### AdminPedidosScreen
- [ ] Swipe derecha funciona (1→2→3)
- [ ] Swipe izquierda funciona (3→2→1)
- [ ] Tabs clickeables funcionan
- [ ] Indicador se actualiza correctamente

### MisPedidosScreen
- [ ] Swipe entre Activos e Historial funciona
- [ ] Pull-to-refresh funciona en ambas páginas
- [ ] No hay interferencia entre swipe y pull

### CatalogoScreen
- [ ] Swipe entre Proveedores y Productos funciona
- [ ] Scroll vertical del grid funciona
- [ ] Selección de proveedor cambia a Productos automáticamente

### AdminNotificacionesScreen
- [ ] Swipe entre Mensajes y Acciones funciona
- [ ] Botón "Gestión de Pedidos" visible siempre
- [ ] Pull-to-refresh funciona en Mensajes

---

## 📊 Reporte de Resultados

Después de las pruebas, completa:

**Fecha de prueba:** _______________

**Dispositivo:** _______________

**Versión Android:** _______________

**Resultados:**
- AdminPedidosScreen: ⬜ ✅ / ❌
- MisPedidosScreen: ⬜ ✅ / ❌
- CatalogoScreen: ⬜ ✅ / ❌
- AdminNotificacionesScreen: ⬜ ✅ / ❌

**Observaciones:**
_______________________________________
_______________________________________
_______________________________________

**El swipe funciona correctamente:** ⬜ SÍ / ⬜ NO

---

**Si TODAS las pruebas pasan, ¡el swipe está implementado exitosamente! 🎉**

Si alguna falla, consulta TROUBLESHOOTING_SWIPE.md para más soluciones.

