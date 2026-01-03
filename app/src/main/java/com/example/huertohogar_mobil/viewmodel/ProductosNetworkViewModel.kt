package com.example.huertohogar_mobil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.repository.ProductosRepository
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.network.dto.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para Productos - Ejemplo de implementación MVVM
 *
 * Demuestra:
 * - Uso de StateFlow para UI reactiva
 * - Manejo de estados de carga
 * - Integración con Repository
 * - Manejo de errores
 */
@HiltViewModel
class ProductosNetworkViewModel @Inject constructor(
    private val productosRepository: ProductosRepository
) : ViewModel() {

    // Estado de UI usando StateFlow
    private val _uiState = MutableStateFlow<ProductosUiState>(ProductosUiState.Idle)
    val uiState: StateFlow<ProductosUiState> = _uiState.asStateFlow()

    // Lista de productos
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    // Producto seleccionado
    private val _productoSeleccionado = MutableStateFlow<Producto?>(null)
    val productoSeleccionado: StateFlow<Producto?> = _productoSeleccionado.asStateFlow()

    // Mensajes de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Flag de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Observar productos desde caché local
        observarProductosLocales()
    }

    /**
     * Observar productos desde la base de datos local
     */
    private fun observarProductosLocales() {
        viewModelScope.launch {
            productosRepository.getProductosLocal()
                .catch { e ->
                    _errorMessage.value = e.message
                }
                .collect { productos ->
                    _productos.value = productos
                }
        }
    }

    /**
     * Cargar productos desde el servidor
     */
    fun cargarProductos(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            productosRepository.getProductos(forceRefresh)
                .collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            _isLoading.value = true
                            _uiState.value = ProductosUiState.Loading
                        }

                        is NetworkResult.Success -> {
                            _isLoading.value = false
                            _productos.value = result.data
                            _uiState.value = ProductosUiState.Success(result.data)
                            _errorMessage.value = null
                        }

                        is NetworkResult.Error -> {
                            _isLoading.value = false
                            _errorMessage.value = result.message
                            _uiState.value = ProductosUiState.Error(result.message)
                        }

                        is NetworkResult.Idle -> {
                            _isLoading.value = false
                            _uiState.value = ProductosUiState.Idle
                        }
                    }
                }
        }
    }

    /**
     * Buscar productos
     */
    fun buscarProductos(query: String) {
        if (query.isBlank()) {
            cargarProductos()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = ProductosUiState.Loading

            when (val result = productosRepository.buscarProductos(query)) {
                is NetworkResult.Success -> {
                    _productos.value = result.data.productos.map { it.toEntity() }
                    _uiState.value = ProductosUiState.Success(
                        result.data.productos.map { it.toEntity() }
                    )
                    _isLoading.value = false
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _uiState.value = ProductosUiState.Error(result.message)
                    _isLoading.value = false
                }

                else -> {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Obtener producto por ID
     */
    fun obtenerProducto(id: String) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = productosRepository.getProducto(id)) {
                is NetworkResult.Success -> {
                    _productoSeleccionado.value = result.data
                    _isLoading.value = false
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _isLoading.value = false
                }

                else -> {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Obtener productos destacados
     */
    fun cargarDestacados() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = productosRepository.destacados(10)) {
                is NetworkResult.Success -> {
                    _productos.value = result.data
                    _uiState.value = ProductosUiState.Success(result.data)
                    _isLoading.value = false
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _uiState.value = ProductosUiState.Error(result.message)
                    _isLoading.value = false
                }

                else -> {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Filtrar por categoría
     */
    fun filtrarPorCategoria(categoria: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = ProductosUiState.Loading

            when (val result = productosRepository.porCategoria(categoria)) {
                is NetworkResult.Success -> {
                    _productos.value = result.data.productos.map { it.toEntity() }
                    _uiState.value = ProductosUiState.Success(
                        result.data.productos.map { it.toEntity() }
                    )
                    _isLoading.value = false
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
                    _uiState.value = ProductosUiState.Error(result.message)
                    _isLoading.value = false
                }

                else -> {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Sincronizar productos manualmente
     */
    fun sincronizar() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = productosRepository.sincronizar()) {
                is NetworkResult.Success -> {
                    cargarProductos(forceRefresh = true)
                    _errorMessage.value = "Sincronización exitosa"
                }

                is NetworkResult.Error -> {
                    _errorMessage.value = "Error en sincronización: ${result.message}"
                    _isLoading.value = false
                }

                else -> {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Limpiar mensaje de error
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Limpiar producto seleccionado
     */
    fun clearSelectedProduct() {
        _productoSeleccionado.value = null
    }
}

/**
 * Estados de UI para la pantalla de productos
 */
sealed class ProductosUiState {
    data object Idle : ProductosUiState()
    data object Loading : ProductosUiState()
    data class Success(val productos: List<Producto>) : ProductosUiState()
    data class Error(val message: String) : ProductosUiState()
}

/**
 * Extensión para convertir ProductoDto a Producto
 */
private fun com.example.huertohogar_mobil.network.dto.ProductoDto.toEntity(): Producto {
    return Producto(
        id = this.id,
        nombre = this.nombre,
        precioCLP = this.precioCLP,
        unidad = this.unidad,
        descripcion = this.descripcion ?: "",
        imagenRes = this.imagenRes ?: 0,
        imagenUri = this.imagenUrl,
        providerEmail = this.providerEmail,
        timestamp = this.timestamp
    )
}

