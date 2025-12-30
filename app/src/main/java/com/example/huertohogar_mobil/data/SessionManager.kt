package com.example.huertohogar_mobil.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
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
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_PROVIDER = "is_provider"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
    }

    fun saveUserSession(email: String, name: String = "", isProvider: Boolean = false) {
        prefs.edit {
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putBoolean(KEY_IS_PROVIDER, isProvider)
        }
    }

    fun saveTokens(access: String, refresh: String) {
        prefs.edit {
            putString(KEY_ACCESS, access)
            putString(KEY_REFRESH, refresh)
        }
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun isUserProvider(): Boolean {
        return prefs.getBoolean(KEY_IS_PROVIDER, false)
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    fun clearSession() {
        // En lugar de borrar todo, podemos considerar mantener algo si es necesario, 
        // pero para cerrar sesión (logout) borrar el email de sesión es correcto.
        // La limpieza de base de datos no debe ocurrir aquí.
        prefs.edit {
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_IS_PROVIDER)
            remove(KEY_ACCESS)
            remove(KEY_REFRESH)
        }
    }
}
