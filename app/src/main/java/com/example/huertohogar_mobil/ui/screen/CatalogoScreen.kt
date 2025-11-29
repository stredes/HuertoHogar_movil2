package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.HuertoIconButton
import com.example.huertohogar_mobil.ui.components.HuertoSearchField
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.ui.components.ProductoCard
import com.example.huertohogar_mobil.viewmodel.MarketUiState

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
            HuertoTopBar(
                title = "HuertoHogar",
                actions = {
                    BadgedBox(badge = {
                        if (ui.countCarrito > 0) Badge { Text("${ui.countCarrito}") }
                    }) {
                        HuertoIconButton(onClick = irCarrito) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                        }
                    }
                }
            )
        }
    ) { pv ->
        Column(Modifier.padding(pv)) {
            var query by remember { mutableStateOf(ui.query) }
            
            Box(modifier = Modifier.padding(12.dp)) {
                HuertoSearchField(
                    query = query,
                    onQueryChange = { query = it; onBuscar(it) },
                    placeholder = "Buscar producto..."
                )
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
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
