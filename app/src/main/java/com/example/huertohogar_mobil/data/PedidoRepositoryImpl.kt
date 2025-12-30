package com.example.huertohogar_mobil.data

import android.util.Log
import com.example.huertohogar_mobil.model.EstadoPedido
import com.example.huertohogar_mobil.model.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PedidoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val pedidoDao: PedidoDao
) : PedidoRepository {

    private val TAG = "PedidoRepository"

    override fun getPedidosComoComprador(email: String): Flow<List<Pedido>> = callbackFlow {
        try {
            Log.d(TAG, "🔍 Configurando listener para pedidos del comprador: $email")
            val listener = firestore.collection("pedidos")
                .whereEqualTo("compradorEmail", email)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "❌ Error escuchando pedidos del comprador", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val pedidos = (snapshot?.toObjects(Pedido::class.java) ?: emptyList())
                        .sortedByDescending { it.fechaPedido }
                    Log.d(TAG, "📦 Pedidos del comprador recibidos: ${pedidos.size}")
                    pedidos.forEach { pedido ->
                        Log.d(TAG, "   - ${pedido.pedidoId}: ${pedido.estado} para proveedor ${pedido.proveedorEmail}")
                    }

                    // Desactivado Room: Firestore es la fuente de verdad

                    if (!trySend(pedidos).isSuccess) {
                        Log.w(TAG, "No se pudo enviar pedidos del comprador")
                    }
                }

            awaitClose {
                Log.d(TAG, "Cerrando listener de pedidos comprador")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getPedidosComoComprador", e)
            throw e
        }
    }

    override fun getPedidosComoProveedor(email: String): Flow<List<Pedido>> = callbackFlow {
        try {
            Log.d(TAG, "🔍 Configurando listener para pedidos del proveedor: $email")
            val listener = firestore.collection("pedidos")
                .whereEqualTo("proveedorEmail", email)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "❌ Error escuchando pedidos del proveedor", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val pedidos = (snapshot?.toObjects(Pedido::class.java) ?: emptyList())
                        .sortedByDescending { it.fechaPedido }
                    Log.d(TAG, "📦 Pedidos del proveedor recibidos: ${pedidos.size}")
                    pedidos.forEach { pedido ->
                        Log.d(TAG, "   - ${pedido.pedidoId}: ${pedido.estado} de ${pedido.compradorEmail}")
                    }

                    // Desactivado Room: Firestore es la fuente de verdad

                    if (!trySend(pedidos).isSuccess) {
                        Log.w(TAG, "No se pudo enviar pedidos del proveedor")
                    }
                }

            awaitClose {
                Log.d(TAG, "Cerrando listener de pedidos proveedor")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getPedidosComoProveedor", e)
            throw e
        }
    }

    override fun getPedidosPendientesProveedor(email: String): Flow<List<Pedido>> = callbackFlow {
        try {
            Log.d(TAG, "🔍 Configurando listener para pedidos PENDIENTES del proveedor: $email")
            val listener = firestore.collection("pedidos")
                .whereEqualTo("proveedorEmail", email)
                .whereEqualTo("estado", "PENDIENTE")
                // Removido orderBy para evitar problemas de índice compuesto
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "❌ Error escuchando pedidos pendientes: ${error.message}", error)
                        // Si el error es por falta de índice, loguear mensaje más claro
                        if (error.message?.contains("index") == true) {
                            Log.e(TAG, "⚠️ FALTA ÍNDICE EN FIRESTORE. Crea un índice para: proveedorEmail + estado")
                        }
                        close(error)
                        return@addSnapshotListener
                    }

                    // Ordenar en memoria después de recibir
                    val pedidos = (snapshot?.toObjects(Pedido::class.java) ?: emptyList())
                        .sortedBy { it.fechaPedido }

                    Log.d(TAG, "📦 Pedidos PENDIENTES recibidos: ${pedidos.size}")
                    pedidos.forEach { pedido ->
                        Log.d(TAG, "   - ${pedido.pedidoId}: de ${pedido.compradorNombre} (\$${pedido.totalCLP})")
                    }

                    // Desactivado Room: Firestore es la fuente de verdad

                    if (!trySend(pedidos).isSuccess) {
                        Log.w(TAG, "No se pudo enviar pedidos pendientes")
                    }
                }

            awaitClose {
                Log.d(TAG, "Cerrando listener de pedidos pendientes")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getPedidosPendientesProveedor", e)
            throw e
        }
    }

    override fun getPedidosListosDespacho(email: String): Flow<List<Pedido>> = callbackFlow {
        try {
            val listener = firestore.collection("pedidos")
                .whereEqualTo("proveedorEmail", email)
                .whereEqualTo("estado", "LISTO_DESPACHO")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error escuchando pedidos listos", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val pedidos = (snapshot?.toObjects(Pedido::class.java) ?: emptyList())
                        .sortedBy { it.fechaPedido }
                    Log.d(TAG, "Pedidos listos para despacho: ${pedidos.size}")

                    // Desactivado Room: Firestore es la fuente de verdad

                    if (!trySend(pedidos).isSuccess) {
                        Log.w(TAG, "No se pudo enviar pedidos listos")
                    }
                }

            awaitClose {
                Log.d(TAG, "Cerrando listener de pedidos listos")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en getPedidosListosDespacho", e)
            throw e
        }
    }

    override fun countPedidosPendientes(email: String): Flow<Int> = callbackFlow {
        try {
            val listener = firestore.collection("pedidos")
                .whereEqualTo("proveedorEmail", email)
                .whereEqualTo("estado", "PENDIENTE")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error contando pedidos pendientes", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    if (!trySend(snapshot?.size() ?: 0).isSuccess) {
                        Log.w(TAG, "No se pudo enviar conteo pendientes")
                    }
                }

            awaitClose {
                Log.d(TAG, "Cerrando listener de conteo pendientes")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en countPedidosPendientes", e)
            throw e
        }
    }

    override fun countNotificacionesComprador(email: String): Flow<Int> = callbackFlow {
        try {
            val listener = firestore.collection("pedidos")
                .whereEqualTo("compradorEmail", email)
                .whereNotEqualTo("estado", "ENTREGADO")
                .whereNotEqualTo("estado", "CANCELADO")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error contando notificaciones comprador", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    if (!trySend(snapshot?.size() ?: 0).isSuccess) {
                        Log.w(TAG, "No se pudo enviar conteo notificaciones")
                    }
                }

            awaitClose {
                Log.d(TAG, "Cerrando listener de notificaciones comprador")
                listener.remove()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en countNotificacionesComprador", e)
            throw e
        }
    }

    override suspend fun crearPedido(pedido: Pedido): Boolean {
        return try {
            Log.d(TAG, "Creando pedido: ${pedido.pedidoId} para proveedor: ${pedido.proveedorEmail}")

            // 1. Guardar en Firebase
            firestore.collection("pedidos").document(pedido.pedidoId).set(pedido).await()

            // Desactivado Room: Firestore es la fuente de verdad

            // 3. Crear notificación para el proveedor en la colección de mensajes
            try {
                val notificacionData = hashMapOf(
                    "id" to "notif_${pedido.pedidoId}",
                    "senderEmail" to pedido.compradorEmail,
                    "senderName" to (pedido.compradorNombre ?: "Cliente"),
                    "receiverEmail" to pedido.proveedorEmail,
                    "content" to "Nuevo pedido recibido de ${pedido.compradorNombre ?: pedido.compradorEmail}. Total: \$${pedido.totalCLP}",
                    "type" to "PEDIDO_NUEVO",
                    "timestamp" to System.currentTimeMillis(),
                    "pedidoId" to pedido.pedidoId,
                    "read" to false
                )
                firestore.collection("messages").document("notif_${pedido.pedidoId}").set(notificacionData).await()
                Log.d(TAG, "✅ Notificación enviada al proveedor")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ No se pudo enviar notificación al proveedor: ${e.message}")
            }

            Log.d(TAG, "✅ Pedido creado exitosamente: ${pedido.pedidoId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando pedido", e)
            false
        }
    }

    override suspend fun confirmarPedido(pedidoId: String): Boolean {
        return updateEstadoPedido(pedidoId, EstadoPedido.CONFIRMADO)
    }

    override suspend fun seleccionarMetodoPago(
        pedidoId: String,
        metodo: String,
        datosTransferencia: String?
    ): Boolean {
        return try {
            val updates = mapOf(
                "metodoPago" to metodo,
                "datosTransferencia" to (datosTransferencia ?: ""),
                "ultimaActualizacion" to System.currentTimeMillis()
            )
            firestore.collection("pedidos").document(pedidoId).update(updates).await()
            Log.d(TAG, "Método de pago seleccionado para: $pedidoId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error seleccionando método de pago", e)
            false
        }
    }

    override suspend fun marcarComoPagado(pedidoId: String): Boolean {
        return updateEstadoPedido(pedidoId, EstadoPedido.PAGADO)
    }

    override suspend fun marcarComoListoDespacho(pedidoId: String): Boolean {
        return try {
            // 1. Actualizar estado del pedido
            val updates = mapOf(
                "estado" to EstadoPedido.LISTO_DESPACHO.toString(),
                "ultimaActualizacion" to System.currentTimeMillis()
            )
            firestore.collection("pedidos").document(pedidoId).update(updates).await()
            Log.d(TAG, "✅ Pedido $pedidoId marcado como LISTO_DESPACHO")

            // 2. Obtener datos del pedido para enviar notificación
            try {
                val pedidoSnapshot = firestore.collection("pedidos").document(pedidoId).get().await()
                val pedido = pedidoSnapshot.toObject(Pedido::class.java)

                if (pedido != null) {
                    // 3. Enviar notificación al cliente
                    val notificacionData = hashMapOf(
                        "id" to "notif_listo_${pedidoId}",
                        "senderEmail" to pedido.proveedorEmail,
                        "senderName" to "Proveedor",
                        "receiverEmail" to pedido.compradorEmail,
                        "content" to "📦 Tu pedido está listo y será enviado pronto. ¡Mantente atento!",
                        "type" to "PEDIDO_LISTO",
                        "timestamp" to System.currentTimeMillis(),
                        "pedidoId" to pedidoId,
                        "read" to false
                    )
                    firestore.collection("messages").document("notif_listo_${pedidoId}").set(notificacionData).await()
                    Log.d(TAG, "✅ Notificación LISTO_DESPACHO enviada al cliente ${pedido.compradorEmail}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ No se pudo enviar notificación de LISTO_DESPACHO: ${e.message}")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marcando pedido LISTO_DESPACHO", e)
            false
        }
    }

    override suspend fun marcarEnCamino(pedidoId: String): Boolean {
        return try {
            // 1. Actualizar estado del pedido
            val updates = mapOf(
                "estado" to EstadoPedido.EN_CAMINO.toString(),
                "ultimaActualizacion" to System.currentTimeMillis(),
                "fechaDespacho" to System.currentTimeMillis()
            )
            firestore.collection("pedidos").document(pedidoId).update(updates).await()
            Log.d(TAG, "✅ Pedido $pedidoId marcado como EN_CAMINO")

            // 2. Obtener datos del pedido para enviar notificación
            try {
                val pedidoSnapshot = firestore.collection("pedidos").document(pedidoId).get().await()
                val pedido = pedidoSnapshot.toObject(Pedido::class.java)

                if (pedido != null) {
                    // 3. Enviar notificación al cliente
                    val notificacionData = hashMapOf(
                        "id" to "notif_encamino_${pedidoId}",
                        "senderEmail" to pedido.proveedorEmail,
                        "senderName" to "Proveedor",
                        "receiverEmail" to pedido.compradorEmail,
                        "content" to "🚚 ¡Tu pedido está en camino! El delivery llegará pronto a tu dirección. Por favor, confirma la recepción cuando lo recibas.",
                        "type" to "PEDIDO_EN_CAMINO",
                        "timestamp" to System.currentTimeMillis(),
                        "pedidoId" to pedidoId,
                        "read" to false
                    )
                    firestore.collection("messages").document("notif_encamino_${pedidoId}").set(notificacionData).await()
                    Log.d(TAG, "✅ Notificación EN_CAMINO enviada al cliente ${pedido.compradorEmail}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ No se pudo enviar notificación de EN_CAMINO: ${e.message}")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marcando pedido EN_CAMINO", e)
            false
        }
    }

    override suspend fun marcarEntregado(pedidoId: String): Boolean {
        return try {
            // 1. Actualizar estado del pedido
            val updates = mapOf(
                "estado" to EstadoPedido.ENTREGADO.toString(),
                "ultimaActualizacion" to System.currentTimeMillis(),
                "fechaEntrega" to System.currentTimeMillis()
            )
            firestore.collection("pedidos").document(pedidoId).update(updates).await()
            Log.d(TAG, "✅ Pedido $pedidoId marcado como ENTREGADO")

            // 2. Obtener datos del pedido para enviar notificación al proveedor
            try {
                val pedidoSnapshot = firestore.collection("pedidos").document(pedidoId).get().await()
                val pedido = pedidoSnapshot.toObject(Pedido::class.java)

                if (pedido != null) {
                    // 3. Enviar notificación al proveedor
                    val notificacionData = hashMapOf(
                        "id" to "notif_entregado_${pedidoId}",
                        "senderEmail" to pedido.compradorEmail,
                        "senderName" to (pedido.compradorNombre ?: "Cliente"),
                        "receiverEmail" to pedido.proveedorEmail,
                        "content" to "✅ ${pedido.compradorNombre ?: "El cliente"} confirmó la recepción del pedido. Total: \$${pedido.totalCLP}",
                        "type" to "PEDIDO_ENTREGADO",
                        "timestamp" to System.currentTimeMillis(),
                        "pedidoId" to pedidoId,
                        "read" to false
                    )
                    firestore.collection("messages").document("notif_entregado_${pedidoId}").set(notificacionData).await()
                    Log.d(TAG, "✅ Notificación ENTREGADO enviada al proveedor ${pedido.proveedorEmail}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ No se pudo enviar notificación de ENTREGADO: ${e.message}")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marcando pedido ENTREGADO", e)
            false
        }
    }

    override suspend fun cancelarPedido(pedidoId: String, motivo: String?): Boolean {
        return try {
            val updates = mutableMapOf(
                "estado" to EstadoPedido.CANCELADO.toString(),
                "ultimaActualizacion" to System.currentTimeMillis()
            )
            if (!motivo.isNullOrBlank()) {
                updates["motivoCancelacion"] = motivo
            }
            firestore.collection("pedidos").document(pedidoId).update(updates as Map<String, Any>).await()
            Log.d(TAG, "Pedido $pedidoId cancelado. Motivo: ${motivo ?: "No especificado"}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelando pedido", e)
            false
        }
    }

    private suspend fun updateEstadoPedido(pedidoId: String, nuevoEstado: EstadoPedido): Boolean {
        return try {
            val updates = mapOf(
                "estado" to nuevoEstado.toString(),
                "ultimaActualizacion" to System.currentTimeMillis()
            )
            firestore.collection("pedidos").document(pedidoId).update(updates).await()
            Log.d(TAG, "Pedido $pedidoId actualizado a: $nuevoEstado")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando estado del pedido", e)
            false
        }
    }

    override suspend fun inicializarListeners(email: String, esProveedor: Boolean) {
        Log.d(TAG, "Inicializando listeners para $email (proveedor: $esProveedor)")
        // Los listeners se inician automáticamente al recopilar los flows
    }
}

//COMPRADOR                                PROVEEDOR/ADMIN
//┌──────────────┐                        ┌──────────────┐
//│ Crea Pedido  │──────────────────────→│ Recibe Pedido│ (PENDIENTE)
//│ (PENDIENTE)  │                        │              │
//└──────────────┘                        └──────────────┘
//                                        │ Confirma
//                                        ↓
//                                        ┌──────────────┐
//                                        │ CONFIRMADO   │──┐
//                                        │              │  │
//                                        └──────────────┘  │
//                                                          │
//┌──────────────┐                        ┌──────────────┐  │
//│ Selecciona   │←────────────────────   │ Espera Pago  │  │
//│ Método Pago  │                        │ (Detalles:   │  │
//│              │                        │  Transf./    │  │
//└──────────────┘                        │  Tarjeta)    │  │
//│ Realiza Pago │                        └──────────────┘  │
//│ (PAGADO)     │──────────────────────→│ Confirmado   │←─ ┘
//└──────────────┘                       │ PAGADO       │
//                                       └──────────────┘
//                                       │ Empacar
//                                       ↓
//                                       ┌──────────────┐
//                                       │ LISTO_       │
//                                       │ DESPACHO     │
//                                       └──────────────┘
//                                        │ Enviar
//                                        ↓
//                                        ┌──────────────┐
//┌──────────────┐                        │ EN_CAMINO    │
//│ Recibe Push  │←─────────────────────│                │
//│ "En Camino"  │                        └──────────────┘
//└──────────────┘                        │ Entregar
//│ Confirma                              ↓
//│ Entrega                               ┌──────────────┐
//↓                                       │ ENTREGADO    │
//┌──────────────┐                        │ ✅ CIERRE     │
//│ ENTREGADO    │←─────────────────────  │              │
//│ ✅ CIERRE     │                        └──────────────┘
//└──────────────┘