package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.huertohogar_mobil.navigation.Routes
import com.example.huertohogar_mobil.ui.components.BottomNavBar

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
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
            composable(Routes.Nosotros.route) { NosotrosScreen() }
            composable(Routes.Contacto.route) { ContactoScreen() }
        }
    }
}
