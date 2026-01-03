package com.example.huertohogar_mobil.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para Productos - Separados de las entidades Room
 * Estos objetos representan la estructura JSON del backend
 */

/**
 * Respuesta del servidor para un producto
 */
data class ProductoDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String?,

    @SerializedName("precioCLP")
    val precioCLP: Int,

    @SerializedName("precioAnterior")
    val precioAnterior: Int?,

    @SerializedName("unidad")
    val unidad: String, // kg, malla, unid, etc.

    @SerializedName("stock")
    val stock: Int = 0,

    @SerializedName("categoria")
    val categoria: String?,

    @SerializedName("imagenUrl")
    val imagenUrl: String?,

    @SerializedName("imagenRes")
    val imagenRes: Int? = null,

    @SerializedName("providerEmail")
    val providerEmail: String,

    @SerializedName("proveedorNombre")
    val proveedorNombre: String?,

    @SerializedName("activo")
    val activo: Boolean = true,

    @SerializedName("destacado")
    val destacado: Boolean = false,

    @SerializedName("calificacion")
    val calificacion: Float = 0f,

    @SerializedName("numeroCalificaciones")
    val numeroCalificaciones: Int = 0,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("fechaActualizacion")
    val fechaActualizacion: Long?
)

/**
 * Request para crear producto
 */
data class CrearProductoRequest(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String?,

    @SerializedName("precioCLP")
    val precioCLP: Int,

    @SerializedName("unidad")
    val unidad: String,

    @SerializedName("stock")
    val stock: Int = 0,

    @SerializedName("categoria")
    val categoria: String?,

    @SerializedName("imagenUrl")
    val imagenUrl: String?
)

/**
 * Request para actualizar producto
 */
data class ActualizarProductoRequest(
    @SerializedName("nombre")
    val nombre: String?,

    @SerializedName("descripcion")
    val descripcion: String?,

    @SerializedName("precioCLP")
    val precioCLP: Int?,

    @SerializedName("stock")
    val stock: Int?,

    @SerializedName("categoria")
    val categoria: String?,

    @SerializedName("activo")
    val activo: Boolean?
)

/**
 * Respuesta paginada de productos
 */
data class ProductosPaginadosResponse(
    @SerializedName("productos")
    val productos: List<ProductoDto>,

    @SerializedName("totalProductos")
    val totalProductos: Int,

    @SerializedName("paginaActual")
    val paginaActual: Int,

    @SerializedName("totalPaginas")
    val totalPaginas: Int,

    @SerializedName("tieneSiguiente")
    val tieneSiguiente: Boolean,

    @SerializedName("tieneAnterior")
    val tieneAnterior: Boolean
)

/**
 * Respuesta de eliminación
 */
data class DeleteProductoResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)

