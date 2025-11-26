package com.example.huertohogar_mobil.data

import com.example.huertohogar_mobil.model.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun registerUser(name: String, email: String, passwordHash: String): Boolean {
        return if (userDao.getUserByEmail(email) == null) {
            userDao.insertUser(User(name = name, email = email, passwordHash = passwordHash))
            true
        } else {
            false
        }
    }

    suspend fun loginUser(email: String, passwordHash: String): User? {
        return userDao.login(email, passwordHash)
    }
}
