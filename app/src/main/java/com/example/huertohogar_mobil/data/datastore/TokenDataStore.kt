package com.example.huertohogar_mobil.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "huerto_tokens")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_ACCESS = stringPreferencesKey("access_token")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token")

    val accessTokenFlow: Flow<String?> = context.tokenDataStore.data.map { it[KEY_ACCESS] }
    val refreshTokenFlow: Flow<String?> = context.tokenDataStore.data.map { it[KEY_REFRESH] }

    suspend fun saveTokens(accessToken: String?, refreshToken: String?) {
        context.tokenDataStore.edit { prefs ->
            if (accessToken != null) prefs[KEY_ACCESS] = accessToken else prefs.remove(KEY_ACCESS)
            if (refreshToken != null) prefs[KEY_REFRESH] = refreshToken else prefs.remove(KEY_REFRESH)
        }
    }

    suspend fun clear() {
        context.tokenDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun getAccessToken(): String? = accessTokenFlow.firstOrNull()
    suspend fun getRefreshToken(): String? = refreshTokenFlow.firstOrNull()
}
