// Rutas de navegaci�n
package com.example.huertohogar_mobil.ui.nav

/** Rutas tipadas para Navigation-Compose */
sealed class Routes(val route: String) {
    data object Catalogo : Routes("catalogo")
    data object Carrito  : Routes("carrito")
    data object Checkout : Routes("checkout")
    data object Detalle  : Routes("detalle/{id}") {
        fun create(id: String) = "detalle/$id"
        const val ARG_ID = "id"
    }
}
