package com.example.huertohogar_mobil.network.api

import com.example.huertohogar_mobil.network.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API para gestión de productos
 * Endpoints REST para operaciones CRUD de productos en el marketplace
 */
interface ProductsApi {

    /**
     * Obtener lista completa de productos
     */
    @GET("products")
    suspend fun list(): Response<List<ProductoDto>>

    /**
     * Obtener productos con paginación
     * @param page Número de página (inicia en 1)
     * @param size Tamaño de página (por defecto 20)
     * @param sort Campo para ordenar (ej: "precio", "nombre", "fecha")
     * @param order Orden ascendente o descendente ("asc" o "desc")
     */
    @GET("products")
    suspend fun listPaginado(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String? = null,
        @Query("order") order: String? = "asc"
    ): Response<ProductosPaginadosResponse>

    /**
     * Buscar producto por ID
     */
    @GET("products/{id}")
    suspend fun get(@Path("id") id: String): Response<ProductoDto>

    /**
     * Filtrar productos por categoría
     */
    @GET("products/categoria/{categoria}")
    suspend fun porCategoria(
        @Path("categoria") categoria: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ProductosPaginadosResponse>

    /**
     * Buscar productos por nombre o descripción
     */
    @GET("products/search")
    suspend fun buscar(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ProductosPaginadosResponse>

    /**
     * Obtener productos destacados
     */
    @GET("products/destacados")
    suspend fun destacados(
        @Query("limit") limit: Int = 10
    ): Response<List<ProductoDto>>

    /**
     * Obtener productos de un vendedor específico
     */
    @GET("products/vendedor/{email}")
    suspend fun porVendedor(
        @Path("email") email: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<ProductosPaginadosResponse>

    /**
     * Obtener mis productos (requiere autenticación)
     */
    @GET("products/mis-productos")
    suspend fun misProductos(): Response<List<ProductoDto>>

    /**
     * Crear nuevo producto
     */
    @POST("products")
    suspend fun create(@Body producto: CrearProductoRequest): Response<ProductoDto>

    /**
     * Actualizar producto existente
     */
    @PUT("products/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body producto: ActualizarProductoRequest
    ): Response<ProductoDto>

    /**
     * Actualización parcial de producto (PATCH)
     */
    @PATCH("products/{id}")
    suspend fun patch(
        @Path("id") id: String,
        @Body cambios: Map<String, Any?>
    ): Response<ProductoDto>

    /**
     * Eliminar producto
     */
    @DELETE("products/{id}")
    suspend fun delete(@Path("id") id: String): Response<DeleteProductoResponse>

    /**
     * Subir imagen de producto
     */
    @Multipart
    @POST("uploads/products")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<Map<String, String>> // Devuelve { "url": "..." }

    /**
     * Actualizar stock de producto
     */
    @PATCH("products/{id}/stock")
    suspend fun actualizarStock(
        @Path("id") id: String,
        @Body body: Map<String, Int> // { "stock": 50 }
    ): Response<ProductoDto>

    /**
     * Activar/desactivar producto
     */
    @PATCH("products/{id}/activo")
    suspend fun toggleActivo(
        @Path("id") id: String,
        @Body body: Map<String, Boolean> // { "activo": true }
    ): Response<ProductoDto>
}



