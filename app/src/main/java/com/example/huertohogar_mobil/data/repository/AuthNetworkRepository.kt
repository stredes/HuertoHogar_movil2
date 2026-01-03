package com.example.huertohogar_mobil.data.repository

import com.example.huertohogar_mobil.data.UserDao
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.network.api.AuthApi
import com.example.huertohogar_mobil.network.dto.*
import com.example.huertohogar_mobil.network.mappers.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para Autenticación
 * Maneja login, registro y gestión de sesión
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    // Puedes inyectar DataStore o SharedPreferences para tokens
    // private val tokenDataStore: TokenDataStore
) {

    /**
     * Login con email y contraseña
     */
    suspend fun login(
        email: String,
        password: String,
        deviceInfo: DeviceInfoDto? = null
    ): NetworkResult<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequestDto(
                email = email,
                password = password,
                deviceInfo = deviceInfo
            )

            val response = authApi.login(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Guardar usuario en Room
                val user = authResponse.usuario.toEntity()
                userDao.insertUser(user)

                // Aquí guardarías el token en DataStore/SharedPreferences
                // tokenDataStore.saveTokens(authResponse.accessToken, authResponse.refreshToken)

                NetworkResult.Success(authResponse)
            } else {
                NetworkResult.Error(
                    message = "Credenciales inválidas",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error al iniciar sesión",
                exception = e
            )
        }
    }

    /**
     * Registro de nuevo usuario
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        rut: String,
        telefono: String? = null,
        direccion: String? = null,
        role: String = "user"
    ): NetworkResult<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            val request = RegistroRequestDto(
                name = name,
                email = email,
                password = password,
                rut = rut,
                telefono = telefono,
                direccion = direccion,
                role = role
            )

            val response = authApi.register(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                // Guardar usuario en Room
                val user = authResponse.usuario.toEntity()
                userDao.insertUser(user)

                // Guardar tokens
                // tokenDataStore.saveTokens(authResponse.accessToken, authResponse.refreshToken)

                NetworkResult.Success(authResponse)
            } else {
                NetworkResult.Error(
                    message = "Error al registrar usuario",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error al registrar usuario",
                exception = e
            )
        }
    }

    /**
     * Obtener perfil del usuario actual
     */
    suspend fun getProfile(): NetworkResult<ProfileResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.me()

            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!

                // Actualizar usuario en Room
                val user = profile.toEntity()
                userDao.insertUser(user)

                NetworkResult.Success(profile)
            } else {
                NetworkResult.Error(
                    message = "Error al obtener perfil",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error al obtener perfil",
                exception = e
            )
        }
    }

    /**
     * Actualizar perfil
     */
    suspend fun actualizarPerfil(
        request: ActualizarPerfilRequest
    ): NetworkResult<ProfileResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.actualizarPerfil(request)

            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!

                // Actualizar en Room
                val user = profile.toEntity()
                userDao.insertUser(user)

                NetworkResult.Success(profile)
            } else {
                NetworkResult.Error(
                    message = "Error al actualizar perfil",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error al actualizar perfil",
                exception = e
            )
        }
    }

    /**
     * Cambiar contraseña
     */
    suspend fun cambiarPassword(
        passwordActual: String,
        passwordNuevo: String
    ): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = CambiarPasswordRequest(passwordActual, passwordNuevo)
            val response = authApi.cambiarPassword(request)

            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.success)
            } else {
                NetworkResult.Error(
                    message = "Error al cambiar contraseña",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error al cambiar contraseña",
                exception = e
            )
        }
    }

    /**
     * Recuperar contraseña
     */
    suspend fun recuperarPassword(email: String): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = RecuperarPasswordRequest(email)
            val response = authApi.recuperarPassword(request)

            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.success)
            } else {
                NetworkResult.Error(
                    message = "Error al recuperar contraseña",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error al recuperar contraseña",
                exception = e
            )
        }
    }

    /**
     * Cerrar sesión
     */
    suspend fun logout(): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = authApi.logout()

            // Limpiar datos locales
            // tokenDataStore.clearTokens()
            // userDao.deleteAll() // O marcar como no autenticado

            if (response.isSuccessful) {
                NetworkResult.Success(true)
            } else {
                // Aunque falle el servidor, limpiar localmente
                NetworkResult.Success(true)
            }
        } catch (e: Exception) {
            // Aunque falle, limpiar localmente
            NetworkResult.Success(true)
        }
    }

    /**
     * Verificar si hay sesión activa (checkeo local)
     */
    suspend fun hayeSesionActiva(): Boolean = withContext(Dispatchers.IO) {
        // Implementar lógica de verificación de token
        // return tokenDataStore.getAccessToken() != null
        false // Placeholder
    }
}

