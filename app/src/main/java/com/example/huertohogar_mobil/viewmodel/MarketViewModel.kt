package com.example.huertohogar_mobil.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.CarritoDao
import com.example.huertohogar_mobil.data.MensajeRepository
import com.example.huertohogar_mobil.data.ProductoRepository
import com.example.huertohogar_mobil.model.CarritoItem
import com.example.huertohogar_mobil.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val TAG = "DB_DEBUG_MARKET"

data class MarketUiState(
    val productos: List<Producto> = emptyList(),
    val carrito: Map<String, Int> = emptyMap(),
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

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repo: ProductoRepository,
    private val carritoDao: CarritoDao,
    private val mensajeRepo: MensajeRepository // Agregamos repo de mensajes
) : ViewModel() {

    private val _ui = MutableStateFlow(MarketUiState())
    val ui: StateFlow<MarketUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.ensureSeeded()
            
            combine(
                repo.productos(),
                carritoDao.getCarrito()
            ) { productos, itemsCarrito ->
                val carritoMap = itemsCarrito.associate { it.productoId to it.cantidad }
                
                val seleccionadoActualizado = _ui.value.seleccionado?.let { sel ->
                    productos.firstOrNull { it.id == sel.id }
                }
                
                Log.d(TAG, "🔄 Sincronizando UI: ${productos.size} productos, ${carritoMap.size} items en carrito")
                
                _ui.value.copy(
                    productos = productos,
                    carrito = carritoMap,
                    seleccionado = seleccionadoActualizado
                )
            }.collect { newState ->
                _ui.value = newState
            }
        }
    }

    fun seleccionar(p: Producto?) { _ui.update { it.copy(seleccionado = p) } }

    fun agregar(p: Producto, delta: Int = 1) {
        if (delta == 0) return
        
        viewModelScope.launch {
            val actualQty = _ui.value.carrito[p.id] ?: 0
            val nuevoQty = (actualQty + delta).coerceAtLeast(0)
            
            Log.d(TAG, "🛒 Modificando carrito: ${p.nombre} -> Cantidad: $nuevoQty")

            if (nuevoQty > 0) {
                carritoDao.insertItem(CarritoItem(p.id, nuevoQty))
            } else {
                carritoDao.deleteItem(p.id)
            }
        }
    }

    fun quitar(p: Producto) = agregar(p, -1)

    fun limpiarCarrito() {
        Log.d(TAG, "🗑️ Vaciando carrito en BD...")
        viewModelScope.launch {
            carritoDao.clearCarrito()
        }
    }

    fun setQuery(value: String) { _ui.update { it.copy(query = value) } }

    fun crearProducto(nombre: String, precio: Int, unidad: String, desc: String, uri: String?) {
        viewModelScope.launch {
            Log.d(TAG, "💾 Guardando nuevo producto...")
            val nuevo = Producto(
                id = UUID.randomUUID().toString(),
                nombre = nombre,
                precioCLP = precio,
                unidad = unidad,
                descripcion = desc,
                imagenRes = 0,
                imagenUri = uri
            )
            repo.agregarProducto(nuevo)
        }
    }

    fun editarProducto(id: String, nombre: String, precio: Int, unidad: String, desc: String, uri: String?, originalImgRes: Int) {
        viewModelScope.launch {
            Log.d(TAG, "💾 Editando producto $id...")
            val actualizado = Producto(
                id = id,
                nombre = nombre,
                precioCLP = precio,
                unidad = unidad,
                descripcion = desc,
                imagenRes = originalImgRes,
                imagenUri = uri
            )
            repo.actualizarProducto(actualizado)
        }
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            Log.d(TAG, "🗑️ Eliminando producto ${producto.id}...")
            carritoDao.deleteItem(producto.id)
            repo.eliminarProducto(producto)
        }
    }

    // Nueva función para enviar contacto
    fun enviarContacto(nombre: String, email: String, mensaje: String) {
        viewModelScope.launch {
            mensajeRepo.enviarMensaje(nombre, email, mensaje)
        }
    }
}
