# Sistema de Identificación de Correos - HuertoHogar

## 📧 Tipos de Correos en la Aplicación

### 1. **Correos Ficticios de Administradores** 🎭

Estos correos están **reservados para administradores de prueba** y proveedores ficticios del sistema:

**Patrón:** `admin@[dominio-ficticio].com`

**Ejemplos válidos:**
- `admin@huertohogar.com` - Administrador principal
- `admin@huevosonline.com` - Proveedor ficticio de huevos
- `admin@verdurasfrescas.com` - Proveedor ficticio de verduras
- `admin@lacteosdelsur.com` - Proveedor ficticio de lácteos
- `admin@frutasnorte.com` - Proveedor ficticio de frutas

**Características:**
- ✅ Pueden crear productos en el catálogo
- ✅ No requieren RUT
- ✅ Son automáticamente reconocidos como proveedores
- ❌ **NO pueden ser registrados por usuarios normales**

---

### 2. **Correos Reales de Usuarios** 👤

Estos son correos de **usuarios reales** que pueden registrarse en la aplicación:

**Ejemplos válidos:**
- `gianlucassanmartin@gmail.com`
- `usuario@hotmail.com`
- `contacto@outlook.com`
- `persona@yahoo.com`
- `alguien@icloud.com`
- Cualquier email de un dominio real conocido

**Características:**
- ✅ Pueden registrarse libremente
- ✅ Requieren RUT válido para registro
- ✅ Pueden comprar productos
- ✅ Pueden agregar amigos proveedores
- ❌ No pueden usar el patrón `admin@*.com` (excepto con dominios reales)

---

### 3. **Usuario Especial: ROOT** 👑

**Email:** `root` (sin dominio)

**Características:**
- ✅ Usuario con máximos privilegios
- ✅ Puede crear administradores
- ✅ Puede gestionar toda la aplicación
- ❌ **NO puede ser registrado manualmente**
- ✅ Creado automáticamente por el sistema

---

## 🔒 Reglas de Validación

### Registro de Usuarios Nuevos:

1. **Formato de Email:**
   - Debe cumplir con el formato estándar de email (RFC 5322)
   - Validado con `android.util.Patterns.EMAIL_ADDRESS`

2. **Restricciones de Patrón:**
   - ❌ NO puede usar `admin@*.com` si el dominio no es real
   - ✅ PUEDE usar `admin@gmail.com` (dominio real)
   - ❌ NO puede usar `root`

3. **RUT:**
   - ✅ Obligatorio para usuarios normales
   - ✅ Debe ser válido (formato chileno con dígito verificador)
   - ❌ No se permiten RUT institucionales (militares/policiales)

### Login:

- ✅ Acepta **todos** los formatos válidos de email
- ✅ Permite tanto correos ficticios como reales
- ✅ Valida credenciales contra base de datos local y Firebase

---

## 🛠️ Implementación Técnica

### Clase: `EmailValidator.kt`

```kotlin
EmailValidator.esAdminFicticio(email)  // ¿Es un admin ficticio?
EmailValidator.esEmailReal(email)       // ¿Es un email real?
EmailValidator.esFormatoValido(email)   // ¿Formato válido?
EmailValidator.esValidoParaRegistro(email, esAdmin) // ¿Puede registrarse?
```

### Dominios Reales Reconocidos:
- gmail.com
- hotmail.com
- outlook.com
- yahoo.com
- icloud.com

---

## 📝 Ejemplos de Uso

### ✅ Registro Exitoso:
```
Email: gianlucassanmartin@gmail.com
RUT: 12.345.678-9
→ Usuario normal registrado
```

### ❌ Registro Rechazado:
```
Email: admin@miempresa.com
RUT: 12.345.678-9
→ Error: "Este email está reservado para administradores del sistema"
```

### ✅ Login Admin Ficticio:
```
Email: admin@huertohogar.com
Password: admin123
→ Login como administrador/proveedor
```

### ✅ Login Usuario Real:
```
Email: gianlucassanmartin@gmail.com
Password: mipassword
→ Login como usuario normal
```

---

## 🎯 Objetivo del Sistema

Este sistema permite:

1. **Separar claramente** proveedores de prueba (ficticios) de usuarios reales
2. **Proteger** los correos de administradores del sistema
3. **Permitir** que usuarios reales con cualquier correo válido se registren
4. **Evitar confusión** entre diferentes tipos de cuentas

---

Última actualización: 2025-12-27

