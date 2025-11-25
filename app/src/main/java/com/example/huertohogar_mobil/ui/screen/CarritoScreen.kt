package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.utils.formatoCLP
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
                BottomAppBar {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total: ${formatoCLP(ui.totalCLP)}", fontWeight = FontWeight.Bold)
                        Button(onClick = onCheckout) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp)); Text("Continuar")
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
                    Card {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(p.nombre, fontWeight = FontWeight.SemiBold)
                                Text("${formatoCLP(p.precio)} / ${p.unidad}")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { onRestar(p) }) { Text("−") }
                                Text("$qty", modifier = Modifier.width(28.dp))
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