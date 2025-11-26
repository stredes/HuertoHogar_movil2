package com.example.huertohogar_mobil.data

import com.example.huertohogar_mobil.model.Producto
import kotlinx.coroutines.flow.Flow

/**
 * Fuente de datos del catálogo.
 */
interface ProductoRepository {
    /** Flujo reactivo del catálogo completo. */
    fun productos(): Flow<List<Producto>>

    /** Asegura que existan datos iniciales (para primera ejecución con DB). */
    suspend fun ensureSeeded()
}
