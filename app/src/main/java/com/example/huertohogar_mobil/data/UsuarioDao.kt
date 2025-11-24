package com.example.huertohogar_mobil.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.huertohogar_mobil.model.Usuario

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuario WHERE correo = :correo AND contrasena = :contrasena")
    suspend fun login(correo: String, contrasena: String): Usuario?
}