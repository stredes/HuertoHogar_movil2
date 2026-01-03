package com.example.huertohogar_mobil.network.dto

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)
