package com.example.huertohogar_mobil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.data.Producto
import com.example.huertohogar_mobil.viewmodel.MarketUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    ui: MarketUiState,
    onBack: () -> Unit,
    onFinalizar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { pv ->
        Column(
            Modifier.padding(pv).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Resumen de compra", style = MaterialTheme.typography.titleLarge)
            ui.carrito.forEach { (id, qty) ->
                val p: Producto? = ui.productos.firstOrNull { it.id == id }
                if (p != null) Text("- $qty x ${p.nombre} (${formatoCLP(p.precioCLP)})")
            }
            Divider()
            Text("Total: ${formatoCLP(ui.totalCLP)}", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onFinalizar) { Text("Finalizar compra") }
        }
    }
}
