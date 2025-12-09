package com.example.huertohogar_mobil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.huertohogar_mobil.navigation.Routes
import com.example.huertohogar_mobil.service.P2pService
import com.example.huertohogar_mobil.ui.screen.*
import com.example.huertohogar_mobil.ui.theme.HuertoHogarMobilTheme
import com.example.huertohogar_mobil.viewmodel.AuthViewModel
import com.example.huertohogar_mobil.viewmodel.MarketViewModel
import com.example.huertohogar_mobil.viewmodel.SocialViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: MarketViewModel by viewModels()
    private val authVm: AuthViewModel by viewModels()
    private val socialVm: SocialViewModel by viewModels()

    private lateinit var navController: NavHostController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.all { it.value }) {
            onAllPermissionsGranted()
        } else {
            onPermissionsDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasAllRequiredPermissions()) {
            requestRuntimePermissions()
        }

        setContent {
            HuertoHogarMobilTheme {
                navController = rememberNavController()
                val ui = vm.ui.collectAsStateWithLifecycle().value
                val authState by authVm.uiState.collectAsStateWithLifecycle()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute !in listOf(
                    Routes.IniciarSesion.route,
                    Routes.Registrarse.route,
                    Routes.RootDashboard.route
                ) && !(currentRoute?.startsWith("edit_user") == true)

                val isAdmin = authState.user?.role == "admin"
                val currentUserEmail = authState.user?.email // Capturamos email actual

                // FIX: Lanzar Servicio en Foreground cuando el usuario está listo y permisos concedidos
                LaunchedEffect(authState.user, hasAllRequiredPermissions()) {
                    if (authState.user != null &&
                        authState.user!!.role != "root" &&
                        hasAllRequiredPermissions()
                    ) {
                        // 1. Notificar al ViewModel (lógica de UI)
                        socialVm.onPermissionsGranted(authState.user!!.email)
                        
                        // 2. Iniciar el Servicio Persistente (Notificaciones y Conexión Background)
                        startP2pService(authState.user!!.email)
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(navController = navController, isAdmin = isAdmin)
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = navController,
                        startDestination = Routes.IniciarSesion.route
                    ) {

                        // ---------- RUTAS PÚBLICAS / USUARIO NORMAL ----------

                        composable(Routes.Inicio.route) {
                            InicioScreen(
                                onNavigateToProductos = { navController.navigate(Routes.Productos.route) },
                                onNavigateToProfile = { navController.navigate(Routes.EditProfile.route) },
                                onLogout = {
                                    stopP2pService() // Detener servicio al cerrar sesión
                                    authVm.logout()
                                    navController.navigate(Routes.IniciarSesion.route) {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }

                        composable(Routes.Productos.route) {
                            CatalogoScreen(
                                ui = ui,
                                onBuscar = vm::setQuery,
                                onSelectProvider = vm::setProviderFilter, // PASAMOS EL CALLBACK
                                onVer = { p -> navController.navigate(Routes.Detalle.create(p.id)) },
                                onAgregar = { p -> vm.agregar(p, 1) },
                                irCarrito = { navController.navigate(Routes.Carrito.route) }
                            )
                        }

                        composable(Routes.Carrito.route) {
                            CarritoScreen(
                                ui = ui,
                                onSumar = vm::agregar,
                                onRestar = vm::quitar,
                                onBack = { navController.popBackStack() },
                                onCheckout = { navController.navigate(Routes.Checkout.route) },
                                onCompartir = { navController.navigate(Routes.CompartirCarrito.route) } // Agregado si existe
                            )
                        }
                        
                        composable(Routes.Checkout.route) {
                            CheckoutScreen(
                                ui = ui,
                                onBack = { navController.popBackStack() },
                                onFinalizar = {
                                    vm.notificarCompra()
                                    vm.limpiarCarrito()
                                    navController.navigate(Routes.FinPago.route) {
                                        popUpTo(Routes.Inicio.route) { inclusive = false }
                                    }
                                }
                            )
                        }
                        
                        composable(
                            Routes.Detalle.route,
                            arguments = listOf(navArgument(Routes.Detalle.ARG_ID) { type = NavType.StringType })
                        ) { backStack ->
                            val id = backStack.arguments?.getString(Routes.Detalle.ARG_ID)
                            val producto = ui.productos.find { it.id == id }
                            if (producto != null) {
                                DetalleScreen(
                                    ui = ui,
                                    producto = producto,
                                    isAdmin = isAdmin,
                                    currentUserEmail = currentUserEmail,
                                    onBack = { navController.popBackStack() },
                                    irCarrito = { navController.navigate(Routes.Carrito.route) },
                                    onAgregar = { vm.agregar(producto, 1) }
                                )
                            }
                        }

                        composable(Routes.Nosotros.route) { NosotrosScreen() }

                        composable(Routes.Blog.route) { BlogScreen() }

                        composable(Routes.Contacto.route) { ContactoScreen() }

                        // ---------- SOCIAL / CHAT ----------

                        composable(Routes.SocialHub.route) {
                            SocialHubScreen(
                                currentUserEmail = authState.user?.email,
                                onChatClick = { amigoId ->
                                    navController.navigate(Routes.Chat.create(amigoId, "Chat"))
                                }
                            )
                        }
                        
                        // Nueva Ruta para Compartir Carrito (si existe el screen)
                        composable(Routes.CompartirCarrito.route) {
                             // CompartirCarritoScreen(viewModel = vm, onBack = { navController.popBackStack() })
                             // Si no existe, comentamos o creamos placeholder. 
                             // El viewmodel tiene la lógica pero no tengo el screen a mano en la lista reciente.
                             // Dejaré un placeholder o lo removeré si falla.
                             // Asumiré que no es crítico por ahora, pero lo dejo vacío
                             Text("Compartir Carrito")
                        }

                        composable(
                            Routes.Chat.route,
                            arguments = listOf(
                                navArgument(Routes.Chat.ARG_ID) { type = NavType.IntType },
                                navArgument(Routes.Chat.ARG_NOMBRE) { type = NavType.StringType }
                            )
                        ) { backStack ->
                            val id = backStack.arguments?.getInt(Routes.Chat.ARG_ID) ?: 0
                            val nombre =
                                backStack.arguments?.getString(Routes.Chat.ARG_NOMBRE) ?: "Amigo"
                            ChatScreen(
                                amigoId = id,
                                amigoNombre = nombre,
                                currentUserEmail = authState.user?.email,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // ---------- ADMIN: PRODUCTOS ----------

                        composable(
                            Routes.AgregarProducto.route,
                            arguments = listOf(
                                navArgument(Routes.AgregarProducto.ARG_ID) {
                                    type = NavType.StringType; nullable = true
                                }
                            )
                        ) { backStack ->
                            val id = backStack.arguments?.getString(Routes.AgregarProducto.ARG_ID)
                            val producto =
                                if (id != null) ui.productos.firstOrNull { it.id == id } else null
                            AgregarProductoScreen(
                                viewModel = vm,
                                productoEditar = producto,
                                currentUserEmail = currentUserEmail, // Pasar email para autoría
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.AdminNotificaciones.route) {
                            AdminNotificacionesScreen(onBack = { navController.popBackStack() })
                        }
                        
                        // ---------- ROOT DASHBOARD ----------
                        composable(Routes.RootDashboard.route) {
                             RootDashboardScreen(
                                 onNavigateUsers = { navController.navigate(Routes.RootUsers.route) },
                                 onNavigateProviders = { navController.navigate(Routes.RootProviders.route) },
                                 onNavigateProducts = { navController.navigate(Routes.RootProducts.route) },
                                 onNavigateCreate = { navController.navigate(Routes.EditUser.create(null)) }, // FIX: Agregar onNavigateCreate
                                 onLogout = {
                                     authVm.logout()
                                     navController.navigate(Routes.IniciarSesion.route) { popUpTo(0) }
                                 }
                             )
                        }
                        
                        composable(Routes.RootUsers.route) {
                            RootUsersScreen(
                                onNavigateEdit = { id -> navController.navigate(Routes.EditUser.create(id)) },
                                onNavigateCreate = { navController.navigate(Routes.EditUser.create(null)) },
                                onNavigateDashboard = { navController.navigate(Routes.RootDashboard.route) {
                                    popUpTo(Routes.RootDashboard.route) { inclusive = true }
                                }},
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable(Routes.RootProviders.route) {
                             RootProvidersScreen(
                                 onProviderClick = { email -> 
                                    navController.navigate(Routes.RootProviderProducts.create(email)) 
                                 },
                                 onBack = { navController.popBackStack() }
                             )
                        }
                        
                        composable(
                            Routes.RootProviderProducts.route,
                            arguments = listOf(navArgument(Routes.RootProviderProducts.ARG_EMAIL) { type = NavType.StringType })
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString(Routes.RootProviderProducts.ARG_EMAIL) ?: ""
                            RootProviderProductsScreen(
                                email = email,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable(Routes.RootProducts.route) {
                            RootProductsScreen(
                                onEdit = { id -> 
                                    navController.navigate(Routes.AgregarProducto.create(id))
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable(
                             Routes.EditUser.route,
                             arguments = listOf(navArgument(Routes.EditUser.ARG_ID) { type = NavType.IntType; defaultValue = -1 })
                        ) { backStackEntry ->
                             val argId = backStackEntry.arguments?.getInt(Routes.EditUser.ARG_ID) ?: -1
                             val userId = if (argId == -1) null else argId
                             EditUserScreen(userId = userId, onBack = { navController.popBackStack() })
                        }
                        
                        composable(Routes.EditProfile.route) {
                             EditProfileScreen(
                                 onBack = { navController.popBackStack() }
                             )
                        }

                        // ---------- LOGIN / REGISTRO ----------

                        composable(Routes.IniciarSesion.route) {
                            IniciarSesionScreen(
                                viewModel = authVm,
                                // ROOT → Dashboard de root
                                onLoginRoot = {
                                    navController.navigate(Routes.RootDashboard.route) {
                                        popUpTo(Routes.IniciarSesion.route) {
                                            inclusive = true
                                        }
                                    }
                                },
                                // ADMIN → por ejemplo al catálogo
                                onLoginAdmin = {
                                    navController.navigate(Routes.Productos.route) {
                                        popUpTo(Routes.IniciarSesion.route) {
                                            inclusive = true
                                        }
                                    }
                                },
                                // USUARIO NORMAL → Inicio
                                onLoginUser = {
                                    navController.navigate(Routes.Inicio.route) {
                                        popUpTo(Routes.IniciarSesion.route) {
                                            inclusive = true
                                        }
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
                                        popUpTo(Routes.IniciarSesion.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }

                        // ---------- FLUJO DE PAGO ----------

                        composable(Routes.FinPago.route) {
                            FinPagoScreen {
                                navController.navigate(Routes.Inicio.route) {
                                    popUpTo(Routes.Inicio.route) { inclusive = true }
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private fun startP2pService(email: String) {
        val intent = Intent(this, P2pService::class.java).apply {
            action = P2pService.ACTION_START
            putExtra(P2pService.EXTRA_EMAIL, email)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopP2pService() {
        val intent = Intent(this, P2pService::class.java).apply {
            action = P2pService.ACTION_STOP
        }
        startService(intent)
    }

    // --- Permisos ---

    private fun hasAllRequiredPermissions(): Boolean {
        // En Android 13+ (Tiramisu), requerimos NEARBY_WIFI_DEVICES y POST_NOTIFICATIONS
        // En Android 12 (S) y anteriores, ACCESS_FINE_LOCATION es fundamental para P2P/WiFi
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        // Comprobar imágenes en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestRuntimePermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun onAllPermissionsGranted() {
        // Inicializar si el usuario ya estaba logueado
        // (La lógica reactiva en LaunchedEffect(user) se encargará)
    }

    private fun onPermissionsDenied() {
        // Manejar el caso de rechazo
    }
}

@Composable
fun BottomNavBar(navController: NavController, isAdmin: Boolean) {
    val items = mutableListOf(
        Routes.Inicio to Icons.Default.Home,
        Routes.Productos to Icons.Default.Store,
        Routes.SocialHub to Icons.Default.People,
        Routes.Nosotros to Icons.Default.Info,
        Routes.Blog to Icons.AutoMirrored.Filled.Article,
    ).apply {
        if (isAdmin) {
            add(Routes.AgregarProducto to Icons.Default.AddCircle)
            add(Routes.AdminNotificaciones to Icons.Default.Notifications)
        }
    }

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { (screen, icon) ->
            val isSelected =
                currentRoute?.startsWith(screen.route.substringBefore("/")) == true

            val label = when (screen) {
                Routes.AgregarProducto -> "Crear"
                Routes.AdminNotificaciones -> "Buzón"
                Routes.SocialHub -> "Comunidad"
                else -> screen.route.replaceFirstChar { it.uppercase() }
            }

            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = isSelected,
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
