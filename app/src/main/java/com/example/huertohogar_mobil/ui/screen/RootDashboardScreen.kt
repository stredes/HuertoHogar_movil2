package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.RootViewModel

@Composable
fun RootDashboardScreen(
    viewModel: RootViewModel = hiltViewModel(),
    onNavigateCreate: () -> Unit,
    onNavigateUsers: () -> Unit,
    onNavigateProducts: () -> Unit,
    onLogout: () -> Unit,
    onNavigateProviders: (() -> Unit)? = null
) {
    val userCount by viewModel.userCount.collectAsStateWithLifecycle()
    val productCount by viewModel.productCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { 
            HuertoTopBar(
                title = "Dashboard Root", 
                canNavigateBack = false
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Estadísticas Generales")
            Text(text = "Usuarios Registrados: $userCount")
            Text(text = "Productos Totales: $productCount")

            Spacer(modifier = Modifier.height(16.dp))

            HuertoButton(text = "Gestionar Usuarios", onClick = onNavigateUsers)
            HuertoButton(text = "Gestionar Productos", onClick = onNavigateProducts)
            
            if (onNavigateProviders != null) {
                HuertoButton(text = "Ver Proveedores", onClick = onNavigateProviders)
            }
            
            HuertoButton(text = "Crear Nuevo Usuario", onClick = onNavigateCreate)
            
            Spacer(modifier = Modifier.weight(1f))

            HuertoButton(
                text = "Sincronizar con Red", 
                onClick = { viewModel.sincronizarDatosConAdmins() },
                modifier = Modifier.fillMaxWidth()
            )

            HuertoButton(text = "Cerrar Sesión", onClick = onLogout)
        }
    }
}
