package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.viewmodel.MarketUiState
import java.text.NumberFormat
import java.util.Locale

private fun formatoCLP(valor: Int): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(valor)
}

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
                BottomAppBar {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total: ${formatoCLP(ui.total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(onClick = onCheckout) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(8.dp)); Text("Continuar")
                        }
                    }
                }
            }
        }
    ) { pv ->
        if (ui.carrito.isEmpty()) {
            Box(Modifier.padding(pv).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tu carrito está vacío")
                }
            }
        } else {
            // Corregido: mapea el carrito buscando el producto por su 'code'
            val lineas = ui.carrito.mapNotNull { (productCode, qty) ->
                ui.productos.firstOrNull { it.code == productCode }?.let { it to qty }
            }
            LazyColumn(
                Modifier.padding(pv).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Corregido: la key es el 'code' del producto, que es único
                items(lineas, key = { it.first.code }) { (p, qty) ->
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(p.nombre, fontWeight = FontWeight.SemiBold)
                                Text("${formatoCLP(p.precio)} / ${p.unidad}")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { onRestar(p) }) { Text("−", style = MaterialTheme.typography.titleLarge) }
                                Text("$qty", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.widthIn(min = 28.dp))
                                IconButton(onClick = { onSumar(p) }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Más")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}