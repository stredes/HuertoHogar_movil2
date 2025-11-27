// Pantalla carrito
package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.CarritoItem
import com.example.huertohogar_mobil.ui.components.CartSummary
import com.example.huertohogar_mobil.viewmodel.MarketUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    ui: MarketUiState,
    onSumar: (Producto) -> Unit,
    onRestar: (Producto) -> Unit,
    onBack: () -> Unit,
    onCheckout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            if (ui.carrito.isNotEmpty()) {
                CartSummary(
                    total = ui.totalCLP,
                    onCheckout = onCheckout
                )
            }
        }
    ) { pv ->
        if (ui.carrito.isEmpty()) {
            Box(Modifier.padding(pv).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tu carrito está vacío")
            }
        } else {
            // Mapeamos id->qty a objetos Producto
            val lineas = ui.carrito.mapNotNull { (id, qty) ->
                ui.productos.firstOrNull { it.id == id }?.let { it to qty }
            }
            LazyColumn(
                modifier = Modifier.padding(pv).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lineas, key = { it.first.id }) { (p, qty) ->
                    CarritoItem(
                        producto = p,
                        quantity = qty,
                        onSumar = onSumar,
                        onRestar = onRestar
                    )
                }
            }
        }
    }
}
