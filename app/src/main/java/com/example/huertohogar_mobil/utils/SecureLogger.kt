package com.example.huertohogar_mobil.utils

import android.util.Log

/**
 * Sistema de logging seguro que protege datos sensibles en producción
 */
object SecureLogger {

    private const val MAX_LOG_LENGTH = 4000

    // Determinar si estamos en modo debug de forma segura
    private val isDebugMode: Boolean by lazy {
        try {
            val buildConfigClass = Class.forName("com.example.huertohogar_mobil.BuildConfig")
            val debugField = buildConfigClass.getField("DEBUG")
            debugField.getBoolean(null)
        } catch (e: Exception) {
            // Si no se puede acceder a BuildConfig, asumir modo debug por seguridad
            true
        }
    }

    // Niveles de logging
    enum class Level {
        VERBOSE, DEBUG, INFO, WARNING, ERROR
    }

    /**
     * Enmascara información sensible en los logs
     */
    private fun maskSensitiveData(message: String): String {
        if (isDebugMode) return message // En desarrollo mostrar todo

        var masked = message

        // Enmascarar correos electrónicos
        masked = masked.replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) { matchResult ->
            val email = matchResult.value
            val parts = email.split("@")
            if (parts.size == 2) {
                val username = parts[0]
                val domain = parts[1]
                val maskedUsername = if (username.length > 2) {
                    "${username.take(2)}***"
                } else {
                    "***"
                }
                "$maskedUsername@${domain.split(".").first()}***"
            } else {
                "***@***"
            }
        }

        // Enmascarar IDs numéricos largos (posibles IDs de usuario)
        masked = masked.replace(Regex("\\b\\d{6,}\\b"), "***ID***")

        // Enmascarar UUIDs
        masked = masked.replace(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"), "***UUID***")

        // Enmascarar contenido de mensajes entre comillas
        masked = masked.replace(Regex("Contenido: .+?\\.\\.\\."), "Contenido: ***")

        return masked
    }

    /**
     * Log verbose - Solo en modo debug
     */
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        if (!isDebugMode) return
        val masked = maskSensitiveData(message)
        if (masked.length > MAX_LOG_LENGTH) {
            Log.v(tag, masked.substring(0, MAX_LOG_LENGTH))
        } else {
            Log.v(tag, masked, throwable)
        }
    }

    /**
     * Log debug - Solo en modo debug
     */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (!isDebugMode) return
        val masked = maskSensitiveData(message)
        if (masked.length > MAX_LOG_LENGTH) {
            Log.d(tag, masked.substring(0, MAX_LOG_LENGTH))
        } else {
            Log.d(tag, masked, throwable)
        }
    }

    /**
     * Log info - Disponible en producción pero enmascarado
     */
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        val masked = maskSensitiveData(message)
        if (masked.length > MAX_LOG_LENGTH) {
            Log.i(tag, masked.substring(0, MAX_LOG_LENGTH))
        } else {
            Log.i(tag, masked, throwable)
        }
    }

    /**
     * Log warning - Disponible en producción pero enmascarado
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        val masked = maskSensitiveData(message)
        if (masked.length > MAX_LOG_LENGTH) {
            Log.w(tag, masked.substring(0, MAX_LOG_LENGTH))
        } else {
            Log.w(tag, masked, throwable)
        }
    }

    /**
     * Log error - Disponible en producción pero enmascarado
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val masked = maskSensitiveData(message)
        if (masked.length > MAX_LOG_LENGTH) {
            Log.e(tag, masked.substring(0, MAX_LOG_LENGTH))
        } else {
            Log.e(tag, masked, throwable)
        }
    }

    /**
     * Log de datos sensibles - SOLO en modo debug
     */
    fun sensitive(tag: String, message: String) {
        if (!isDebugMode) return
        Log.d("$tag-SENSITIVE", "🔒 $message")
    }

    /**
     * Verifica si el logging está habilitado
     */
    fun isLoggingEnabled(): Boolean = isDebugMode

    /**
     * Log de métricas que es seguro en producción
     */
    fun metric(tag: String, metricName: String, value: Any) {
        if (isDebugMode) {
            Log.d(tag, "📊 $metricName: $value")
        }
    }
}

