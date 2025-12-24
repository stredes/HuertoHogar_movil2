package com.example.huertohogar_mobil.data.sync

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "DeduplicationStrategy"

data class ChangeSignature(
    val pedidoId: String,
    val timestamp: Long,
    val contenidoHash: String
)

/**
 * Estrategia de deduplicación para evitar procesar el mismo cambio múltiples veces.
 *
 * Problema: Los listeners de Firestore pueden dispararse múltiples veces para el mismo
 * documento cambio cuando hay cambios rápidos o reconexiones. Esta clase mantiene un
 * registro de cambios ya procesados para evitar duplicados.
 *
 * Límite: Mantiene los últimos 1000 cambios en memoria. Los cambios más antiguos
 * de 5 minutos se descartan automáticamente.
 */
class DeduplicationStrategy {

    private val processedChanges = mutableMapOf<String, ChangeSignature>()
    private val mutex = Mutex()
    private val CLEANUP_INTERVAL_MS = 5 * 60 * 1000L // 5 minutos
    private var lastCleanupTime = System.currentTimeMillis()

    /**
     * Detecta si un cambio ya ha sido procesado
     */
    suspend fun isDuplicate(
        pedidoId: String,
        timestamp: Long,
        contenidoHash: String
    ): Boolean = mutex.withLock {
        cleanupIfNeeded()

        val key = buildKey(pedidoId, timestamp)
        val existing = processedChanges[key]

        if (existing != null && existing.contenidoHash == contenidoHash) {
            Log.d(TAG, "Cambio duplicado detectado para $pedidoId (timestamp=$timestamp)")
            return true
        }

        return false
    }

    /**
     * Registra un cambio como procesado
     */
    suspend fun recordChange(
        pedidoId: String,
        timestamp: Long,
        contenidoHash: String
    ) = mutex.withLock {
        val key = buildKey(pedidoId, timestamp)
        processedChanges[key] = ChangeSignature(pedidoId, timestamp, contenidoHash)

        Log.d(TAG, "Cambio registrado: $pedidoId (timestamp=$timestamp)")

        // Limitar tamaño de memoria
        if (processedChanges.size > 1000) {
            Log.w(TAG, "Cache de deduplicación alcanzó 1000 elementos, limpiando...")
            processedChanges.clear()
        }
    }

    /**
     * Limpia cambios antiguos de forma periódica
     */
    private fun cleanupIfNeeded() {
        val now = System.currentTimeMillis()
        if (now - lastCleanupTime < CLEANUP_INTERVAL_MS) {
            return
        }

        val threshold = now - CLEANUP_INTERVAL_MS
        val toRemove = processedChanges.filter { it.value.timestamp < threshold }.keys

        toRemove.forEach {
            processedChanges.remove(it)
        }

        lastCleanupTime = now

        if (toRemove.isNotEmpty()) {
            Log.d(TAG, "Limpiadas ${toRemove.size} entradas de deduplicación antiguas")
        }
    }

    private fun buildKey(pedidoId: String, timestamp: Long): String {
        return "$pedidoId:$timestamp"
    }
}

