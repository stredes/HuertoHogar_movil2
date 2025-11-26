package com.example.huertohogar_mobil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.ProductoRepository
import com.example.huertohogar_mobil.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado inmutable de la UI para HuertoHogar.
 */
data class MarketUiState(
    val productos: List<Producto> = emptyList(),
    val carrito: Map<String, Int> = emptyMap(), // idProducto -> cantidad
    val seleccionado: Producto? = null,
    val query: String = ""
) {
    val countCarrito: Int get() = carrito.values.sum()
    val totalCLP: Int get() = carrito.entries.sumOf { (id, qty) ->
        productos.firstOrNull { it.id == id }?.precioCLP?.times(qty) ?: 0
    }
    val productosFiltrados: List<Producto> get() {
        val q = query.trim().lowercase()
        return if (q.isEmpty()) productos
        else productos.filter { it.nombre.lowercase().contains(q) }
    }
}

/**
 * ViewModel MVVM con Hilt.
 * - Observa el catálogo desde el Repository (Flow)
 * - Expone estado inmutable para Compose
 * - Contiene la lógica de negocio del carrito
 */
@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repo: ProductoRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(MarketUiState())
    val ui: StateFlow<MarketUiState> = _ui.asStateFlow()

    init {
        // Cargar catálogo reactivo desde el repositorio
        viewModelScope.launch {
            // Asegurar que la BD tenga datos iniciales
            repo.ensureSeeded()
            
            // Observar cambios
            repo.productos().collect { list ->
                val seleccionadoActualizado = _ui.value.seleccionado?.let { sel ->
                    list.firstOrNull { it.id == sel.id }
                }
                _ui.update { it.copy(productos = list, seleccionado = seleccionadoActualizado) }
            }
        }
    }

    fun seleccionar(p: Producto?) { _ui.update { it.copy(seleccionado = p) } }

    fun agregar(p: Producto, delta: Int = 1) {
        if (delta == 0) return
        _ui.update { state ->
            val actual = state.carrito[p.id] ?: 0
            val nuevo = (actual + delta).coerceAtLeast(0)
            val nuevoMapa = if (nuevo == 0) state.carrito - p.id else state.carrito + (p.id to nuevo)
            state.copy(carrito = nuevoMapa)
        }
    }

    fun quitar(p: Producto) = agregar(p, -1)

    fun limpiarCarrito() { _ui.update { it.copy(carrito = emptyMap()) } }

    fun setQuery(value: String) { _ui.update { it.copy(query = value) } }
}
