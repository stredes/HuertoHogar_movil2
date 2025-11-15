package com.example.huertohogar_mobil.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.huertohogar_mobil.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Inicio.route) {
        composable(Routes.Inicio.route) { InicioScreen(navController) }
        composable(Routes.Catalogo.route) { CatalogoScreen(navController) }
        composable(Routes.Carrito.route) { CarritoScreen(navController) }
        composable(Routes.Checkout.route) { CheckoutScreen(navController) }
        composable(
            route = Routes.Detalle.route,
            arguments = listOf(navArgument(Routes.Detalle.ARG_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Routes.Detalle.ARG_ID)
            requireNotNull(id)
            DetalleScreen(navController, id)
        }
        composable(Routes.Nosotros.route) { NosotrosScreen(navController) }
        composable(Routes.Blog.route) { BlogScreen(navController) }
        composable(Routes.Contacto.route) { ContactoScreen(navController) }
        composable(Routes.IniciarSesion.route) { IniciarSesionScreen(navController) }
        composable(Routes.Registrarse.route) { RegistrarseScreen(navController) }
        composable(Routes.FinPago.route) { FinPagoScreen(navController) }
        composable(Routes.Productos.route) { ProductosScreen(navController) }
    }
}
