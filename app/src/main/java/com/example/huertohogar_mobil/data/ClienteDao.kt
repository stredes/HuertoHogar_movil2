package com.example.huertohogar_mobil.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.huertohogar_mobil.model.Cliente
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY apellido ASC, nombre ASC")
    fun getClientes(): Flow<List<Cliente>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clientes: List<Cliente>)

    @Query("SELECT * FROM clientes WHERE id = :clienteId")
    fun getCliente(clienteId: String): Flow<Cliente>
}
