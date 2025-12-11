package com.example.huertohogar_mobil.data

import android.util.Log
import com.example.huertohogar_mobil.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private const val TAG = "DB_DEBUG_USER"

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseRepository: FirebaseRepository
) {
    // Método para asegurar que existe el usuario root
    suspend fun ensureRootUser() {
        val existing = userDao.getUserByEmail("root")
        if (existing == null) {
            val rootUser = User(
                name = "Super Usuario",
                email = "root",
                passwordHash = "root", // Clave por defecto
                role = "root"
            )
            userDao.insertUser(rootUser)
            Log.d(TAG, "✅ Usuario ROOT creado por defecto.")
        } else if (existing.role != "root") {
             // Si existe pero no es root (ej. alguien se registró como 'root' antes), corregimos el rol
             Log.w(TAG, "⚠️ Usuario 'root' detectado con rol incorrecto. Corrigiendo a 'root'.")
             val fixedUser = existing.copy(role = "root")
             userDao.insertUser(fixedUser)
        }
    }

    suspend fun registerUser(name: String, email: String, passwordHash: String): Boolean {
        Log.d(TAG, "Intentando registrar: $email")
        
        // Bloquear registro manual de root
        if (email.trim().lowercase() == "root") {
            Log.w(TAG, "❌ Intento de registrar usuario reservado 'root'. Bloqueado.")
            return false
        }
        
        return if (userDao.getUserByEmail(email) == null) {
            // Solo usuarios normales se registran por la pantalla pública
            val newUser = User(
                name = name, 
                email = email, 
                passwordHash = passwordHash,
                role = "user"
            )
            userDao.insertUser(newUser)
            firebaseRepository.registerUser(newUser) // Sync con Firebase al crear
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
            firebaseRepository.registerUser(newAdmin) // Sync
            Log.d(TAG, "✅ Nuevo ADMIN creado por Root: $email")
            return true
        }
        return false
    }
    
    // CRUD Generico
    suspend fun createUser(user: User): Boolean {
        if (userDao.getUserByEmail(user.email) == null) {
            userDao.insertUser(user)
            firebaseRepository.registerUser(user) // Sync
            return true
        }
        return false
    }

    suspend fun updateUser(user: User) {
        // En Room, insert con REPLACE funciona como update si el ID existe
        userDao.insertUser(user)
        firebaseRepository.registerUser(user) // Sync
    }

    suspend fun loginUser(email: String, passwordHash: String): User? {
        ensureRootUser() // Aseguramos root al intentar loguear
        
        Log.d(TAG, "Buscando credenciales para: $email")
        
        // 1. Prioridad: Validar contra Firebase (Nube como fuente de verdad)
        // Esto permite login en dispositivos nuevos o tras borrar datos.
        var user = firebaseRepository.getUserDirectly(email)
        
        if (user != null) {
             if (user.passwordHash == passwordHash) {
                 Log.d(TAG, "✅ Usuario validado en Firebase. Sincronizando local...")
                 
                 // Preservamos el ID local si ya existía para no romper relaciones en Room
                 val existingLocal = userDao.getUserByEmail(email)
                 val userToSave = if (existingLocal != null) {
                     user.copy(id = existingLocal.id)
                 } else {
                     user.copy(id = 0) // ID 0 para autogenerar
                 }
                 
                 userDao.insertUser(userToSave)
                 
                 // Retornamos el usuario desde la DB local para tener el ID correcto
                 return userDao.getUserByEmail(email)
             } else {
                 Log.w(TAG, "❌ Password incorrecto en Firebase.")
                 // Si la nube dice que la clave está mal, denegamos el acceso (salvo que sea root)
                 if (email != "root") return null
             }
        }

        // 2. Fallback: Si Firebase falla (Offline o error), intentamos localmente
        Log.d(TAG, "⚠️ Firebase no disponible o usuario no encontrado. Intentando local...")
        user = userDao.login(email, passwordHash)
        
        if (user != null) {
             Log.d(TAG, "✅ Login local exitoso. Usuario: ${user.name} (Rol: ${user.role})")
             
             // Autocorrección de admin antiguo (opcional, mantengo para compatibilidad)
             if (email == "admin@huertohogar.com" && user.role != "admin") {
                val adminUser = user.copy(role = "admin")
                userDao.insertUser(adminUser)
                return adminUser
            }
        } else {
             Log.w(TAG, "❌ Login fallido localmente también.")
        }
        
        return user
    }
    
    suspend fun verifyUserExists(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }
    
    suspend fun getUser(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    // Alias para compatibilidad con MarketViewModel
    suspend fun getUserByEmail(email: String): User? = getUser(email)

    suspend fun resetPassword(email: String, newPass: String): Boolean {
        val user = userDao.getUserByEmail(email)
        if (user != null) {
            val updatedUser = user.copy(passwordHash = newPass)
            userDao.insertUser(updatedUser)
            firebaseRepository.registerUser(updatedUser) // Sync
            return true
        }
        return false
    }

    fun getAllAdmins(): Flow<List<User>> = userDao.getAllAdmins()
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers() // Todos los usuarios
    
    suspend fun getAllUsersSync(): List<User> = userDao.getAllUsersSync() // Metodo sincrono para sync
    
    suspend fun deleteUser(userId: Int) = userDao.deleteUser(userId)
    suspend fun getUserCount(): Int = userDao.getUserCount()
    suspend fun nukeUsers() = userDao.deleteAllNonRootUsers()
}
