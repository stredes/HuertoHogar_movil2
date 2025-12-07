package com.example.huertohogar_mobil.data

import androidx.room.*
import com.example.huertohogar_mobil.model.Amistad
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {

    // --- AMIGOS ---

    @Query("""
        SELECT u.* FROM users u
        INNER JOIN amistades a ON u.id = a.amigoId
        WHERE a.usuarioId = :miId
    """)
    fun getAmigos(miId: Int): Flow<List<User>>

    @Query("""
        SELECT * FROM users 
        WHERE id != :miId 
        AND id NOT IN (SELECT amigoId FROM amistades WHERE usuarioId = :miId)
        AND name LIKE '%' || :query || '%'
    """)
    suspend fun buscarUsuarios(miId: Int, query: String): List<User>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregarAmigo(amistad: Amistad)

    // --- CHAT ---

    @Query("""
        SELECT * FROM mensajes_chat 
        WHERE (remitenteId = :yoId AND destinatarioId = :otroId) 
           OR (remitenteId = :otroId AND destinatarioId = :yoId)
        ORDER BY timestamp ASC
    """)
    fun getConversacion(yoId: Int, otroId: Int): Flow<List<MensajeChat>>

    // Versión síncrona (suspend) para P2P Sync
    @Query("""
        SELECT * FROM mensajes_chat 
        WHERE (remitenteId = :yoId AND destinatarioId = :otroId) 
           OR (remitenteId = :otroId AND destinatarioId = :yoId)
        ORDER BY timestamp ASC
    """)
    suspend fun getConversacionSync(yoId: Int, otroId: Int): List<MensajeChat>

    // Para sincronizar TODO el historial (backup de mi otro dispositivo)
    @Query("SELECT * FROM mensajes_chat")
    suspend fun getAllMensajesSync(): List<MensajeChat>

    @Query("SELECT * FROM mensajes_chat WHERE remitenteId = :remitenteId AND destinatarioId = :destinatarioId AND estado IN (0, 2)")
    suspend fun getMensajesPendientes(remitenteId: Int, destinatarioId: Int): List<MensajeChat>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMensaje(mensaje: MensajeChat): Long

    @Query("UPDATE mensajes_chat SET estado = :nuevoEstado WHERE id = :mensajeId")
    suspend fun updateEstado(mensajeId: Long, nuevoEstado: Int)

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM mensajes_chat 
            WHERE remitenteId = :remitenteId 
            AND destinatarioId = :destinatarioId 
            AND timestamp = :timestamp 
            AND contenido = :contenido
        )
    """)
    suspend fun existeMensaje(remitenteId: Int, destinatarioId: Int, timestamp: Long, contenido: String): Boolean
}
