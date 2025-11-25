package com.example.huertohogar_mobil.data

import com.example.huertohogar_mobil.model.Producto
import kotlinx.coroutines.flow.Flow

/**
 * Define el contrato para la fuente de datos del catálogo.
 * Abstrae el origen de los datos (BD, API, etc.) de los ViewModels.
 */
interface ProductoRepository {
    /** Flujo reactivo del catálogo completo de productos. */
    fun productos(): Flow<List<Producto>>

    /** 
     * Asegura que la base de datos tenga los datos iniciales.
     * En nuestro caso, la BD ya se puebla sola la primera vez que se crea,
     * pero esta función permite asegurar que no esté vacía en futuras ejecuciones.
     */
    suspend fun ensureSeeded()
}