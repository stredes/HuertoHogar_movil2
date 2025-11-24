package com.example.huertohogar_mobil.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.huertohogar_mobil.model.Producto

@Dao
interface ProductoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: Producto)

    @Query("SELECT * FROM producto")
    suspend fun obtenerTodos(): List<Producto>

    @Query("SELECT * FROM producto WHERE categoria = :categoria")
    suspend fun obtenerPorCategoria(categoria: String): List<Producto>

    @Query("SELECT * FROM producto WHERE code = :code LIMIT 1")
    suspend fun obtenerPorCodigo(code: String): Producto?
}