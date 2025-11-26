package com.example.huertohogar_mobil.data

import com.example.huertohogar_mobil.model.Producto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación del repositorio que obtiene los datos desde la BD local (Room).
 * Gracias a @Inject, Hilt sabrá cómo crear esta clase y pasarle el productoDao.
 */
class ProductoRepositoryImpl @Inject constructor(
    private val productoDao: ProductoDao
) : ProductoRepository {

    override fun productos(): Flow<List<Producto>> = productoDao.obtenerTodos()

    override suspend fun ensureSeeded() {
        // En nuestra configuración actual, la base de datos se puebla en su creación.
        // Esta función podría usarse en el futuro para agregar más lógica,
        // como por ejemplo, actualizar datos desde una API externa.
    }
}