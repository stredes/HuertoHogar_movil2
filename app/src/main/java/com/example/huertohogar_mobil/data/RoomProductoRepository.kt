package com.example.huertohogar_mobil.data

import com.example.huertohogar_mobil.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RoomProductoRepository @Inject constructor(
    private val productoDao: ProductoDao
) : ProductoRepository {
    override fun productos(): Flow<List<Producto>> = productoDao.getAllProductos()

    override suspend fun ensureSeeded() {
        val current = productoDao.getAllProductos().first()
        if (current.isEmpty()) {
            productoDao.insertAll(SeedData.productos)
        }
    }
}
