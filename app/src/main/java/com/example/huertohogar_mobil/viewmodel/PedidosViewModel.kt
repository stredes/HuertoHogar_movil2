package com.example.huertohogar_mobil.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.PedidoRepository
import com.example.huertohogar_mobil.data.SessionManager
import com.example.huertohogar_mobil.data.remote.RemotePedidoRepository
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
class PedidosViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository,
    private val sessionManager: SessionManager,
    private val remotePedidoRepository: RemotePedidoRepository
) : ViewModel() {

    private val _misPedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val misPedidos: StateFlow<List<Pedido>> = _misPedidos.asStateFlow()

    init {
        initializeObserver()
    }

    private fun initializeObserver() {
        viewModelScope.launch {
            val userEmail = sessionManager.getUserEmail()

            if (userEmail.isNullOrEmpty()) {
                Log.e("PedidosViewModel", "❌ No se pudo obtener el email del usuario")
                return@launch
            }

            Log.d("PedidosViewModel", "✅ Iniciando observador de pedidos para: $userEmail")

            remotePedidoRepository.getPedidosComoComprador(userEmail)
                .catch { e ->
                    Log.e("PedidosViewModel", "❌ Error al cargar pedidos: ${e.message}")
                }
                .collect { pedidos ->
                    Log.d("PedidosViewModel", "✅ Pedidos recibidos: ${pedidos.size}")
                    _misPedidos.update { pedidos }
                }
        }
    }
}
