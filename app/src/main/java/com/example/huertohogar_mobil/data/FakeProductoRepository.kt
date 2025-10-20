package com.example.huertohogar_mobil.data

import com.example.huertohogar_mobil.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementación en memoria (fake) del repositorio.
 * Útil para desarrollo/local: expone un catálogo fijo como Flow.
 */
class FakeProductoRepository : ProductoRepository {

    // Catálogo de ejemplo (usa drawables colocados en res/drawable/)
    private val seed = listOf(
        Producto("1","Leche",1890,"1 L","Leche de vaca pasteurizada.", R.drawable.leche_1l),
        Producto("2","Manzana",1290,"kg","Manzana roja crocante.", R.drawable.manzana),
        Producto("3","Miel",4990,"frasco 500 g","Miel pura de abeja.", R.drawable.miel),
        Producto("4","Naranja",1190,"kg","Naranja jugosa.", R.drawable.naranja),
        Producto("5","Pimientos Tricolores",1890,"bandeja","Mix rojo, amarillo y naranja.", R.drawable.pimientos_tricolores),
        Producto("6","Plátanos Cavendish",990,"kg","Plátano maduro.", R.drawable.platanos_cavendish),
        Producto("7","Zanahoria",850,"kg","Zanahoria dulce y crocante.", R.drawable.zanahoria),
        Producto("8","Espinacas",1200,"bolsa","Hojas frescas.", R.drawable.espinacas)
    )

    // StateFlow interno que sostiene el catálogo
    private val _productos = MutableStateFlow(seed)

    /** Devuelve el catálogo como Flow (inmutable para los consumidores). */
    override fun productos(): Flow<List<Producto>> = _productos.asStateFlow()

    // --- Extensiones útiles para futuros pasos (opcionales) ---

    /** Reemplaza todo el catálogo (útil si luego sincronizas con red/DB). */
    fun setProductos(nuevos: List<Producto>) {
        _productos.value = nuevos
    }

    /** Aplica un filtro simple por nombre (retorna lista filtrada sin mutar el catálogo base). */
    fun filtrarPorNombre(query: String): List<Producto> {
        val q = query.trim().lowercase()
        return _productos.value.filter { it.nombre.lowercase().contains(q) }
    }
}
