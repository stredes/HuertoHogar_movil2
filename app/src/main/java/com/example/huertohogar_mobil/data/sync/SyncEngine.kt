package com.example.huertohogar_mobil.data.sync

import android.util.Log
import com.example.huertohogar_mobil.model.Pedido
import com.example.huertohogar_mobil.model.EstadoPedido
import com.example.huertohogar_mobil.data.PedidoDao
import com.example.huertohogar_mobil.data.FirebaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SyncEngine"

@Singleton
class SyncEngine @Inject constructor(
    private val pedidoDao: PedidoDao,
    private val firebaseRepository: FirebaseRepository,
    private val stateValidator: StateValidator,
    private val conflictResolver: ConflictResolver
) {
    private val syncScope = CoroutineScope(Dispatchers.IO)

    suspend fun updatePedidoState(
        pedidoId: String,
        newState: EstadoPedido,
        metodoPago: String? = null,
        datosTransferencia: String? = null,
        fechaDespacho: Long? = null,
        fechaEntrega: Long? = null
    ): Boolean = withContext(syncScope.coroutineContext) {
        val localPedido = pedidoDao.getPedidoById(pedidoId)
        if (localPedido == null) {
            Log.e(TAG, "Error: Pedido local no encontrado para actualizar: $pedidoId")
            return@withContext false
        }

        val currentState = try {
            EstadoPedido.valueOf(localPedido.estado)
        } catch (e: Exception) {
            EstadoPedido.PENDIENTE
        }

        if (!stateValidator.isValidStateTransition(currentState, newState)) {
            return@withContext false
        }

        val newTimestamp = System.currentTimeMillis()
        val updatedPedido = localPedido.copy(
            estado = newState.name,
            ultimaActualizacion = newTimestamp,
            metodoPago = metodoPago ?: localPedido.metodoPago,
            datosTransferencia = datosTransferencia ?: localPedido.datosTransferencia,
            fechaDespacho = fechaDespacho ?: localPedido.fechaDespacho,
            fechaEntrega = fechaEntrega ?: localPedido.fechaEntrega
        )

        try {
            pedidoDao.insertPedido(updatedPedido)
            val success = firebaseRepository.actualizarEstadoPedido(
                pedidoId = updatedPedido.pedidoId,
                estado = newState,
                metodoPago = updatedPedido.metodoPago,
                datosTransferencia = updatedPedido.datosTransferencia,
                fechaDespacho = updatedPedido.fechaDespacho,
                ultimaActualizacion = newTimestamp
            )
            if (!success) {
                Log.w(TAG, "Fallo al actualizar pedido en Firebase. Se reintentará más tarde.")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al actualizar estado del pedido", e)
            false
        }
    }
}
