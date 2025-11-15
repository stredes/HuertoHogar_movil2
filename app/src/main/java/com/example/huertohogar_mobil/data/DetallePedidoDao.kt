package com.example.huertohogar_mobil.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.huertohogar_mobil.model.DetallePedido
import kotlinx.coroutines.flow.Flow

@Dao
interface DetallePedidoDao {
    @Query("SELECT * FROM detalles_pedido WHERE pedidoId = :pedidoId")
    fun getDetallesByPedido(pedidoId: String): Flow<List<DetallePedido>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(detalles: List<DetallePedido>)
}
