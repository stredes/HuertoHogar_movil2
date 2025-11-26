package com.example.huertohogar_mobil.data

import androidx.room.*
import com.example.huertohogar_mobil.model.MensajeContacto
import kotlinx.coroutines.flow.Flow

@Dao
interface MensajeDao {
    @Query("SELECT * FROM mensajes_contacto ORDER BY id DESC")
    fun getAllMensajes(): Flow<List<MensajeContacto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMensaje(mensaje: MensajeContacto)

    @Delete
    suspend fun deleteMensaje(mensaje: MensajeContacto)

    @Query("UPDATE mensajes_contacto SET respondido = :estado WHERE id = :id")
    suspend fun marcarComoRespondido(id: Int, estado: Boolean)
}
