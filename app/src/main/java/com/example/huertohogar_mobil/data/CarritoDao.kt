package com.example.huertohogar_mobil.data

import androidx.room.*
import com.example.huertohogar_mobil.model.CarritoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {
    @Query("SELECT * FROM carrito_items")
    fun getCarrito(): Flow<List<CarritoItem>>

    // Version sincrona para P2P Sync
    @Query("SELECT * FROM carrito_items")
    suspend fun getCarritoSync(): List<CarritoItem>

    @Query("SELECT * FROM carrito_items WHERE productoId = :productoId")
    suspend fun getItem(productoId: String): CarritoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CarritoItem)

    @Query("DELETE FROM carrito_items WHERE productoId = :productoId")
    suspend fun deleteItem(productoId: String)

    @Query("DELETE FROM carrito_items")
    suspend fun clearCarrito()
}
