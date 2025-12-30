package com.example.huertohogar_mobil.data.remote

import retrofit2.HttpException
import java.io.IOException

object RemoteErrorHandler {
    fun map(throwable: Throwable): RemoteResult.Error {
        return when (throwable) {
            is HttpException -> {
                val code = throwable.code()
                val message = throwable.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    ?: throwable.message()
                    ?: "Error HTTP"
                RemoteResult.Error(message = message, code = code)
            }
            is IOException -> RemoteResult.Error("Sin conexión", null)
            else -> RemoteResult.Error(throwable.message ?: "Error desconocido", null)
        }
    }
}
