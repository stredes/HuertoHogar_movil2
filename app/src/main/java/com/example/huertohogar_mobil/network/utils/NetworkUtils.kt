package com.example.huertohogar_mobil.network.utils

import com.example.huertohogar_mobil.network.dto.NetworkResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    apiCall: suspend () -> T
): NetworkResult<T> {
    return withContext(dispatcher) {
        try {
            val result = apiCall()
            NetworkResult.Success(result)
        } catch (e: Throwable) {
            NetworkResult.Error(
                message = e.message ?: "Error desconocido",
                code = null,
                exception = e
            )
        }
    }
}
