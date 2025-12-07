package com.example.huertohogar_mobil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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

                LaunchedEffect(authState.user, hasAllRequiredPermissions()) {
                    if (authState.user != null && authState.user!!.role != "root" && hasAllRequiredPermissions()) {
                        socialVm.onPermissionsGranted(authState.user!!.email)
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
                        composable(Routes.Inicio.route) { 
                            InicioScreen(
                                onNavigateToProductos = { navController.navigate(Routes.Productos.route) }, 
                                onLogout = { 
                                    authVm.logout()
                                    navController.navigate(Routes.IniciarSesion.route) { popUpTo(0) }
                                }
                            )
                        }
                        composable(Routes.Productos.route) { 
                            CatalogoScreen(
                                ui = ui, 
                                onBuscar = vm::setQuery, 
                                onVer = { p -> navController.navigate(Routes.Detalle.create(p.id)) }, 
                                onAgregar = { p -> vm.agregar(p, 1) }, 
                                irCarrito = { navController.navigate(Routes.Carrito.route) }
                            )
                        }
                        composable(Routes.Nosotros.route) { NosotrosScreen() }
                        composable(Routes.Blog.route) { BlogScreen() }
                        composable(Routes.Contacto.route) { ContactoScreen() }
                        composable(Routes.SocialHub.route) { 
                            SocialHubScreen(
                                currentUserEmail = authState.user?.email, 
                                onChatClick = { amigoId -> navController.navigate(Routes.Chat.create(amigoId, "Chat")) }
                            )
                        }
                        composable(Routes.Chat.route, arguments = listOf(navArgument(Routes.Chat.ARG_ID) { type = NavType.IntType }, navArgument(Routes.Chat.ARG_NOMBRE) { type = NavType.StringType })) { backStack ->
                            val id = backStack.arguments?.getInt(Routes.Chat.ARG_ID) ?: 0
                            val nombre = backStack.arguments?.getString(Routes.Chat.ARG_NOMBRE) ?: "Amigo"
                            ChatScreen(
                                amigoId = id, 
                                amigoNombre = nombre, 
                                currentUserEmail = authState.user?.email, 
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.AgregarProducto.route, arguments = listOf(navArgument(Routes.AgregarProducto.ARG_ID) { type = NavType.StringType; nullable = true })) { backStack ->
                            val id = backStack.arguments?.getString(Routes.AgregarProducto.ARG_ID)
                            val producto = if (id != null) ui.productos.firstOrNull { it.id == id } else null
                            AgregarProductoScreen(
                                viewModel = vm,
                                productoEditar = producto, 
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.AdminNotificaciones.route) { AdminNotificacionesScreen(onBack = { navController.popBackStack() }) }
                        composable(Routes.IniciarSesion.route) { 
                            IniciarSesionScreen(
                                viewModel = authVm, 
                                onLoginExitoso = { 
                                    val user = authVm.uiState.value.user
                                    if (user?.role == "root") {
                                        navController.navigate(Routes.RootDashboard.route) { popUpTo(Routes.IniciarSesion.route) { inclusive = true } }
                                    } else {
                                        navController.navigate(Routes.Inicio.route) { popUpTo(Routes.IniciarSesion.route) { inclusive = true } }
                                    }
                                }, 
                                onIrARegistro = { navController.navigate(Routes.Registrarse.route) }
                            )
                        }
                        composable(Routes.Registrarse.route) { 
                            RegistrarseScreen(
                                viewModel = authVm, 
                                onRegistroExitoso = { navController.navigate(Routes.Inicio.route) { popUpTo(Routes.IniciarSesion.route) { inclusive = true } } }
                            )
                        }
                        composable(Routes.FinPago.route) { FinPagoScreen { navController.navigate(Routes.Inicio.route) { popUpTo(Routes.Inicio.route) { inclusive = true } } } }
                        composable(Routes.Detalle.route, arguments = listOf(navArgument(Routes.Detalle.ARG_ID) { type = NavType.StringType })) { backStack ->
                            val id = backStack.arguments?.getString(Routes.Detalle.ARG_ID)
                            val producto = ui.productos.firstOrNull { it.id == id }
                            if (producto != null) {
                                DetalleScreen(
                                    ui = ui, 
                                    producto = producto, 
                                    isAdmin = isAdmin, 
                                    onBack = { navController.popBackStack() }, 
                                    irCarrito = { navController.navigate(Routes.Carrito.route) }, 
                                    onAgregar = { vm.agregar(producto, 1) }, 
                                    onEditar = { navController.navigate(Routes.AgregarProducto.create(producto.id)) }, 
                                    onEliminar = { vm.eliminarProducto(producto); navController.popBackStack() }
                                )
                            }
                        }
                        composable(Routes.Carrito.route) { 
                            CarritoScreen(
                                ui = ui, 
                                onSumar = { p -> vm.agregar(p, 1) }, 
                                onRestar = { p -> vm.agregar(p, -1) }, 
                                onBack = { navController.popBackStack() }, 
                                onCheckout = { navController.navigate(Routes.Checkout.route) }, 
                                onCompartir = { navController.navigate(Routes.CompartirCarrito.route) }
                            )
                        }
                        composable(Routes.CompartirCarrito.route) { 
                            CompartirCarritoScreen(
                                marketUiState = ui, 
                                socialViewModel = socialVm, 
                                onBack = { navController.popBackStack() }, 
                                onCompartidoExitoso = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.Checkout.route) { 
                            CheckoutScreen(
                                ui = ui, 
                                onBack = { navController.popBackStack() }, 
                                onFinalizar = { vm.limpiarCarrito(); navController.navigate(Routes.FinPago.route) }
                            )
                        }
                        composable(Routes.RootDashboard.route) { 
                            RootDashboardScreen(
                                viewModel = hiltViewModel(),
                                onNavigateCreate = { navController.navigate(Routes.EditUser.create(null)) }, 
                                onNavigateUsers = { navController.navigate(Routes.RootUsers.route) }, 
                                onNavigateProducts = { navController.navigate(Routes.RootProducts.route) }, 
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
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.RootProducts.route) { RootProductsScreen(onBack = { navController.popBackStack() }) }
                        composable(Routes.EditUser.route, arguments = listOf(navArgument(Routes.EditUser.ARG_ID) { type = NavType.IntType; defaultValue = 0 })) { backStack ->
                            val id = backStack.arguments?.getInt(Routes.EditUser.ARG_ID)
                            val finalId = if (id != 0) id else null
                            EditUserScreen(
                                userId = finalId, 
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
        
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == "ACTION_OPEN_CHAT") {
            val userId = intent.getIntExtra("chat_user_id", -1)
            val userName = intent.getStringExtra("chat_user_name")
            if (userId != -1 && userName != null && ::navController.isInitialized) {
                navController.navigate(Routes.Chat.create(userId, userName))
            }
        }
    }

    private fun hasAllRequiredPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= 33) {
            listOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun requestRuntimePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        requestPermissionLauncher.launch(permissions)
    }
    
    private fun onAllPermissionsGranted() {
        val user = authVm.uiState.value.user
        if (user != null && user.role != "root") {
            socialVm.onPermissionsGranted(user.email)
        }
    }

    private fun onPermissionsDenied() {
        Log.w("MainActivity", "Permisos denegados.")
    }
}

@Composable
fun BottomNavBar(navController: NavController, isAdmin: Boolean) {
    val items = mutableListOf(
        Routes.Inicio to Icons.Default.Home,
        Routes.Productos to Icons.Default.Store,
        Routes.SocialHub to Icons.Default.People, 
        Routes.Nosotros to Icons.Default.Info,
        Routes.Blog to Icons.Default.Article,
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
            val isSelected = currentRoute?.startsWith(screen.route.substringBefore("/")) == true
            
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
