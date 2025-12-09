package com.example.huertohogar_mobil.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.huertohogar_mobil.model.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos")
    fun getAllProductos(): Flow<List<Producto>>
    
    @Query("SELECT * FROM productos")
    suspend fun getAllProductosSync(): List<Producto>

    @Query("SELECT * FROM productos WHERE providerEmail = :providerEmail")
    fun getProductosByProvider(providerEmail: String): Flow<List<Producto>>

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun getProductoByIdSync(id: String): Producto?

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<Producto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(producto: Producto)

    @Update
    suspend fun update(producto: Producto)

    @Delete
    suspend fun delete(producto: Producto)
}
