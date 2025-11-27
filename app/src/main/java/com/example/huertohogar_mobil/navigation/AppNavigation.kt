package com.example.huertohogar_mobil.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.huertohogar_mobil.ui.screen.*
import com.example.huertohogar_mobil.viewmodel.AuthViewModel
import com.example.huertohogar_mobil.viewmodel.MarketViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    vm: MarketViewModel,
    authVm: AuthViewModel
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val authState by authVm.uiState.collectAsStateWithLifecycle()
    val isAdmin = authState.user?.role == "admin"

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.IniciarSesion.route
    ) {
        composable(Routes.Inicio.route) {
            InicioScreen(
                onNavigateToProductos = { navController.navigate(Routes.Productos.route) },
                onLogout = {
                    authVm.logout()
                    navController.navigate(Routes.IniciarSesion.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.Productos.route) {
            CatalogoScreen(
                ui = ui,
                onBuscar = vm::setQuery,
                onVer = { p -> navController.navigate(Routes.Detalle.create(p.id)) },
                onAgregar = { p -> vm.agregar(p, +1) },
                irCarrito = { navController.navigate(Routes.Carrito.route) }
            )
        }
        composable(Routes.Nosotros.route) { NosotrosScreen() }
        composable(Routes.Blog.route) { BlogScreen() }
        composable(Routes.Contacto.route) { ContactoScreen() }

        // Nueva funcionalidad Social
        composable(Routes.SocialHub.route) {
            SocialHubScreen(
                currentUserEmail = authState.user?.email,
                onChatClick = { amigoId ->
                    navController.navigate(Routes.Chat.create(amigoId, "Chat"))
                }
            )
        }

        composable(
            route = Routes.Chat.route,
            arguments = listOf(
                navArgument(Routes.Chat.ARG_ID) { type = NavType.IntType },
                navArgument(Routes.Chat.ARG_NOMBRE) { type = NavType.StringType }
            )
        ) { backStack ->
            val id = backStack.arguments?.getInt(Routes.Chat.ARG_ID) ?: 0
            val nombre = backStack.arguments?.getString(Routes.Chat.ARG_NOMBRE) ?: "Amigo"

            ChatScreen(
                amigoId = id,
                amigoNombre = nombre,
                currentUserEmail = authState.user?.email,
                onBack = { navController.popBackStack() }
            )
        }

        // Rutas de Admin
        composable(
            route = Routes.AgregarProducto.route,
            arguments = listOf(navArgument(Routes.AgregarProducto.ARG_ID) { 
                type = NavType.StringType
                nullable = true 
            })
        ) { backStack ->
             val id = backStack.arguments?.getString(Routes.AgregarProducto.ARG_ID)
             val producto = if(id != null) ui.productos.firstOrNull { it.id == id } else null
             
             AgregarProductoScreen(
                 productoEditar = producto,
                 onBack = { navController.popBackStack() }
             )
        }

        composable(Routes.AdminNotificaciones.route) {
            AdminNotificacionesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.IniciarSesion.route) { 
            IniciarSesionScreen(
                viewModel = authVm,
                onLoginExitoso = { 
                    navController.navigate(Routes.Inicio.route) {
                        popUpTo(Routes.IniciarSesion.route) { inclusive = true }
                    }
                },
                onIrARegistro = {
                    navController.navigate(Routes.Registrarse.route)
                }
            )
        }
        
        composable(Routes.Registrarse.route) { 
            RegistrarseScreen(
                viewModel = authVm,
                onRegistroExitoso = { 
                    navController.navigate(Routes.Inicio.route) {
                        popUpTo(Routes.IniciarSesion.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.FinPago.route) {
            FinPagoScreen(onVolverAlInicio = {
                navController.navigate(Routes.Inicio.route) {
                    popUpTo(Routes.Inicio.route) { inclusive = true }
                }
            })
        }

        composable(
            route = Routes.Detalle.route,
            arguments = listOf(navArgument(Routes.Detalle.ARG_ID) { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments?.getString(Routes.Detalle.ARG_ID)
            val producto = ui.productos.firstOrNull { it.id == id }
            if (producto != null) {
                DetalleScreen(
                    ui = ui,
                    producto = producto,
                    isAdmin = isAdmin, 
                    onBack = { navController.popBackStack() },
                    irCarrito = { navController.navigate(Routes.Carrito.route) },
                    onAgregar = { vm.agregar(producto, +1) },
                    onEditar = { navController.navigate(Routes.AgregarProducto.create(producto.id)) },
                    onEliminar = { 
                        vm.eliminarProducto(producto)
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Routes.Carrito.route) {
            CarritoScreen(
                ui = ui,
                onSumar = { p -> vm.agregar(p, +1) },
                onRestar = { p -> vm.agregar(p, -1) },
                onBack = { navController.popBackStack() },
                onCheckout = { navController.navigate(Routes.Checkout.route) }
            )
        }

        composable(Routes.Checkout.route) {
            CheckoutScreen(
                ui = ui,
                onBack = { navController.popBackStack() },
                onFinalizar = {
                    vm.limpiarCarrito()
                    navController.navigate(Routes.FinPago.route)
                }
            )
        }
    }
}
