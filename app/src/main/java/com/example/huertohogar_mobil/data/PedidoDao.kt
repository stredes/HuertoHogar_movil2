package com.example.huertohogar_mobil.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.huertohogar_mobil.model.Pedido
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
    @Query("SELECT * FROM pedidos WHERE clienteId = :clienteId ORDER BY fecha DESC")
    fun getPedidosByCliente(clienteId: String): Flow<List<Pedido>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pedidos: List<Pedido>)
}
