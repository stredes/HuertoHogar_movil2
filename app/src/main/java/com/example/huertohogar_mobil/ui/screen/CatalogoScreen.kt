package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.ProductoCard
import com.example.huertohogar_mobil.ui.theme.BrownHuerto
import com.example.huertohogar_mobil.viewmodel.MarketViewModel

@Composable
fun CatalogoScreen(
    navController: NavController, 
    viewModel: MarketViewModel // 1. Recibimos el MarketViewModel
) {
    // 2. Observamos el estado (MarketUiState) que expone el ViewModel
    val uiState by viewModel.ui.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Catálogo de Productos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = BrownHuerto,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 3. Mostramos la lista de productos del estado
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.productosFiltrados) { producto ->
                // Usamos el ProductoCard que ya tenías.
                // ¡Más adelante podemos modificarlo para que sea interactivo!
                ProductoCard(producto = producto)
            }
        }
    }
}