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
import com.example.huertohogar_mobil.viewmodel.MarketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: MarketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar la base de datos (seeding) apenas se crea la actividad
        // Aunque esto se llama en el init del ViewModel, al tenerlo aquí aseguramos que la instancia se crea.
        // El lazy delegate 'by viewModels()' crea la instancia cuando se accede por primera vez.
        // Acceder a 'vm' aquí fuerza su creación y ejecución del bloque init { ... }
        vm.toString() 

        setContent {
            HuertoHogarMobilTheme {
                val nav = rememberNavController()
                val ui = vm.ui.collectAsStateWithLifecycle().value

                // Determinar si se debe mostrar la barra de navegación
                val navBackStackEntry by nav.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                val showBottomBar = currentRoute !in listOf(
                    Routes.IniciarSesion.route, 
                    Routes.Registrarse.route
                )

                Scaffold(
                    bottomBar = { 
                        if (showBottomBar) {
                            BottomNavBar(navController = nav)
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = nav,
                        // Cambiamos el inicio a IniciarSesion
                        startDestination = Routes.IniciarSesion.route
                    ) {
                        composable(Routes.Inicio.route) {
                            InicioScreen(onNavigateToProductos = { nav.navigate(Routes.Productos.route) })
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
                        
                        composable(Routes.IniciarSesion.route) { 
                            IniciarSesionScreen(
                                onLoginExitoso = { 
                                    // Navegar al Inicio y limpiar el stack para no volver al login con Back
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
                                    onBack = { nav.popBackStack() },
                                    irCarrito = { nav.navigate(Routes.Carrito.route) },
                                    onAgregar = { vm.agregar(producto, +1) }
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
fun BottomNavBar(navController: NavController) {
    // Eliminamos IniciarSesion y Registrarse de la lista de items del menu inferior
    val items = listOf(
        Routes.Inicio to Icons.Default.Home,
        Routes.Productos to Icons.Default.Store,
        Routes.Nosotros to Icons.Default.Info,
        Routes.Blog to Icons.Default.Article,
        Routes.Contacto to Icons.Default.Email,
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { (screen, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
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
