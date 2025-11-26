package com.example.huertohogar_mobil.model

import com.google.firebase.firestore.DocumentId

// Los valores por defecto son obligatorios para que Firestore pueda deserializar el objeto
data class Product(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val precioCLP: Int = 0,
    val unidad: String = "",
    val descripcion: String = "",
    val imagenUrl: String = ""
)
