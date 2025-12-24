package com.example.huertohogar_mobil.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.PedidoRepository
import com.example.huertohogar_mobil.data.SessionManager
import com.example.huertohogar_mobil.model.Pedido
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminPedidosViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _pedidosPendientes = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidosPendientes: StateFlow<List<Pedido>> = _pedidosPendientes.asStateFlow()

    private val _todosLosPedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val todosLosPedidos: StateFlow<List<Pedido>> = _todosLosPedidos.asStateFlow()

    private val _gananciasTotal = MutableStateFlow(0)
    val gananciasTotal: StateFlow<Int> = _gananciasTotal.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        initializeObservers()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.update { true }
            try {
                // Los datos se actualizarán automáticamente a través de los listeners de Firestore
                // Solo necesitamos simular un pequeño delay para la animación
                kotlinx.coroutines.delay(500)
                Log.d("AdminPedidosViewModel", "🔄 Refresh completado")
            } finally {
                _isRefreshing.update { false }
            }
        }
    }

    private fun initializeObservers() {
        viewModelScope.launch {
            val proveedorEmail = sessionManager.getUserEmail()

            if (proveedorEmail.isNullOrEmpty()) {
                Log.e("AdminPedidosViewModel", "❌ No se pudo obtener el email del proveedor")
                return@launch
            }

            Log.d("AdminPedidosViewModel", "✅ Iniciando observadores para proveedor: $proveedorEmail")

            // Observar todos los pedidos del proveedor primero
            launch {
                pedidoRepository.getPedidosComoProveedor(proveedorEmail)
                    .catch { e ->
                        Log.e("AdminPedidosViewModel", "❌ Error al cargar todos los pedidos: ${e.message}", e)
                    }
                    .collect { pedidos ->
                        Log.d("AdminPedidosViewModel", "✅ Todos los pedidos recibidos: ${pedidos.size}")
                        val pendientes = pedidos.count { it.estado == "PENDIENTE" }
                        val confirmados = pedidos.count { it.estado == "CONFIRMADO" }
                        val pagados = pedidos.count { it.estado == "PAGADO" }
                        val listos = pedidos.count { it.estado == "LISTO_DESPACHO" }
                        val enCamino = pedidos.count { it.estado == "EN_CAMINO" }
                        val entregados = pedidos.count { it.estado == "ENTREGADO" }
                        Log.d("AdminPedidosViewModel", "   Estado: $pendientes pendientes, $confirmados confirmados, $pagados pagados, $listos listos, $enCamino en camino, $entregados entregados")

                        _todosLosPedidos.update { pedidos }

                        // Calcular pedidos activos (no entregados ni cancelados)
                        val pedidosActivos = pedidos.filter {
                            it.estado !in listOf("ENTREGADO", "CANCELADO")
                        }
                        _pedidosPendientes.update { pedidosActivos }
                        Log.d("AdminPedidosViewModel", "   Pedidos activos (requieren atención): ${pedidosActivos.size}")

                        // Calcular ganancias totales (solo pedidos entregados)
                        val ganancias = pedidos
                            .filter { it.estado == "ENTREGADO" }
                            .sumOf { it.totalCLP }
                        _gananciasTotal.update { ganancias }
                        Log.d("AdminPedidosViewModel", "   Ganancias totales: \$$ganancias")
                    }
            }
        }
    }

    fun confirmarPedido(pedidoId: String) {
        viewModelScope.launch {
            Log.d("AdminPedidosViewModel", "Confirmando pedido: $pedidoId")
            val success = pedidoRepository.confirmarPedido(pedidoId)
            if (success) {
                Log.d("AdminPedidosViewModel", "✅ Pedido confirmado")
            } else {
                Log.e("AdminPedidosViewModel", "❌ Error al confirmar pedido")
            }
        }
    }

    fun rechazarPedido(pedidoId: String) {
        viewModelScope.launch {
            Log.d("AdminPedidosViewModel", "Rechazando pedido: $pedidoId")
            val success = pedidoRepository.cancelarPedido(pedidoId)
            if (success) {
                Log.d("AdminPedidosViewModel", "✅ Pedido rechazado")
            } else {
                Log.e("AdminPedidosViewModel", "❌ Error al rechazar pedido")
            }
        }
    }

    fun marcarListoDespacho(pedidoId: String) {
        viewModelScope.launch {
            Log.d("AdminPedidosViewModel", "Marcando pedido como listo: $pedidoId")
            pedidoRepository.marcarComoListoDespacho(pedidoId)
        }
    }

    fun marcarEnCamino(pedidoId: String) {
        viewModelScope.launch {
            Log.d("AdminPedidosViewModel", "Marcando pedido en camino: $pedidoId")
            pedidoRepository.marcarEnCamino(pedidoId)
        }
    }
}
