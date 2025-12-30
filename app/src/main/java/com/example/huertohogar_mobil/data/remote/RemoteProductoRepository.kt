package com.example.huertohogar_mobil.data.remote

import com.example.huertohogar_mobil.data.ProductoRepository
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.network.api.ProductsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteProductoRepository @Inject constructor(
    private val api: ProductsApi
) : ProductoRepository {
    override fun productos(): Flow<List<Producto>> = flow {
        emit(api.list())
    }

    override fun getProductosByProvider(providerEmail: String): Flow<List<Producto>> = flow {
        val all = api.list()
        emit(all.filter { it.providerEmail.equals(providerEmail, ignoreCase = true) })
    }

    override suspend fun getAllProductosSync(): List<Producto> = api.list()

    override suspend fun ensureSeeded() {
        // No-op en modo remoto
    }

    override suspend fun agregarProducto(producto: Producto) {
        api.create(producto)
    }

    override suspend fun actualizarProducto(producto: Producto) {
        val payload = mapOf(
            "nombre" to producto.nombre,
            "precioCLP" to producto.precioCLP,
            "unidad" to producto.unidad,
            "descripcion" to producto.descripcion,
            "imagenUri" to producto.imagenUri,
            "providerEmail" to producto.providerEmail
        )
        api.update(producto.id, payload)
    }

    override suspend fun eliminarProducto(producto: Producto) {
        api.delete(producto.id)
    }

    override suspend fun getProductCount(): Int = api.list().size
}

