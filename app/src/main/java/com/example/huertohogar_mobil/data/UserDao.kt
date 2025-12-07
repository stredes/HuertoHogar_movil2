package com.example.huertohogar_mobil.data

import androidx.room.*
import com.example.huertohogar_mobil.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // FIX: Añadido COLLATE NOCASE para que la búsqueda de email no sea sensible a mayúsculas
    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    // FIX: Añadido COLLATE NOCASE para que el login no sea sensible a mayúsculas en el email
    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE AND passwordHash = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    // --- ROOT & ADMIN ---
    
    @Query("SELECT * FROM users WHERE role != 'root'")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users")
    suspend fun getAllUsersSync(): List<User>

    @Query("SELECT * FROM users WHERE role = 'admin'")
    fun getAllAdmins(): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Int)
    
    @Query("DELETE FROM users WHERE role != 'root'")
    suspend fun deleteAllNonRootUsers()
}
