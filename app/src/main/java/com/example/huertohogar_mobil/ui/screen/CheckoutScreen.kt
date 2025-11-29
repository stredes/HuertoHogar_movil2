package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.MarketUiState

@Composable
fun CheckoutScreen(
    ui: MarketUiState,
    onBack: () -> Unit,
    onFinalizar: () -> Unit
) {
    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Checkout",
                canNavigateBack = true,
                onNavigateBack = onBack
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
            
            HuertoButton(
                text = "Finalizar compra",
                onClick = onFinalizar
            )
        }
    }
}
