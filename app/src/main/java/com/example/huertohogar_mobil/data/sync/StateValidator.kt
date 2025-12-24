package com.example.huertohogar_mobil.data.sync

import android.util.Log
import com.example.huertohogar_mobil.model.EstadoPedido
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "StateValidator"

@Singleton
class StateValidator @Inject constructor() {

    fun isValidStateTransition(
        currentState: EstadoPedido,
        targetState: EstadoPedido
    ): Boolean {
        if (currentState == targetState) {
            Log.d(TAG, "Transición idempotente: $currentState → $targetState")
            return true
        }

        val isValid = when (currentState) {
            EstadoPedido.PENDIENTE -> targetState in listOf(EstadoPedido.CONFIRMADO, EstadoPedido.CANCELADO)
            EstadoPedido.CONFIRMADO -> targetState in listOf(EstadoPedido.ESPERANDO_PAGO, EstadoPedido.CANCELADO)
            EstadoPedido.ESPERANDO_PAGO -> targetState in listOf(EstadoPedido.PAGADO, EstadoPedido.CANCELADO)
            EstadoPedido.PAGADO -> targetState in listOf(EstadoPedido.LISTO_DESPACHO, EstadoPedido.CANCELADO)
            EstadoPedido.LISTO_DESPACHO -> targetState in listOf(EstadoPedido.EN_CAMINO, EstadoPedido.CANCELADO)
            EstadoPedido.EN_CAMINO -> targetState in listOf(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO)
            EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO -> false
        }

        if (isValid) {
            Log.d(TAG, "✅ Transición válida: $currentState → $targetState")
        } else {
            Log.w(TAG, "❌ Transición inválida: $currentState → $targetState")
        }

        return isValid
    }

    fun getValidNextStates(currentState: EstadoPedido): List<EstadoPedido> {
        return when (currentState) {
            EstadoPedido.PENDIENTE -> listOf(EstadoPedido.CONFIRMADO, EstadoPedido.CANCELADO)
            EstadoPedido.CONFIRMADO -> listOf(EstadoPedido.ESPERANDO_PAGO, EstadoPedido.CANCELADO)
            EstadoPedido.ESPERANDO_PAGO -> listOf(EstadoPedido.PAGADO, EstadoPedido.CANCELADO)
            EstadoPedido.PAGADO -> listOf(EstadoPedido.LISTO_DESPACHO, EstadoPedido.CANCELADO)
            EstadoPedido.LISTO_DESPACHO -> listOf(EstadoPedido.EN_CAMINO, EstadoPedido.CANCELADO)
            EstadoPedido.EN_CAMINO -> listOf(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO)
            EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO -> emptyList()
        }
    }
}
