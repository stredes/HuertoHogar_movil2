package com.example.huertohogar_mobil.utils

import android.util.Log

private const val TAG = "EmailValidator"

/**
 * Utilidad para validar y clasificar emails en la aplicación
 *
 * Soporta tres tipos de usuarios:
 * 1. Root: email = "root" (usuario especial del sistema)
 * 2. Administradores ficticios: formato admin@(algo).com
 * 3. Usuarios regulares: correos reales (gianlucassanmartin@gmail.com, etc.)
 */
object EmailValidator {

    /**
     * Verifica si es el usuario root especial (email = "root")
     */
    fun esRoot(email: String): Boolean {
        return email.trim().lowercase() == "root"
    }

    /**
     * Verifica si un email corresponde a un administrador ficticio
     *
     * Patrón: admin@(algo).com donde (algo) no es un dominio de email real conocido
     * Ejemplos válidos: admin@huertohogar.com, admin@huevosonline.com
     * Ejemplos inválidos: admin@gmail.com (porque gmail es real)
     */
    fun esAdminFicticio(email: String): Boolean {
        val emailLower = email.trim().lowercase()

        // Root no es admin ficticio
        if (esRoot(emailLower)) {
            return false
        }

        // Debe empezar con "admin@"
        if (!emailLower.startsWith("admin@")) {
            return false
        }

        // Extraer el dominio
        val domain = emailLower.substringAfter("@")

        // Lista de dominios de email REALES conocidos
        // Si el dominio es de aquí, NO es un admin ficticio
        val dominiosRealesConocidos = listOf(
            "gmail.com",
            "hotmail.com",
            "outlook.com",
            "yahoo.com",
            "icloud.com",
            "protonmail.com",
            "mail.com",
            "aol.com",
            "yandex.com"
        )

        if (dominiosRealesConocidos.contains(domain)) {
            Log.d(TAG, "admin@$domain NO es ficticio (dominio real conocido)")
            return false
        }

        // Si el dominio NO es real conocido, entonces es ficticio
        Log.d(TAG, "admin@$domain ES ficticio (dominio no es real conocido)")
        return true
    }

    /**
     * Verifica si un email es de un usuario regular (no admin ficticio, no root)
     */
    fun esUsuarioRegular(email: String): Boolean {
        return !esAdminFicticio(email) && !esRoot(email)
    }

    /**
     * Valida el formato básico de un email
     * Permite:
     * - "root" (usuario especial)
     * - Cualquier formato de email válido (real o ficticio admin)
     */
    fun esFormatoValido(email: String): Boolean {
        val trimmed = email.trim()

        // "root" es un caso especial válido
        if (esRoot(trimmed)) {
            Log.d(TAG, "✅ Email 'root' es válido (usuario especial)")
            return true
        }

        // Para cualquier otro, debe ser un email válido
        val esEmailValido = android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()

        if (esEmailValido) {
            Log.d(TAG, "✅ Email '$trimmed' tiene formato válido")
        } else {
            Log.d(TAG, "❌ Email '$trimmed' tiene formato inválido")
        }

        return esEmailValido
    }

    /**
     * Valida que un email sea aceptable para registro
     *
     * Reglas:
     * - Formato válido (ver esFormatoValido)
     * - Si es admin ficticio, solo se permite si esAdmin=true
     * - Root no se puede registrar (solo existe por defecto)
     */
    fun esValidoParaRegistro(email: String, esAdmin: Boolean = false): Boolean {
        val trimmed = email.trim()

        // Root no se puede registrar manualmente
        if (esRoot(trimmed)) {
            Log.w(TAG, "❌ No se permite registrar el usuario 'root' manualmente")
            return false
        }

        // Validar formato
        if (!esFormatoValido(trimmed)) {
            Log.w(TAG, "❌ Email '$trimmed' tiene formato inválido para registro")
            return false
        }

        // Si es admin ficticio
        if (esAdminFicticio(trimmed)) {
            if (!esAdmin) {
                Log.w(TAG, "❌ Email '$trimmed' es admin ficticio pero esAdmin=false")
                return false
            }
            Log.d(TAG, "✅ Email '$trimmed' es admin ficticio y esAdmin=true")
            return true
        }

        // Usuario regular
        Log.d(TAG, "✅ Email '$trimmed' es usuario regular válido")
        return true
    }
}

