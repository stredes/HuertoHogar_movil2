package com.example.huertohogar_mobil.data

import com.example.huertohogar_mobil.model.Producto
import kotlinx.coroutines.flow.Flow

interface ProductoRepository {
    fun productos(): Flow<List<Producto>>
    suspend fun getAllProductosSync(): List<Producto>
    suspend fun ensureSeeded()
    suspend fun agregarProducto(producto: Producto)
    suspend fun actualizarProducto(producto: Producto)
    suspend fun eliminarProducto(producto: Producto)
    suspend fun getProductCount(): Int
}
