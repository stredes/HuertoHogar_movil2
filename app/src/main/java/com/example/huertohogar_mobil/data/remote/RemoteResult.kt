package com.example.huertohogar_mobil.data.remote

sealed class RemoteResult<out T> {
    data class Success<T>(val data: T) : RemoteResult<T>()
    data class Error(val message: String, val code: Int? = null) : RemoteResult<Nothing>()
}

