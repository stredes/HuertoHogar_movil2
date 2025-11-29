package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.CartItemRow
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.MarketUiState

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
            HuertoTopBar(
                title = "Carrito",
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        },
        bottomBar = {
            if (ui.carrito.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total: ${formatoCLP(ui.totalCLP)}", fontWeight = FontWeight.Bold)
                        
                        // Using HuertoButton instead of raw Button
                        Box(modifier = Modifier.width(150.dp)) {
                            HuertoButton(
                                text = "Continuar",
                                onClick = onCheckout,
                                icon = { Icon(Icons.Filled.Add, contentDescription = null) }
                            )
                        }
                    }
                }
            }
        }
    ) { pv ->
        if (ui.carrito.isEmpty()) {
            Box(Modifier.padding(pv).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tu carrito está vacío")
            }
        } else {
            val lineas = ui.carrito.mapNotNull { (id, qty) ->
                ui.productos.firstOrNull { it.id == id }?.let { it to qty }
            }
            LazyColumn(
                Modifier.padding(pv).fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lineas, key = { it.first.id }) { (p, qty) ->
                    CartItemRow(
                        producto = p,
                        cantidad = qty,
                        onSumar = { onSumar(p) },
                        onRestar = { onRestar(p) }
                    )
                }
            }
        }
    }
}
