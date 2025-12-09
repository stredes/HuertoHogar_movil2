package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.HuertoCard
import com.example.huertohogar_mobil.ui.components.HuertoIconButton
import com.example.huertohogar_mobil.ui.components.HuertoSearchField
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.ui.components.ProductoCard
import com.example.huertohogar_mobil.viewmodel.MarketUiState

@Composable
fun CatalogoScreen(
    ui: MarketUiState,
    onBuscar: (String) -> Unit,
    onSelectProvider: (String?) -> Unit,
    onVer: (Producto) -> Unit,
    onAgregar: (Producto) -> Unit,
    irCarrito: () -> Unit
) {
    // Si no hay proveedor seleccionado y hay multiples proveedores, mostrar dashboard de selección
    val showingProviderSelection = ui.selectedProviderEmail == null && ui.admins.isNotEmpty()

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = if (ui.selectedProviderEmail != null) "Productos de ${ui.selectedProviderEmail}" else "HuertoHogar - Proveedores",
                // Si estamos filtrando por un proveedor específico, permitimos volver a la selección
                canNavigateBack = ui.selectedProviderEmail != null,
                onNavigateBack = { onSelectProvider(null) }, // Volver a "Todos" (Dashboard)
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
            
            if (showingProviderSelection) {
                // MODO DASHBOARD DE PROVEEDORES
                Text(
                    "Selecciona un proveedor para ver su catálogo",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ui.admins) { admin ->
                        ProviderCard(
                            name = admin.name,
                            email = admin.email,
                            onClick = { 
                                // AL HACER CLICK, LLAMAMOS AL CALLBACK
                                onSelectProvider(admin.email) 
                            }
                        )
                    }
                    
                    // Opción para ver productos sin proveedor (o admin general) si existen
                    item {
                        ProviderCard(
                            name = "HuertoHogar General",
                            email = "Todos los demás",
                            onClick = { 
                                // Usamos un filtro especial o null con flag si quisiéramos "otros"
                                onSelectProvider("admin@huertohogar.com") 
                            } 
                        )
                    }
                }

            } else {
                // MODO LISTA DE PRODUCTOS (DE UN PROVEEDOR O TODOS SI NO HAY ADMINS)
                var query by remember { mutableStateOf(ui.query) }
                
                // Barra de búsqueda dentro del catálogo del proveedor
                Box(modifier = Modifier.padding(12.dp)) {
                    HuertoSearchField(
                        query = query,
                        onQueryChange = { query = it; onBuscar(it) },
                        placeholder = "Buscar en este catálogo..."
                    )
                }
                
                // LISTA DE PRODUCTOS
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    if (ui.productosFiltrados.isEmpty()) {
                         item {
                             Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                 Text(
                                     if (ui.selectedProviderEmail != null) "Este proveedor no tiene productos." 
                                     else "No se encontraron productos.",
                                     color = MaterialTheme.colorScheme.secondary, 
                                     textAlign = TextAlign.Center
                                 )
                             }
                         }
                    } else {
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
    }
}

@Composable
fun ProviderCard(
    name: String,
    email: String,
    onClick: () -> Unit
) {
    HuertoCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            // IMPORTANTE: clickable debe ser lo primero o estar en el Modifier externo
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Store, 
                contentDescription = null, 
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
