package com.example.huertohogar_mobil.data

import android.util.Log
import com.example.huertohogar_mobil.model.User
import javax.inject.Inject

private const val TAG = "DB_DEBUG_USER"

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun registerUser(name: String, email: String, passwordHash: String): Boolean {
        Log.d(TAG, "Intentando registrar: $email")
        return if (userDao.getUserByEmail(email) == null) {
            val role = if (email == "admin@huertohogar.com") "admin" else "user"
            
            val newUser = User(
                name = name, 
                email = email, 
                passwordHash = passwordHash,
                role = role
            )
            userDao.insertUser(newUser)
            Log.d(TAG, "✅ Usuario creado en DB: ${newUser.email} con rol: $role")
            true
        } else {
            Log.w(TAG, "❌ Fallo registro: El email $email ya existe en la DB")
            false
        }
    }

    suspend fun loginUser(email: String, passwordHash: String): User? {
        Log.d(TAG, "Buscando credenciales en DB para: $email")
        val user = userDao.login(email, passwordHash)
        
        if (user != null) {
             Log.d(TAG, "✅ Login exitoso. Usuario encontrado: ${user.name} (Rol: ${user.role})")
             
             // Autocorrección de admin
             if (email == "admin@huertohogar.com" && user.role != "admin") {
                Log.d(TAG, "⚠️ Detectado admin sin privilegios. Corrigiendo en DB...")
                val adminUser = user.copy(role = "admin")
                userDao.insertUser(adminUser)
                return adminUser
            }
        } else {
             Log.w(TAG, "❌ Login fallido. Credenciales incorrectas o usuario no existe.")
        }
        
        return user
    }
}
