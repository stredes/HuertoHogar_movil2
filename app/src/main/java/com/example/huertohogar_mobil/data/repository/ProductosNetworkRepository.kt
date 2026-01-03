package com.example.huertohogar_mobil.data.repository

import com.example.huertohogar_mobil.data.ProductoDao
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.network.api.ProductsApi
import com.example.huertohogar_mobil.network.dto.*
import com.example.huertohogar_mobil.network.mappers.toEntity
import com.example.huertohogar_mobil.network.mappers.toEntityList
import com.example.huertohogar_mobil.network.mappers.toDto
import com.example.huertohogar_mobil.network.utils.safeApiCall
import com.example.huertohogar_mobil.network.utils.handleResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para Productos
 * Implementa el patrón Repository con soporte offline-first
 *
 * Responsabilidades:
 * - Coordinar entre fuente de datos local (Room) y remota (API)
 * - Manejar sincronización y caché
 * - Transformar DTOs a entidades
 * - Proporcionar manejo de errores consistente
 */
@Singleton
class ProductosRepository @Inject constructor(
    private val productsApi: ProductsApi,
    private val productoDao: ProductoDao
) {

    /**
     * Obtener productos desde la caché local
     */
    fun getProductosLocal(): Flow<List<Producto>> {
        return productoDao.getAllFlow()
    }

    /**
     * Obtener productos desde el servidor y actualizar caché
     * Estrategia: Cache-first con actualización en background
     */
    fun getProductos(forceRefresh: Boolean = false): Flow<NetworkResult<List<Producto>>> = flow {
        try {
            emit(NetworkResult.Loading)

            // Si no forzamos refresh, emitir datos locales primero
            if (!forceRefresh) {
                val localData = productoDao.getAll()
                if (localData.isNotEmpty()) {
                    emit(NetworkResult.Success(localData))
                }
            }

            // Obtener datos del servidor
            val response = productsApi.list()

            if (response.isSuccessful && response.body() != null) {
                val productos = response.body()!!.toEntityList()

                // Actualizar caché local
                withContext(Dispatchers.IO) {
                    productoDao.deleteAll()
                    productoDao.insertAll(productos)
                }

                emit(NetworkResult.Success(productos))
            } else {
                // Si falla, intentar con datos locales
                val localData = productoDao.getAll()
                if (localData.isNotEmpty()) {
                    emit(NetworkResult.Success(localData))
                } else {
                    emit(NetworkResult.Error(
                        message = "Error al obtener productos: ${response.code()}",
                        code = response.code()
                    ))
                }
            }
        } catch (e: Exception) {
            // En caso de error, intentar con caché local
            val localData = productoDao.getAll()
            if (localData.isNotEmpty()) {
                emit(NetworkResult.Success(localData))
            } else {
                emit(NetworkResult.Error(
                    message = e.message ?: "Error desconocido",
                    exception = e
                ))
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Obtener productos paginados
     */
    suspend fun getProductosPaginados(
        page: Int = 1,
        size: Int = 20,
        sort: String? = null,
        order: String? = "asc"
    ): NetworkResult<ProductosPaginadosResponse> = withContext(Dispatchers.IO) {
        try {
            val response = productsApi.listPaginado(page, size, sort, order)
            handleResponse(response)
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error al obtener productos paginados",
                exception = e
            )
        }
    }

    /**
     * Buscar producto por ID
     */
    suspend fun getProducto(id: String): NetworkResult<Producto> = withContext(Dispatchers.IO) {
        try {
            // Intentar primero desde caché
            val localProduct = productoDao.getById(id)

            // Luego actualizar desde servidor
            val response = productsApi.get(id)

            if (response.isSuccessful && response.body() != null) {
                val producto = response.body()!!.toEntity()
                productoDao.insert(producto)
                NetworkResult.Success(producto)
            } else {
                // Si falla el servidor pero tenemos caché
                if (localProduct != null) {
                    NetworkResult.Success(localProduct)
                } else {
                    NetworkResult.Error(
                        message = "Producto no encontrado",
                        code = response.code()
                    )
                }
            }
        } catch (e: Exception) {
            // Fallback a caché local
            val localProduct = productoDao.getById(id)
            if (localProduct != null) {
                NetworkResult.Success(localProduct)
            } else {
                NetworkResult.Error(
                    message = e.message ?: "Error al obtener producto",
                    exception = e
                )
            }
        }
    }

    /**
     * Buscar productos por nombre
     */
    suspend fun buscarProductos(
        query: String,
        page: Int = 1,
        size: Int = 20
    ): NetworkResult<ProductosPaginadosResponse> = withContext(Dispatchers.IO) {
        try {
            val response = productsApi.buscar(query, page, size)
            handleResponse(response)
        } catch (e: Exception) {
            // Fallback: buscar en caché local
            val localResults = productoDao.search("%$query%")
            NetworkResult.Success(
                ProductosPaginadosResponse(
                    productos = localResults.map { it.toDto() },
                    totalProductos = localResults.size,
                    paginaActual = 1,
                    totalPaginas = 1,
                    tieneSiguiente = false,
                    tieneAnterior = false
                )
            )
        }
    }

    /**
     * Filtrar por categoría
     */
    suspend fun porCategoria(
        categoria: String,
        page: Int = 1,
        size: Int = 20
    ): NetworkResult<ProductosPaginadosResponse> = withContext(Dispatchers.IO) {
        try {
            val response = productsApi.porCategoria(categoria, page, size)
            handleResponse(response)
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error al filtrar por categoría",
                exception = e
            )
        }
    }

    /**
     * Obtener productos destacados
     */
    suspend fun destacados(limit: Int = 10): NetworkResult<List<Producto>> = withContext(Dispatchers.IO) {
        try {
            val response = productsApi.destacados(limit)
            if (response.isSuccessful && response.body() != null) {
                val productos = response.body()!!.toEntityList()
                NetworkResult.Success(productos)
            } else {
                NetworkResult.Error(
                    message = "Error al obtener destacados",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error al obtener destacados",
                exception = e
            )
        }
    }

    /**
     * Crear producto
     */
    suspend fun crearProducto(request: CrearProductoRequest): NetworkResult<Producto> = withContext(Dispatchers.IO) {
        try {
            val response = productsApi.create(request)
            if (response.isSuccessful && response.body() != null) {
                val producto = response.body()!!.toEntity()
                productoDao.insert(producto)
                NetworkResult.Success(producto)
            } else {
                NetworkResult.Error(
                    message = "Error al crear producto",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error al crear producto",
                exception = e
            )
        }
    }


    /**
     * Actualizar producto
     */
    suspend fun actualizarProducto(
        id: String,
        request: ActualizarProductoRequest
    ): NetworkResult<Producto> = withContext(Dispatchers.IO) {
        when (val result = safeApiCall { productsApi.update(id, request) }) {
            is NetworkResult.Success -> {
                val producto = result.data.toEntity()
                productoDao.update(producto)
                NetworkResult.Success(producto)
            }
            is NetworkResult.Error -> result
            else -> NetworkResult.Error("Estado inesperado")
        }
    }

    /**
     * Eliminar producto
     */
    suspend fun eliminarProducto(id: String): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
        when (val result = safeApiCall { productsApi.delete(id) }) {
            is NetworkResult.Success -> {
                productoDao.deleteById(id)
                NetworkResult.Success(result.data.success)
            }
            is NetworkResult.Error -> result
            else -> NetworkResult.Error("Estado inesperado")
        }
    }

    // ========== SINCRONIZACIÓN ==========

    /**
     * Sincronización manual completa
     *
     * Limpia toda la caché y descarga datos frescos del servidor
     */
    suspend fun sincronizar(): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        when (val result = safeApiCall { productsApi.list() }) {
            is NetworkResult.Success -> {
                val productos = result.data.toEntityList()
                productoDao.deleteAll()
                productoDao.insertAll(productos)
                NetworkResult.Success(Unit)
            }
            is NetworkResult.Error -> result
            else -> NetworkResult.Error("Error en sincronización")
        }
    }
}

