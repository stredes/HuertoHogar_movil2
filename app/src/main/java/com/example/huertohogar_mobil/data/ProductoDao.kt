package com.example.huertohogar_mobil.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.huertohogar_mobil.model.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun getProductos(): Flow<List<Producto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<Producto>)

    @Query("DELETE FROM productos")
    suspend fun deleteAll()

    @Query("SELECT * FROM productos WHERE id = :productoId")
    fun getProducto(productoId: String): Flow<Producto>
}
