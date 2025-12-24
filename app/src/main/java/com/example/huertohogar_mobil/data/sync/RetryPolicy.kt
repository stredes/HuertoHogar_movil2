package com.example.huertohogar_mobil.data.sync

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "RetryPolicy"

data class FailedOperation(
    val operationId: String,
    val failureTime: Long,
    val error: String,
    val retryCount: Int = 0,
    val maxRetries: Int = 5
) {
    fun shouldRetry(): Boolean = retryCount < maxRetries

    fun nextRetryDelayMs(): Long {
        // Backoff exponencial: 1s, 2s, 4s, 8s, 16s
        return 1000L * (1 shl retryCount)
    }
}

/**
 * Política de reintentos para operaciones que fallan en Firestore.
 *
 * Problema: Cuando Firebase falla (sin conexión, timeout, etc.),
 * el cambio se pierde. Esta clase mantiene una cola de operaciones
 * fallidas para reintentarlas después.
 *
 * Estrategia: Backoff exponencial (1s, 2s, 4s, 8s, 16s máximo)
 */
class RetryPolicy {

    private val failedOperations = mutableMapOf<String, FailedOperation>()
    private val mutex = Mutex()

    /**
     * Registra una operación que falló
     */
    suspend fun recordFailure(
        operation: String,
        error: String
    ) = mutex.withLock {
        val existing = failedOperations[operation]

        if (existing != null) {
            failedOperations[operation] = existing.copy(
                retryCount = existing.retryCount + 1,
                failureTime = System.currentTimeMillis(),
                error = error
            )
            Log.w(TAG, "Reintento #${existing.retryCount + 1} para $operation")
        } else {
            failedOperations[operation] = FailedOperation(
                operationId = operation,
                failureTime = System.currentTimeMillis(),
                error = error
            )
            Log.w(TAG, "Operación registrada para reintento: $operation (Razón: $error)")
        }
    }

    /**
     * Obtiene operaciones que están listas para ser reintentadas
     */
    suspend fun getReadyForRetry(): List<FailedOperation> = mutex.withLock {
        val now = System.currentTimeMillis()
        val readyList = mutableListOf<FailedOperation>()

        failedOperations.forEach { (operationId, failedOp) ->
            if (!failedOp.shouldRetry()) {
                Log.e(TAG, "❌ Operación agotó reintentos: $operationId")
                return@forEach
            }

            val nextRetryTime = failedOp.failureTime + failedOp.nextRetryDelayMs()
            if (now >= nextRetryTime) {
                readyList.add(failedOp)
            }
        }

        return@withLock readyList
    }

    /**
     * Marca una operación como completada (remove de la cola)
     */
    suspend fun markSuccessful(operationId: String) = mutex.withLock {
        if (failedOperations.remove(operationId) != null) {
            Log.d(TAG, "✅ Operación resuelta exitosamente: $operationId")
        }
    }

    /**
     * Obtiene el número de operaciones pendientes de reintento
     */
    suspend fun getPendingRetryCount(): Int = mutex.withLock {
        return@withLock failedOperations.size
    }

    /**
     * Procesa reintentos pendientes (debe ser llamado desde background worker)
     * @return Número de operaciones procesadas
     */
    suspend fun processPendingRetries(): Int = mutex.withLock {
        val readyOps = getReadyForRetry()
        Log.d(TAG, "Procesando ${readyOps.size} operaciones listas para reintento")

        // Aquí normalmente se reintentarían en Firebase, pero el loop actual
        // debe ser implementado en PedidoRepositoryImpl
        return@withLock readyOps.size
    }

    /**
     * Limpia operaciones que no se pueden reintentar (agotadas)
     */
    suspend fun cleanupExhausted() = mutex.withLock {
        val exhausted = failedOperations.filter { !it.value.shouldRetry() }
        exhausted.forEach { (operationId, _) ->
            failedOperations.remove(operationId)
            Log.d(TAG, "Operación agotada removida: $operationId")
        }
    }
}

