package com.example.huertohogar_mobil.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.huertohogar_mobil.model.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: Producto)

    // Corregido: Devuelve un Flow para que sea reactivo
    @Query("SELECT * FROM producto")
    fun obtenerTodos(): Flow<List<Producto>>

    // Corregido: Devuelve un Flow para que sea reactivo
    @Query("SELECT * FROM producto WHERE categoria = :categoria")
    fun obtenerPorCategoria(categoria: String): Flow<List<Producto>>

    // Corregido: Devuelve un Flow para que sea reactivo
    @Query("SELECT * FROM producto WHERE id = :id LIMIT 1")
    fun obtenerPorCodigo(id: String): Flow<Producto?>
}