package com.example.huertohogar_mobil.data

import androidx.room.*
import com.example.huertohogar_mobil.model.Solicitud
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {
    @Query("SELECT * FROM solicitudes WHERE receiverEmail = :email AND estado = 'PENDIENTE'")
    fun getSolicitudesPendientes(email: String): Flow<List<Solicitud>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolicitud(solicitud: Solicitud)

    @Query("UPDATE solicitudes SET estado = :nuevoEstado WHERE id = :id")
    suspend fun updateEstado(id: Int, nuevoEstado: String)
    
    @Query("DELETE FROM solicitudes WHERE id = :id")
    suspend fun deleteSolicitud(id: Int)
    
    @Query("SELECT * FROM solicitudes WHERE senderEmail = :senderEmail AND receiverEmail = :receiverEmail")
    suspend fun getSolicitud(senderEmail: String, receiverEmail: String): Solicitud?
}
