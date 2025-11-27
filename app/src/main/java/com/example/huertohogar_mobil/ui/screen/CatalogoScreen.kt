package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.ProductoCard
import com.example.huertohogar_mobil.viewmodel.MarketUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    ui: MarketUiState,
    onBuscar: (String) -> Unit,
    onVer: (Producto) -> Unit,
    onAgregar: (Producto) -> Unit,
    irCarrito: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HuertoHogar", fontWeight = FontWeight.Bold) },
                actions = {
                    BadgedBox(badge = {
                        if (ui.countCarrito > 0) Badge { Text("${ui.countCarrito}") }
                    }) {
                        IconButton(onClick = irCarrito) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                        }
                    }
                }
            )
        }
    ) { pv ->
        Column(Modifier.padding(pv)) {
            var query by remember { mutableStateOf(ui.query) }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; onBuscar(it) },
                label = { Text("Buscar producto...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ui.productosFiltrados, key = { it.id }) { p ->
                    ProductoCard(
                        p = p,
                        onClick = { onVer(p) },
                        onAgregar = { onAgregar(p) }
                    )
                }
            }
        }
    }
}
