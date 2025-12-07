package com.example.huertohogar_mobil.data

import android.util.Log
import com.example.huertohogar_mobil.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private const val TAG = "DB_DEBUG_USER"

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    // Método para asegurar que existe el usuario root
    suspend fun ensureRootUser() {
        if (userDao.getUserByEmail("root") == null) {
            val rootUser = User(
                name = "Super Usuario",
                email = "root",
                passwordHash = "root", // Clave por defecto
                role = "root"
            )
            userDao.insertUser(rootUser)
            Log.d(TAG, "✅ Usuario ROOT creado por defecto.")
        }
    }

    suspend fun registerUser(name: String, email: String, passwordHash: String): Boolean {
        Log.d(TAG, "Intentando registrar: $email")
        return if (userDao.getUserByEmail(email) == null) {
            // Solo usuarios normales se registran por la pantalla pública
            val newUser = User(
                name = name, 
                email = email, 
                passwordHash = passwordHash,
                role = "user"
            )
            userDao.insertUser(newUser)
            Log.d(TAG, "✅ Usuario creado en DB: ${newUser.email}")
            true
        } else {
            Log.w(TAG, "❌ Fallo registro: El email $email ya existe en la DB")
            false
        }
    }
    
    // Método exclusivo para Root para crear Admins
    suspend fun createAdmin(name: String, email: String, passwordHash: String): Boolean {
        if (userDao.getUserByEmail(email) == null) {
            val newAdmin = User(
                name = name,
                email = email,
                passwordHash = passwordHash,
                role = "admin"
            )
            userDao.insertUser(newAdmin)
            Log.d(TAG, "✅ Nuevo ADMIN creado por Root: $email")
            return true
        }
        return false
    }
    
    // CRUD Generico
    suspend fun createUser(user: User): Boolean {
        if (userDao.getUserByEmail(user.email) == null) {
            userDao.insertUser(user)
            return true
        }
        return false
    }

    suspend fun updateUser(user: User) {
        // En Room, insert con REPLACE funciona como update si el ID existe
        userDao.insertUser(user)
    }

    suspend fun loginUser(email: String, passwordHash: String): User? {
        ensureRootUser() // Aseguramos root al intentar loguear
        
        Log.d(TAG, "Buscando credenciales en DB para: $email")
        val user = userDao.login(email, passwordHash)
        
        if (user != null) {
             Log.d(TAG, "✅ Login exitoso. Usuario encontrado: ${user.name} (Rol: ${user.role})")
             
             // Autocorrección de admin antiguo (opcional, mantengo para compatibilidad)
             if (email == "admin@huertohogar.com" && user.role != "admin") {
                val adminUser = user.copy(role = "admin")
                userDao.insertUser(adminUser)
                return adminUser
            }
        } else {
             Log.w(TAG, "❌ Login fallido. Credenciales incorrectas o usuario no existe.")
        }
        
        return user
    }

    fun getAllAdmins(): Flow<List<User>> = userDao.getAllAdmins()
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers() // Todos los usuarios
    
    suspend fun getAllUsersSync(): List<User> = userDao.getAllUsersSync() // Metodo sincrono para sync
    
    suspend fun deleteUser(userId: Int) = userDao.deleteUser(userId)
    suspend fun getUserCount(): Int = userDao.getUserCount()
    suspend fun nukeUsers() = userDao.deleteAllNonRootUsers()
}
