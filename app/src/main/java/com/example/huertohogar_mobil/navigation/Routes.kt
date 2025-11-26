package com.example.huertohogar_mobil.navigation

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
    
    // Rutas de Admin
    data object AgregarProducto : Routes("agregar_producto?id={id}") {
        fun create(id: String? = null) = if (id != null) "agregar_producto?id=$id" else "agregar_producto"
        const val ARG_ID = "id"
    }
    data object AdminNotificaciones : Routes("admin_notificaciones")

    // Rutas Sociales
    data object SocialHub : Routes("social_hub")
    data object Chat : Routes("chat/{id}/{nombre}") {
        fun create(id: Int, nombre: String) = "chat/$id/$nombre"
        const val ARG_ID = "id"
        const val ARG_NOMBRE = "nombre"
    }
}
