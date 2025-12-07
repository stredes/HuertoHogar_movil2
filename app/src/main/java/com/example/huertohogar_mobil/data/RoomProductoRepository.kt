package com.example.huertohogar_mobil.data

import android.util.Log
import com.example.huertohogar_mobil.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val TAG = "DB_DEBUG_PRODUCTOS"

class RoomProductoRepository @Inject constructor(
    private val productoDao: ProductoDao
) : ProductoRepository {
    
    override fun productos(): Flow<List<Producto>> = productoDao.getAllProductos()
        .onEach { list ->
            Log.d(TAG, "Recuperados ${list.size} productos de la BD Local")
        }

    override suspend fun getAllProductosSync(): List<Producto> {
        return productoDao.getAllProductosSync()
    }

    override suspend fun ensureSeeded() {
        // Siempre actualizamos los productos semilla al iniciar.
        // Esto es CRÍTICO porque guardamos 'imagenRes' (Int) en la base de datos.
        // Los IDs de recursos de Android cambian cada vez que se compila la app (R.drawable.xxx).
        // Si no actualizamos, la BD tendrá IDs viejos que apuntan a recursos incorrectos o inexistentes.
        Log.d(TAG, "Actualizando productos semilla para asegurar IDs de recursos correctos...")
        try {
            productoDao.insertAll(SeedData.productos)
            Log.d(TAG, "Seed data actualizado correctamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando seed data", e)
        }
    }

    override suspend fun agregarProducto(producto: Producto) {
        Log.d(TAG, "Intentando guardar producto nuevo: ${producto.nombre}")
        try {
            productoDao.insert(producto)
            Log.d(TAG, "✅ Producto guardado exitosamente en SQLite: ${producto.id}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando producto: ${e.message}")
            throw e
        }
    }

    override suspend fun actualizarProducto(producto: Producto) {
        Log.d(TAG, "Intentando actualizar producto: ${producto.nombre}")
        productoDao.update(producto)
        Log.d(TAG, "✅ Producto actualizado exitosamente")
    }

    override suspend fun eliminarProducto(producto: Producto) {
        Log.d(TAG, "Intentando eliminar producto: ${producto.nombre}")
        productoDao.delete(producto)
        Log.d(TAG, "✅ Producto eliminado exitosamente")
    }
    
    override suspend fun getProductCount(): Int {
        return productoDao.getCount()
    }
}
