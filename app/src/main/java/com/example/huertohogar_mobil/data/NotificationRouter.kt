package com.example.huertohogar_mobil.data

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Router centralizado de notificaciones que actúa como capa de validación final
 * antes de enviar cualquier notificación o solicitud.
 *
 * Responsabilidades:
 * - Evitar notificaciones/solicitudes duplicadas
 * - Mantener registro de notificaciones recientes
 * - Validar destinatarios
 * - Logging centralizado
 */
@Singleton
class NotificationRouter @Inject constructor(
    private val socialDao: SocialDao,
    private val userDao: UserDao
) {
    private val TAG = "NotificationRouter"

    // Cache de notificaciones enviadas recientemente (email_tipo_timestamp)
    // Se limpia automáticamente después de 5 minutos
    private val recentNotifications = mutableMapOf<String, Long>()
    private val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutos

    // Tipos de notificación soportados
    enum class NotificationType {
        FRIEND_REQUEST,
        REQUEST_ACCEPTED,
        CHAT_MESSAGE,
        PEDIDO_NUEVO,
        PEDIDO_LISTO,
        PEDIDO_EN_CAMINO,
        PEDIDO_ENTREGADO,
        CONTACT_FORM
    }

    /**
     * Valida y registra una notificación antes de ser enviada
     * @return true si la notificación debe enviarse, false si debe bloquearse
     */
    suspend fun canSendNotification(
        type: NotificationType,
        fromEmail: String,
        toEmail: String,
        extraData: String? = null
    ): Boolean {
        Log.d(TAG, "🔍 Validando notificación: $type de $fromEmail para $toEmail")

        // 1. Limpiar cache antiguo
        cleanOldCache()

        // 2. Normalizar emails (lowercase, trim)
        val from = fromEmail.lowercase().trim()
        val to = toEmail.lowercase().trim()

        // 3. Validación básica
        if (from == to) {
            Log.d(TAG, "❌ Bloqueado: No se puede enviar notificación a uno mismo")
            return false
        }

        // 4. Validación específica por tipo
        return when (type) {
            NotificationType.FRIEND_REQUEST -> canSendFriendRequest(from, to)
            NotificationType.REQUEST_ACCEPTED -> canSendRequestAccepted(from, to)
            NotificationType.CHAT_MESSAGE -> canSendChatMessage(from, to, extraData)
            NotificationType.PEDIDO_NUEVO,
            NotificationType.PEDIDO_LISTO,
            NotificationType.PEDIDO_EN_CAMINO,
            NotificationType.PEDIDO_ENTREGADO -> canSendPedidoNotification(type, from, to, extraData)
            NotificationType.CONTACT_FORM -> canSendContactForm(from, to)
        }
    }

    /**
     * Valida si se puede enviar una solicitud de amistad
     */
    private suspend fun canSendFriendRequest(from: String, to: String): Boolean {
        // Obtener usuarios
        val senderUser = userDao.getUserByEmail(from)
        val receiverUser = userDao.getUserByEmail(to)

        if (senderUser == null || receiverUser == null) {
            Log.d(TAG, "❌ FRIEND_REQUEST bloqueado: Usuario no encontrado")
            return false
        }

        // 1. Verificar si ya son amigos
        val yaAmigos = socialDao.esAmigo(senderUser.id, receiverUser.id) ||
                      socialDao.esAmigo(receiverUser.id, senderUser.id)
        if (yaAmigos) {
            Log.d(TAG, "❌ FRIEND_REQUEST bloqueado: Ya son amigos")
            return false
        }

        // 2. Verificar solicitudes existentes
        val existenteDirecta = socialDao.getSolicitud(from, to)
        val existenteInversa = socialDao.getSolicitud(to, from)

        if (existenteDirecta != null && existenteDirecta.estado in listOf("PENDIENTE", "ACEPTADA")) {
            Log.d(TAG, "❌ FRIEND_REQUEST bloqueado: Ya existe solicitud directa ${existenteDirecta.estado}")
            return false
        }

        if (existenteInversa != null && existenteInversa.estado in listOf("PENDIENTE", "ACEPTADA")) {
            Log.d(TAG, "❌ FRIEND_REQUEST bloqueado: Ya existe solicitud inversa ${existenteInversa.estado}")
            return false
        }

        // 3. Verificar cache reciente
        val cacheKey = "FRIEND_REQUEST_${from}_${to}"
        if (isInRecentCache(cacheKey)) {
            Log.d(TAG, "❌ FRIEND_REQUEST bloqueado: Enviado recientemente (cache)")
            return false
        }

        // TODO OK: Registrar en cache y permitir
        addToCache(cacheKey)
        Log.d(TAG, "✅ FRIEND_REQUEST permitido de $from para $to")
        return true
    }

    /**
     * Valida si se puede enviar notificación de solicitud aceptada
     */
    private suspend fun canSendRequestAccepted(from: String, to: String): Boolean {
        val senderUser = userDao.getUserByEmail(from)
        val receiverUser = userDao.getUserByEmail(to)

        if (senderUser == null || receiverUser == null) {
            Log.d(TAG, "❌ REQUEST_ACCEPTED bloqueado: Usuario no encontrado")
            return false
        }

        // Verificar que no se haya enviado esta notificación recientemente
        val cacheKey = "REQUEST_ACCEPTED_${from}_${to}"
        if (isInRecentCache(cacheKey)) {
            Log.d(TAG, "❌ REQUEST_ACCEPTED bloqueado: Enviado recientemente")
            return false
        }

        addToCache(cacheKey)
        Log.d(TAG, "✅ REQUEST_ACCEPTED permitido de $from para $to")
        return true
    }

    /**
     * Valida si se puede enviar notificación de mensaje de chat
     */
    private fun canSendChatMessage(from: String, to: String, messageId: String?): Boolean {
        // Para mensajes de chat, permitimos siempre (el usuario decide si quiere notificaciones)
        // Pero verificamos que no sea el mismo mensaje duplicado

        if (messageId != null) {
            val cacheKey = "CHAT_${from}_${to}_${messageId}"
            if (isInRecentCache(cacheKey)) {
                Log.d(TAG, "❌ CHAT bloqueado: Mensaje duplicado")
                return false
            }
            addToCache(cacheKey)
        }

        Log.d(TAG, "✅ CHAT permitido de $from para $to")
        return true
    }

    /**
     * Valida si se puede enviar notificación de pedido
     */
    private fun canSendPedidoNotification(
        type: NotificationType,
        from: String,
        to: String,
        pedidoId: String?
    ): Boolean {
        if (pedidoId == null) {
            Log.d(TAG, "❌ PEDIDO bloqueado: Sin ID de pedido")
            return false
        }

        // Verificar que no se haya enviado esta notificación específica recientemente
        val cacheKey = "${type.name}_${pedidoId}_${from}_${to}"
        if (isInRecentCache(cacheKey)) {
            Log.d(TAG, "❌ ${type.name} bloqueado: Enviado recientemente para pedido $pedidoId")
            return false
        }

        addToCache(cacheKey)
        Log.d(TAG, "✅ ${type.name} permitido para pedido $pedidoId")
        return true
    }

    /**
     * Valida si se puede enviar formulario de contacto
     */
    private fun canSendContactForm(from: String, to: String): Boolean {
        // Verificar que no se envíen múltiples formularios en poco tiempo
        val cacheKey = "CONTACT_${from}_${to}"
        if (isInRecentCache(cacheKey)) {
            Log.d(TAG, "❌ CONTACT_FORM bloqueado: Enviado recientemente")
            return false
        }

        addToCache(cacheKey)
        Log.d(TAG, "✅ CONTACT_FORM permitido de $from")
        return true
    }

    /**
     * Verifica si una clave está en el cache reciente
     */
    private fun isInRecentCache(key: String): Boolean {
        val timestamp = recentNotifications[key]
        if (timestamp != null) {
            val elapsed = System.currentTimeMillis() - timestamp
            return elapsed < CACHE_DURATION_MS
        }
        return false
    }

    /**
     * Agrega una clave al cache
     */
    private fun addToCache(key: String) {
        recentNotifications[key] = System.currentTimeMillis()
    }

    /**
     * Limpia entradas antiguas del cache
     */
    private fun cleanOldCache() {
        val now = System.currentTimeMillis()
        val toRemove = recentNotifications.filter { (_, timestamp) ->
            now - timestamp > CACHE_DURATION_MS
        }.keys

        toRemove.forEach { key ->
            recentNotifications.remove(key)
        }

        if (toRemove.isNotEmpty()) {
            Log.d(TAG, "🧹 Cache limpiado: ${toRemove.size} entradas antiguas eliminadas")
        }
    }

    /**
     * Limpia todo el cache (útil para testing)
     */
    fun clearCache() {
        recentNotifications.clear()
        Log.d(TAG, "🧹 Cache completamente limpiado")
    }

    /**
     * Obtiene estadísticas del cache
     */
    fun getCacheStats(): String {
        cleanOldCache()
        return "Cache activo: ${recentNotifications.size} notificaciones"
    }
}

