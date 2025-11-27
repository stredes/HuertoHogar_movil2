package com.example.huertohogar_mobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.huertohogar_mobil.navigation.AppNavHost
import com.example.huertohogar_mobil.navigation.Routes
import com.example.huertohogar_mobil.ui.components.BottomNavBar
import com.example.huertohogar_mobil.ui.theme.HuertoHogarMobilTheme
import com.example.huertohogar_mobil.viewmodel.AuthViewModel
import com.example.huertohogar_mobil.viewmodel.MarketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: MarketViewModel by viewModels()
    private val authVm: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        vm.toString() // Keeping user's code

        setContent {
            HuertoHogarMobilTheme {
                val nav = rememberNavController()
                val authState by authVm.uiState.collectAsStateWithLifecycle()

                val navBackStackEntry by nav.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                val showBottomBar = currentRoute !in listOf(
                    Routes.IniciarSesion.route, 
                    Routes.Registrarse.route
                )
                
                val isAdmin = authState.user?.role == "admin"

                Scaffold(
                    bottomBar = { 
                        if (showBottomBar) {
                            BottomNavBar(navController = nav, isAdmin = isAdmin)
                        }
                    }
                ) { paddingValues ->
                    AppNavHost(
                        navController = nav,
                        modifier = Modifier.padding(paddingValues),
                        vm = vm,
                        authVm = authVm
                    )
                }
            }
        }
    }
}
