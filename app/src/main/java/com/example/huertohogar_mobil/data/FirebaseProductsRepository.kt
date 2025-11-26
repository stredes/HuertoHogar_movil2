package com.example.huertohogar_mobil.data

import android.net.Uri
import android.util.Log
import com.example.huertohogar_mobil.model.Product
import com.example.huertohogar_mobil.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirebaseProductsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProductsRepository {

    private val productsCollection = firestore.collection(Constants.COLLECTION_PRODUCTS)

    override fun getProductsStream(): Flow<List<Product>> = callbackFlow {
        val subscription = productsCollection
            .orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(Product::class.java)
                    trySend(products).isSuccess
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun addProduct(product: Product, imageUri: Uri?): Result<Boolean> {
        return try {
            val docRef = productsCollection.document()
            val newId = docRef.id
            var finalImageUrl = ""

            if (imageUri != null) {
                val storageRef = storage.reference.child("${Constants.STORAGE_PRODUCTS_IMAGES_PATH}/$newId.jpg")
                storageRef.putFile(imageUri).await()
                finalImageUrl = storageRef.downloadUrl.await().toString()
            }

            val finalProduct = product.copy(id = newId, imagenUrl = finalImageUrl)
            docRef.set(finalProduct).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error adding product", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(productId: String, newProduct: Product, newImageUri: Uri?): Result<Boolean> {
        return try {
            var finalImageUrl = newProduct.imagenUrl

            if (newImageUri != null) {
                val storageRef = storage.reference.child("${Constants.STORAGE_PRODUCTS_IMAGES_PATH}/$productId.jpg")
                storageRef.putFile(newImageUri).await()
                finalImageUrl = storageRef.downloadUrl.await().toString()
            }

            val updatedProduct = newProduct.copy(imagenUrl = finalImageUrl)
            productsCollection.document(productId).set(updatedProduct).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productId: String, imageUrl: String?): Result<Boolean> {
        return try {
            productsCollection.document(productId).delete().await()
            if (!imageUrl.isNullOrEmpty()) {
                try {
                    val storageRef = storage.reference.child("${Constants.STORAGE_PRODUCTS_IMAGES_PATH}/$productId.jpg")
                    storageRef.delete().await()
                } catch (e: Exception) {
                    Log.w("FirebaseRepo", "Could not delete image: $e")
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
