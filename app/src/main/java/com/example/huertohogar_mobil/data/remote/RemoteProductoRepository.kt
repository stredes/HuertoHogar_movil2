package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.data.ProductoRepository
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.network.api.ProductsApi
import com.example.huertohogar_mobil.network.dto.CrearProductoRequest
import com.example.huertohogar_mobil.network.dto.ActualizarProductoRequest
import com.example.huertohogar_mobil.network.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteProductoRepository @Inject constructor(
    private val api: ProductsApi
) : ProductoRepository {
    override fun productos(): Flow<List<Producto>> = flow {
        val resp = api.list()
        if (resp.isSuccessful && resp.body() != null) {
            emit(resp.body()!!.map { it.toEntity() })
        } else {
            emit(emptyList())
        }
    }

    override fun getProductosByProvider(providerEmail: String): Flow<List<Producto>> = flow {
        val resp = api.list()
        if (resp.isSuccessful && resp.body() != null) {
            val all = resp.body()!!.map { it.toEntity() }
            emit(all.filter { it.providerEmail.equals(providerEmail, ignoreCase = true) })
        } else {
            emit(emptyList())
        }
    }

    override suspend fun getAllProductosSync(): List<Producto> {
        val resp = api.list()
        return if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.map { it.toEntity() }
        } else emptyList()
    }

    override suspend fun ensureSeeded() {
        // No-op en modo remoto
    }

    override suspend fun agregarProducto(producto: Producto) {
        val request = CrearProductoRequest(
            nombre = producto.nombre,
            precioCLP = producto.precioCLP,
            unidad = producto.unidad,
            descripcion = producto.descripcion,
            imagenUri = producto.imagenUri,
            providerEmail = producto.providerEmail,
            categoria = producto.categoria,
            destacado = producto.destacado,
            stock = producto.stock
        )
        api.create(request)
    }

    override suspend fun actualizarProducto(producto: Producto) {
        val request = ActualizarProductoRequest(
            nombre = producto.nombre,
            precioCLP = producto.precioCLP,
            unidad = producto.unidad,
            descripcion = producto.descripcion,
            imagenUri = producto.imagenUri,
            categoria = producto.categoria,
            destacado = producto.destacado,
            stock = producto.stock
        )
        api.update(producto.id, request)
    }

    override suspend fun eliminarProducto(producto: Producto) {
        api.delete(producto.id)
    }

    override suspend fun getProductCount(): Int {
        val resp = api.list()
        return if (resp.isSuccessful && resp.body() != null) {
            resp.body()!!.size
        } else 0
    }
}

