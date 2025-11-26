package com.example.huertohogar_mobil.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.huertohogar_mobil.navigation.Routes

// Data class para simplificar la creación de los items
data class NavItem(
    val route: Routes,
    val icon: ImageVector
)

@Composable
fun BottomNavBar(navController: NavController) {
    // Define los items de la barra de navegación
    val items = listOf(
        NavItem(Routes.Inicio, Icons.Default.Home),
        NavItem(Routes.Productos, Icons.Default.Store),
        NavItem(Routes.Nosotros, Icons.Default.Info),
        NavItem(Routes.Contacto, Icons.Default.Email)
    )

    NavigationBar {
        // Obtiene la ruta actual para saber qué item resaltar
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { (screen, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                selected = currentRoute == screen.route,
                onClick = {
                    // Navega a la pantalla seleccionada
                    navController.navigate(screen.route) {
                        // Evita acumular un gran stack de destinos
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Evita múltiples copias del mismo destino
                        launchSingleTop = true
                        // Restaura el estado al volver a un destino
                        restoreState = true
                    }
                }
            )
        }
    }
}