package com.example.huertohogar_mobil.network.api

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadsApi {
    @Multipart
    @POST("uploads/products")
    suspend fun uploadProductImage(@Part image: MultipartBody.Part): Map<String, String>
}

