package com.example.huertohogar_mobil.data.sync

import android.util.Log
import com.example.huertohogar_mobil.model.Pedido
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ConflictResolver"

@Singleton
class ConflictResolver @Inject constructor() {

    fun resolve(local: Pedido, remote: Pedido): Pedido {
        val localTimestamp = local.ultimaActualizacion
        val remoteTimestamp = remote.ultimaActualizacion

        Log.d(TAG, "Resolviendo conflicto para ${local.pedidoId}:")
        Log.d(TAG, "  Local:  ${local.estado} (timestamp=$localTimestamp)")
        Log.d(TAG, "  Remote: ${remote.estado} (timestamp=$remoteTimestamp)")

        val winner = if (remoteTimestamp > localTimestamp) {
            Log.d(TAG, "  → Remote gana (más reciente: $remoteTimestamp > $localTimestamp)")
            remote
        } else if (localTimestamp > remoteTimestamp) {
            Log.d(TAG, "  → Local gana (más reciente: $localTimestamp > $remoteTimestamp)")
            local
        } else {
            if (local.pedidoId < remote.pedidoId) {
                Log.d(TAG, "  → Local gana (desempate por ID)")
                local
            } else {
                Log.d(TAG, "  → Remote gana (desempate por ID)")
                remote
            }
        }

        Log.d(TAG, "  Resultado final: ${winner.estado} (timestamp=${winner.ultimaActualizacion})")
        return winner
    }

    fun hasConflict(local: Pedido, remote: Pedido): Boolean {
        val stateConflict = local.estado != remote.estado
        val amountConflict = local.totalCLP != remote.totalCLP

        val hasConflict = stateConflict || amountConflict

        if (hasConflict) {
            Log.w(TAG, "⚠️ Conflicto detectado en ${local.pedidoId}:")
            if (stateConflict) Log.w(TAG, "  Estado: ${local.estado} vs ${remote.estado}")
            if (amountConflict) Log.w(TAG, "  Total: ${local.totalCLP} vs ${remote.totalCLP}")
        }

        return hasConflict
    }
}
