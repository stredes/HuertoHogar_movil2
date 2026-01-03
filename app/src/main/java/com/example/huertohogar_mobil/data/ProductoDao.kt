package com.example.huertohogar_mobil.data

import androidx.room.*
import com.example.huertohogar_mobil.model.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    // ========== QUERIES DE LECTURA ==========

    /**
     * Obtener todos los productos como Flow (reactivo)
     * Alias para compatibilidad con ProductosRepository
     */
    @Query("SELECT * FROM productos")
    fun getAllFlow(): Flow<List<Producto>>

    /**
     * Obtener todos los productos (para mantener compatibilidad)
     */
    @Query("SELECT * FROM productos")
    fun getAllProductos(): Flow<List<Producto>>
    
    /**
     * Obtener todos los productos de forma síncrona (suspend)
     * Alias para compatibilidad con ProductosRepository
     */
    @Query("SELECT * FROM productos")
    suspend fun getAll(): List<Producto>

    /**
     * Obtener todos los productos de forma síncrona (nombre original)
     */
    @Query("SELECT * FROM productos")
    suspend fun getAllProductosSync(): List<Producto>

    /**
     * Obtener producto por ID (suspend)
     * Alias para compatibilidad con ProductosRepository
     */
    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Producto?

    /**
     * Obtener producto por ID (nombre original)
     */
    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun getProductoByIdSync(id: String): Producto?

    /**
     * Buscar productos por nombre o descripción
     * Usado por búsqueda local en ProductosRepository
     */
    @Query("SELECT * FROM productos WHERE nombre LIKE :query OR descripcion LIKE :query")
    suspend fun search(query: String): List<Producto>

    /**
     * Obtener productos por proveedor
     */
    @Query("SELECT * FROM productos WHERE providerEmail = :providerEmail")
    fun getProductosByProvider(providerEmail: String): Flow<List<Producto>>

    /**
     * Contar productos
     */
    @Query("SELECT COUNT(*) FROM productos")
    suspend fun getCount(): Int

    // ========== OPERACIONES DE ESCRITURA ==========

    /**
     * Insertar múltiples productos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<Producto>)

    /**
     * Insertar un producto
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(producto: Producto)

    /**
     * Actualizar producto
     */
    @Update
    suspend fun update(producto: Producto)

    /**
     * Eliminar producto
     */
    @Delete
    suspend fun delete(producto: Producto)

    /**
     * Eliminar todos los productos
     * Usado por sincronización en ProductosRepository
     */
    @Query("DELETE FROM productos")
    suspend fun deleteAll()

    /**
     * Eliminar producto por ID
     * Usado por ProductosRepository.eliminarProducto()
     */
    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun deleteById(id: String)
}


