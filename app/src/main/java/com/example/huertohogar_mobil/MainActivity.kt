package com.example.huertohogar_mobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.huertohogar_mobil.navigation.Routes
import com.example.huertohogar_mobil.ui.screen.*
import com.example.huertohogar_mobil.ui.theme.HuertoHogarMobilTheme
import com.example.huertohogar_mobil.viewmodel.AuthViewModel
import com.example.huertohogar_mobil.viewmodel.MarketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: MarketViewModel by viewModels()
    private val authVm: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        vm.toString() 

        setContent {
            HuertoHogarMobilTheme {
                val nav = rememberNavController()
                val ui = vm.ui.collectAsStateWithLifecycle().value
                val authState by authVm.uiState.collectAsStateWithLifecycle()

                val navBackStackEntry by nav.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                val showBottomBar = currentRoute !in listOf(
                    Routes.IniciarSesion.route, 
                    Routes.Registrarse.route
                )
                
                val isAdmin = authState.user?.role == "admin"

                Scaffold(
                    bottomBar = { 
                        if (showBottomBar) {
                            BottomNavBar(navController = nav, isAdmin = isAdmin)
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = nav,
                        startDestination = Routes.IniciarSesion.route
                    ) {
                        composable(Routes.Inicio.route) {
                            InicioScreen(
                                onNavigateToProductos = { nav.navigate(Routes.Productos.route) },
                                onLogout = {
                                    authVm.logout()
                                    nav.navigate(Routes.IniciarSesion.route) {
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
                                onVer = { p -> nav.navigate(Routes.Detalle.create(p.id)) },
                                onAgregar = { p -> vm.agregar(p, +1) },
                                irCarrito = { nav.navigate(Routes.Carrito.route) }
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
                                    // Navegar al chat, pasando el nombre temporalmente (se podría optimizar buscando el nombre)
                                    // Por simplicidad, solo pasamos el ID y el chat cargará los datos
                                    nav.navigate(Routes.Chat.create(amigoId, "Chat"))
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
                                onBack = { nav.popBackStack() }
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
                                 onBack = { nav.popBackStack() }
                             )
                        }

                        composable(Routes.AdminNotificaciones.route) {
                            AdminNotificacionesScreen(
                                onBack = { nav.popBackStack() }
                            )
                        }

                        composable(Routes.IniciarSesion.route) { 
                            IniciarSesionScreen(
                                viewModel = authVm,
                                onLoginExitoso = { 
                                    nav.navigate(Routes.Inicio.route) {
                                        popUpTo(Routes.IniciarSesion.route) { inclusive = true }
                                    }
                                },
                                onIrARegistro = {
                                    nav.navigate(Routes.Registrarse.route)
                                }
                            )
                        }
                        
                        composable(Routes.Registrarse.route) { 
                            RegistrarseScreen(
                                viewModel = authVm,
                                onRegistroExitoso = { 
                                    nav.navigate(Routes.Inicio.route) {
                                        popUpTo(Routes.IniciarSesion.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable(Routes.FinPago.route) {
                            FinPagoScreen(onVolverAlInicio = {
                                nav.navigate(Routes.Inicio.route) {
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
                                    onBack = { nav.popBackStack() },
                                    irCarrito = { nav.navigate(Routes.Carrito.route) },
                                    onAgregar = { vm.agregar(producto, +1) },
                                    onEditar = { nav.navigate(Routes.AgregarProducto.create(producto.id)) },
                                    onEliminar = { 
                                        vm.eliminarProducto(producto)
                                        nav.popBackStack()
                                    }
                                )
                            }
                        }

                        composable(Routes.Carrito.route) {
                            CarritoScreen(
                                ui = ui,
                                onSumar = { p -> vm.agregar(p, +1) },
                                onRestar = { p -> vm.agregar(p, -1) },
                                onBack = { nav.popBackStack() },
                                onCheckout = { nav.navigate(Routes.Checkout.route) }
                            )
                        }

                        composable(Routes.Checkout.route) {
                            CheckoutScreen(
                                ui = ui,
                                onBack = { nav.popBackStack() },
                                onFinalizar = {
                                    vm.limpiarCarrito()
                                    nav.navigate(Routes.FinPago.route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController, isAdmin: Boolean) {
    val items = mutableListOf(
        Routes.Inicio to Icons.Default.Home,
        Routes.Productos to Icons.Default.Store,
        Routes.SocialHub to Icons.Default.People, // Nuevo ítem Social
        Routes.Nosotros to Icons.Default.Info,
        Routes.Blog to Icons.Default.Article,
        // Routes.Contacto to Icons.Default.Email, // Sacamos contacto para hacer espacio
    )
    
    if (isAdmin) {
        items.add(Routes.AgregarProducto to Icons.Default.AddCircle)
        items.add(Routes.AdminNotificaciones to Icons.Default.Notifications)
    }

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { (screen, icon) ->
            
            val isSelected = when(screen) {
                 Routes.AgregarProducto -> currentRoute?.startsWith("agregar_producto") == true
                 else -> currentRoute == screen.route
            }
            
            val label = when(screen) {
                Routes.AgregarProducto -> "Crear"
                Routes.AdminNotificaciones -> "Buzón"
                Routes.SocialHub -> "Comunidad"
                else -> screen.route.substringBefore("/").replaceFirstChar { it.uppercase() }
            }

            NavigationBarItem(
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                selected = isSelected,
                onClick = {
                    val rutaDestino = if(screen == Routes.AgregarProducto) "agregar_producto" else screen.route
                    
                    navController.navigate(rutaDestino) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
