package com.example.huertohogar_mobil.network.dto

/**
 * Clase sellada para representar el resultado de operaciones de red
 * Proporciona un manejo robusto de errores y estados de carga
 */
sealed class NetworkResult<out T> {
    /**
     * Operación exitosa con datos
     */
    data class Success<out T>(val data: T) : NetworkResult<T>()

    /**
     * Error de red o del servidor
     */
    data class Error(
        val message: String,
        val code: Int? = null,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing>()

    /**
     * Estado de carga
     */
    data object Loading : NetworkResult<Nothing>()

    /**
     * Estado inicial o vacío
     */
    data object Idle : NetworkResult<Nothing>()
}

/**
 * Extensión para mapear el resultado a otro tipo
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> NetworkResult.Error(message, code, exception)
        is NetworkResult.Loading -> NetworkResult.Loading
        is NetworkResult.Idle -> NetworkResult.Idle
    }
}

/**
 * Extensión para obtener los datos o null
 */
fun <T> NetworkResult<T>.getOrNull(): T? {
    return when (this) {
        is NetworkResult.Success -> data
        else -> null
    }
}

/**
 * Extensión para ejecutar código solo si es exitoso
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

/**
 * Extensión para ejecutar código solo si es error
 */
inline fun <T> NetworkResult<T>.onError(action: (String, Int?, Throwable?) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(message, code, exception)
    }
    return this
}

