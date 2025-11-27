package com.example.huertohogar_mobil.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.huertohogar_mobil.navigation.Routes

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
