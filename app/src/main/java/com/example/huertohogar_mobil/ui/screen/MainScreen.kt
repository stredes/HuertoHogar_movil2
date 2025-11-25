package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.huertohogar_mobil.navigation.Routes
import com.example.huertohogar_mobil.ui.components.BottomNavBar

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Do not show bottom bar on login screen
    val showBottomBar = currentRoute != Routes.IniciarSesion.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Inicio.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Inicio.route) {
                InicioScreen(onNavigateToProductos = {
                    navController.navigate(Routes.Productos.route)
                })
            }
            composable(Routes.Productos.route) {
                CatalogoScreen(navController = navController, viewModel = hiltViewModel())
            }
            composable(Routes.Nosotros.route) { Text("Nosotros Screen") }
            composable(Routes.Blog.route) { Text("Blog Screen") }
            composable(Routes.Contacto.route) { Text("Contacto Screen") }
            composable(Routes.IniciarSesion.route) {
                LoginScreen(navController = navController)
            }
        }
    }
}