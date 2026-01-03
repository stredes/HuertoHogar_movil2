package com.example.huertohogar_mobil.network.mappers

import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.network.dto.ProductoDto
import com.example.huertohogar_mobil.network.dto.CrearProductoRequest
import com.example.huertohogar_mobil.network.dto.ActualizarProductoRequest

/**
 * Mappers para convertir entre DTOs de red y entidades de Room
 * Separa la lógica de conversión para mantener el código limpio
 */

/**
 * Convierte ProductoDto (de la API) a Producto (entidad Room)
 */
fun ProductoDto.toEntity(): Producto {
    return Producto(
        id = this.id,
        nombre = this.nombre,
        precioCLP = this.precioCLP,
        unidad = this.unidad,
        descripcion = this.descripcion ?: "",
        imagenRes = this.imagenRes ?: 0,
        imagenUri = this.imagenUrl,
        providerEmail = this.providerEmail,
        timestamp = this.timestamp
    )
}

/**
 * Convierte Producto (entidad Room) a ProductoDto
 */
fun Producto.toDto(): ProductoDto {
    return ProductoDto(
        id = this.id,
        nombre = this.nombre,
        descripcion = this.descripcion,
        precioCLP = this.precioCLP,
        precioAnterior = null,
        unidad = this.unidad,
        stock = 0,
        categoria = null,
        imagenUrl = this.imagenUri,
        imagenRes = if (this.imagenRes != 0) this.imagenRes else null,
        providerEmail = this.providerEmail ?: "",
        proveedorNombre = null,
        activo = true,
        destacado = false,
        calificacion = 0f,
        numeroCalificaciones = 0,
        timestamp = this.timestamp ?: System.currentTimeMillis(),
        fechaActualizacion = null
    )
}

/**
 * Convierte Producto a CrearProductoRequest
 */
fun Producto.toCreateRequest(): CrearProductoRequest {
    return CrearProductoRequest(
        nombre = this.nombre,
        descripcion = this.descripcion.ifEmpty { null },
        precioCLP = this.precioCLP,
        unidad = this.unidad,
        stock = 0,
        categoria = null,
        imagenUrl = this.imagenUri
    )
}

/**
 * Convierte Producto a ActualizarProductoRequest
 */
fun Producto.toUpdateRequest(): ActualizarProductoRequest {
    return ActualizarProductoRequest(
        nombre = this.nombre,
        descripcion = this.descripcion.ifEmpty { null },
        precioCLP = this.precioCLP,
        stock = null,
        categoria = null,
        activo = null
    )
}

/**
 * Convierte lista de ProductoDto a lista de Producto
 */
fun List<ProductoDto>.toEntityList(): List<Producto> {
    return this.map { it.toEntity() }
}

/**
 * Convierte lista de Producto a lista de ProductoDto
 */
fun List<Producto>.toDtoList(): List<ProductoDto> {
    return this.map { it.toDto() }
}

