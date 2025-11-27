package com.example.huertohogar_mobil.data

import androidx.room.*
import com.example.huertohogar_mobil.model.Amistad
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {

    // --- AMIGOS ---

    // Obtener la lista de amigos de un usuario (uniendo con la tabla de usuarios para obtener nombres)
    @Query("""
        SELECT u.* FROM users u
        INNER JOIN amistades a ON u.id = a.amigoId
        WHERE a.usuarioId = :miId
    """)
    fun getAmigos(miId: Int): Flow<List<User>>

    // Buscar usuarios que NO son mis amigos (para agregar)
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

    // Obtener conversación entre dos usuarios
    @Query("""
        SELECT * FROM mensajes_chat 
        WHERE (remitenteId = :yoId AND destinatarioId = :otroId) 
           OR (remitenteId = :otroId AND destinatarioId = :yoId)
        ORDER BY timestamp ASC
    """)
    fun getConversacion(yoId: Int, otroId: Int): Flow<List<MensajeChat>>

    @Insert
    suspend fun enviarMensaje(mensaje: MensajeChat)
}
