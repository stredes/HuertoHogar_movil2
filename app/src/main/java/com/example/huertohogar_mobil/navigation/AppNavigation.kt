package com.example.huertohogar_mobil.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.huertohogar_mobil.ui.screen.CatalogoScreen
import com.example.huertohogar_mobil.ui.screen.LoginScreen
// import com.example.huertohogar_mobil.ui.screen.SplashScreen (Opcional si luego la creamos)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Definimos "login" como la pantalla de inicio
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("catalogo") {
            CatalogoScreen(navController = navController)
        }
    }
}