package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.huertohogar_mobil.navigation.Routes
import com.example.huertohogar_mobil.ui.components.BottomNavBar

/**
 * Pantalla principal que actúa como contenedor de la app post-login.
 * Utiliza un Scaffold para mostrar la barra de navegación inferior (BottomNavBar)
 * y un NavHost para el contenido principal de la app.
 */
@Composable
fun MainScreen() {
    // Este NavController gestiona la navegación DENTRO del Scaffold (entre Catálogo, Nosotros, etc.)
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Productos.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // La ruta "inicio" por ahora muestra el catálogo
            composable(Routes.Inicio.route) {
                CatalogoScreen(navController = navController, viewModel = hiltViewModel())
            }
            composable(Routes.Productos.route) {
                CatalogoScreen(navController = navController, viewModel = hiltViewModel())
            }
            // Placeholders para las otras pantallas de la barra de navegación
            composable(Routes.Nosotros.route) { Text("Nosotros Screen") }
            composable(Routes.Blog.route) { Text("Blog Screen") }
            composable(Routes.Contacto.route) { Text("Contacto Screen") }
            composable(Routes.IniciarSesion.route) {
                // Este ítem en la barra podría usarse para cerrar sesión en el futuro.
                // Por ahora, es un placeholder.
                Text("Pantalla de Perfil/Cerrar Sesión")
            }
        }
    }
}