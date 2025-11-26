package com.example.huertohogar_mobil.data

import android.net.Uri
import com.example.huertohogar_mobil.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {
    fun getProductsStream(): Flow<List<Product>>
    suspend fun addProduct(product: Product, imageUri: Uri?): Result<Boolean>
    suspend fun updateProduct(productId: String, newProduct: Product, newImageUri: Uri?): Result<Boolean>
    suspend fun deleteProduct(productId: String, imageUrl: String?): Result<Boolean>
}
