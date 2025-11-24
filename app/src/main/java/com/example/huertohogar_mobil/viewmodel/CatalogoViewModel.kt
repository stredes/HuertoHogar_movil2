package com.example.huertohogar_mobil.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.HuertoHogarDatabase
import com.example.huertohogar_mobil.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CatalogoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = HuertoHogarDatabase.getDatabase(application)
    private val productoDao = database.productoDao()

    // Estado observable: La lista de productos
    private val _productos = MutableStateFlow(emptyList<Producto>())
    val productos: StateFlow<List<Producto>> = _productos

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            // Obtenemos los datos de la BD y actualizamos el estado
            val lista = productoDao.obtenerTodos()
            _productos.value = lista
        }
    }
}