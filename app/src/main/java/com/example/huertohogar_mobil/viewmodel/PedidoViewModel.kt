package com.example.huertohogar_mobil.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.PedidoRepository
import com.example.huertohogar_mobil.data.SessionManager
import com.example.huertohogar_mobil.model.EstadoPedido
import com.example.huertohogar_mobil.model.Pedido
import com.example.huertohogar_mobil.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

enum class SyncStatus {
    IDLE
}

data class PedidoUiState(
    val pedidos: List<Pedido> = emptyList(),
    val misPedidos: List<Pedido> = emptyList(),
    val misPedidosActivos: List<Pedido> = emptyList(),  // Pedidos en proceso
    val misPedidosHistorial: List<Pedido> = emptyList(),  // Pedidos completados/entregados
    val pedidosRecibidos: List<Pedido> = emptyList(),
    val pedidosRecibidosActivos: List<Pedido> = emptyList(),  // Pedidos a procesar
    val pedidosRecibidosHistorial: List<Pedido> = emptyList(),  // Pedidos completados
    val isLoading: Boolean = false,
    val error: String? = null,
    val syncStatus: SyncStatus = SyncStatus.IDLE
)

@HiltViewModel
class PedidoViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidoUiState())
    val uiState: StateFlow<PedidoUiState> = _uiState.asStateFlow()

    private var currentUserEmail: String? = null
    private var esProveedor: Boolean = false

    init {
        // Inicializar automáticamente al crear el ViewModel
        initialize()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Los datos se actualizan automáticamente a través de los listeners
                kotlinx.coroutines.delay(500)
                Log.d("PedidoViewModel", "🔄 Refresh completado")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                currentUserEmail = sessionManager.getUserEmail()
                esProveedor = sessionManager.isUserProvider()

                if (currentUserEmail.isNullOrEmpty()) {
                    _uiState.update {
                        it.copy(
                            error = "Usuario no autenticado. Por favor, inicie sesión.",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                setupObservers(currentUserEmail!!, esProveedor)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error al inicializar pedidos: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun setupObservers(userEmail: String, isProvider: Boolean) {
        viewModelScope.launch {
            if (isProvider) {
                // Como proveedor: observar pedidos recibidos
                pedidoRepository.getPedidosComoProveedor(userEmail)
                    .catch { e ->
                        Log.e("PedidoViewModel", "❌ Error al cargar pedidos del proveedor: ${e.message}")
                        _uiState.update { it.copy(error = "Error al cargar pedidos: ${e.message}", isLoading = false) }
                    }
                    .collect { pedidos ->
                        Log.d("PedidoViewModel", "✅ Pedidos del proveedor actualizados: ${pedidos.size}")

                        // Separar activos de historial
                        val activos = pedidos.filter { it.estado != EstadoPedido.ENTREGADO.name && it.estado != EstadoPedido.CANCELADO.name }
                        val historial = pedidos.filter { it.estado == EstadoPedido.ENTREGADO.name || it.estado == EstadoPedido.CANCELADO.name }

                        _uiState.update {
                            it.copy(
                                pedidos = pedidos,
                                pedidosRecibidos = pedidos,
                                pedidosRecibidosActivos = activos,
                                pedidosRecibidosHistorial = historial,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } else {
                // Como comprador: observar mis pedidos
                pedidoRepository.getPedidosComoComprador(userEmail)
                    .catch { e ->
                        Log.e("PedidoViewModel", "❌ Error al cargar pedidos del comprador: ${e.message}")
                        _uiState.update { it.copy(error = "Error al cargar pedidos: ${e.message}", isLoading = false) }
                    }
                    .collect { pedidos ->
                        Log.d("PedidoViewModel", "✅ Pedidos del comprador actualizados: ${pedidos.size}")

                        // Separar activos de historial
                        val activos = pedidos.filter { it.estado != EstadoPedido.ENTREGADO.name && it.estado != EstadoPedido.CANCELADO.name }
                        val historial = pedidos.filter { it.estado == EstadoPedido.ENTREGADO.name || it.estado == EstadoPedido.CANCELADO.name }

                        _uiState.update {
                            it.copy(
                                pedidos = pedidos,
                                misPedidos = pedidos,
                                misPedidosActivos = activos,
                                misPedidosHistorial = historial,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            }
        }
    }

    fun crearPedido(
        carrito: Map<Producto, Int>,
        proveedorEmail: String,
        total: Int,
        metodoPago: String = "EFECTIVO",
        datosTransferencia: String? = null,
        direccion: String = ""
    ) {
        viewModelScope.launch {
            val compradorEmail = currentUserEmail ?: return@launch
            val compradorNombre = sessionManager.getUserName() ?: "Cliente"

            Log.d("PedidoViewModel", "🛒 Creando pedido:")
            Log.d("PedidoViewModel", "  - Comprador: $compradorNombre ($compradorEmail)")
            Log.d("PedidoViewModel", "  - Proveedor: $proveedorEmail")
            Log.d("PedidoViewModel", "  - Total: $total")
            Log.d("PedidoViewModel", "  - Método de pago: $metodoPago")
            Log.d("PedidoViewModel", "  - Dirección: $direccion")

            val detalleJson = JSONArray(
                carrito.map {
                    JSONObject().apply {
                        put("productoId", it.key.id)
                        put("nombre", it.key.nombre)
                        put("cantidad", it.value)
                        put("precio", it.key.precioCLP)
                    }
                }
            ).toString()

            val timestamp = System.currentTimeMillis()
            val nuevoPedido = Pedido(
                pedidoId = UUID.randomUUID().toString(),
                compradorEmail = compradorEmail,
                compradorNombre = compradorNombre,
                proveedorEmail = proveedorEmail,
                totalCLP = total,
                detalleJson = detalleJson,
                estado = EstadoPedido.PENDIENTE.name,
                fechaPedido = timestamp,
                ultimaActualizacion = timestamp,
                metodoPago = metodoPago,
                datosTransferencia = datosTransferencia,
                direccionEntrega = direccion
            )

            Log.d("PedidoViewModel", "📦 Pedido a guardar: ${nuevoPedido.pedidoId}")

            val success = pedidoRepository.crearPedido(nuevoPedido)
            if (success) {
                Log.d("PedidoViewModel", "✅ Pedido creado exitosamente")
            } else {
                Log.e("PedidoViewModel", "❌ Error al crear pedido")
                _uiState.update { it.copy(error = "No se pudo crear el pedido") }
            }
        }
    }

    fun confirmarPedido(pedidoId: String) = updateEstado(pedidoId, EstadoPedido.CONFIRMADO)
    fun seleccionarMetodoPago(pedidoId: String, metodo: String, datosTransferencia: String? = null) {
        viewModelScope.launch {
            val success = pedidoRepository.seleccionarMetodoPago(pedidoId, metodo, datosTransferencia)
            if (!success) {
                _uiState.update { it.copy(error = "No se pudo seleccionar el método de pago") }
            }
        }
    }
    fun confirmarEntrega(pedidoId: String) = updateEstado(pedidoId, EstadoPedido.ENTREGADO)
    fun marcarComoPagado(pedidoId: String) = updateEstado(pedidoId, EstadoPedido.PAGADO)
    fun marcarListoDespacho(pedidoId: String) = updateEstado(pedidoId, EstadoPedido.LISTO_DESPACHO)
    fun marcarEnCamino(pedidoId: String) = updateEstado(pedidoId, EstadoPedido.EN_CAMINO)
    fun marcarEntregado(pedidoId: String) = updateEstado(pedidoId, EstadoPedido.ENTREGADO)
    fun cancelarPedido(pedidoId: String) = updateEstado(pedidoId, EstadoPedido.CANCELADO)

    private fun updateEstado(pedidoId: String, nuevoEstado: EstadoPedido) {
        viewModelScope.launch {
            val success = when(nuevoEstado) {
                EstadoPedido.CONFIRMADO -> pedidoRepository.confirmarPedido(pedidoId)
                EstadoPedido.PAGADO -> pedidoRepository.marcarComoPagado(pedidoId)
                EstadoPedido.LISTO_DESPACHO -> pedidoRepository.marcarComoListoDespacho(pedidoId)
                EstadoPedido.EN_CAMINO -> pedidoRepository.marcarEnCamino(pedidoId)
                EstadoPedido.ENTREGADO -> pedidoRepository.marcarEntregado(pedidoId)
                EstadoPedido.CANCELADO -> pedidoRepository.cancelarPedido(pedidoId)
                else -> false
            }
            if (!success) {
                _uiState.update { it.copy(error = "No se pudo actualizar el estado del pedido") }
            }
        }
    }

    fun getPedidoById(pedidoId: String): Pedido? {
        return _uiState.value.pedidos.find { it.pedidoId == pedidoId }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
