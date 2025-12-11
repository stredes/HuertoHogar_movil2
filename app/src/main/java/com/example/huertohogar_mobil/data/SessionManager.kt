package com.example.huertohogar_mobil.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("huerto_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun saveUserSession(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun clearSession() {
        // En lugar de borrar todo, podemos considerar mantener algo si es necesario, 
        // pero para cerrar sesión (logout) borrar el email de sesión es correcto.
        // La limpieza de base de datos no debe ocurrir aquí.
        prefs.edit().remove(KEY_USER_EMAIL).apply()
    }
}
