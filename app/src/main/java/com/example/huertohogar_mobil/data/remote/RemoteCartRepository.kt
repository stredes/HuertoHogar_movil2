package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.model.CarritoItem
import com.example.huertohogar_mobil.network.api.CartApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteCartRepository @Inject constructor(
    private val api: CartApi
) {
    suspend fun getCart(): List<CarritoItem> = api.getCart()
    suspend fun addItem(productId: String, qty: Int): List<CarritoItem> = api.addItem(mapOf("productId" to productId, "qty" to qty))
    suspend fun removeItem(productId: String): List<CarritoItem> = api.removeItem(productId)
    suspend fun clear(): Map<String, String> = api.clear()
}

