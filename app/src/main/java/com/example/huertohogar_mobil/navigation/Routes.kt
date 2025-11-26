package com.example.huertohogar_mobil.navigation

/**
 * Clase sellada para definir las rutas de navegación de la app.
 * Esto previene errores al escribir las rutas como strings y centraliza su definición.
 */
sealed class Routes(val route: String) {
    object Inicio : Routes("inicio")
    object Productos : Routes("catalogo") // Coincide con tu AppNavigation
    object Nosotros : Routes("nosotros")
    object Contacto : Routes("contacto")
    object IniciarSesion : Routes("login") // Coincide con tu AppNavigation
    object Registrarse : Routes("registrarse")
}