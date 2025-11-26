package com.example.huertohogar_mobil.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.ProductsRepository
import com.example.huertohogar_mobil.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProductsUiState {
    data object Loading : ProductsUiState
    data class Success(val products: List<Product>) : ProductsUiState
    data class Error(val message: String) : ProductsUiState
}

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductsRepository
) : ViewModel() {

    val uiState: StateFlow<ProductsUiState> = repository.getProductsStream()
        // SOLUCIÓN: Especificamos el tipo de salida del map al tipo general de la interfaz sellada.
        .map<List<Product>, ProductsUiState> { products -> 
            ProductsUiState.Success(products) 
        }
        .catch { emit(ProductsUiState.Error(it.message ?: "Error desconocido")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProductsUiState.Loading
        )

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus

    fun addProduct(nombre: String, precio: Int, uri: Uri?) {
        viewModelScope.launch {
            _operationStatus.value = "Subiendo..."
            val newProduct = Product(nombre = nombre, precioCLP = precio)
            val result = repository.addProduct(newProduct, uri)
            if (result.isSuccess) {
                _operationStatus.value = "Producto agregado"
            } else {
                _operationStatus.value = "Error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product.id, product.imagenUrl)
        }
    }
    
    fun clearStatus() {
        _operationStatus.value = null
    }
}
