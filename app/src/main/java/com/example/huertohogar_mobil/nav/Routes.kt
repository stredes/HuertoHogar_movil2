package com.example.huertohogar_mobil.nav

sealed class Routes(val route: String) {
    data object Catalogo : Routes("catalogo")
    data object Carrito  : Routes("carrito")
    data object Checkout : Routes("checkout")
    data object Detalle  : Routes("detalle/{id}") {
        fun create(id: String) = "detalle/$id"
        const val ARG_ID = "id"
    }
    data object Inicio : Routes("inicio")
    data object Productos : Routes("productos")
    data object Nosotros : Routes("nosotros")
    data object Blog : Routes("blog")
    data object Contacto : Routes("contacto")
    data object IniciarSesion : Routes("iniciar_sesion")
    data object Registrarse : Routes("registrarse")
    data object FinPago : Routes("fin_pago")
}
