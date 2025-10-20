package com.example.huertohogar_mobil.data

import kotlinx.coroutines.flow.Flow

/**
 * Fuente de datos del catálogo.
 * En producción puede venir de Room o de una API; aquí lo modelamos con Flow para reactividad.
 */
interface ProductoRepository {
    /** Flujo reactivo del catálogo completo. */
    fun productos(): Flow<List<Producto>>
}
