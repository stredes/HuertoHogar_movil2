package com.example.huertohogar_mobil.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.huertohogar_mobil.ui.screen.LoginScreen
import com.example.huertohogar_mobil.ui.screen.MainScreen

/**
 * Gestiona la navegación de alto nivel de la aplicación.
 * Decide si mostrar el flujo de Login o la pantalla principal de la app.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // El NavHost principal que gestiona el flujo de autenticación.
    NavHost(navController = navController, startDestination = "main") {

        // Ruta para la pantalla de inicio de sesión
        composable(Routes.IniciarSesion.route) {
            LoginScreen(navController = navController)
        }

        // Ruta para la pantalla principal de la aplicación (post-login)
        composable("main") { // Usamos un string simple para el "contenedor" principal
            MainScreen()
        }
    }
}
