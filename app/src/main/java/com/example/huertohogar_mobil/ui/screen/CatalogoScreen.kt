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
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.launch

@Composable
fun CatalogoScreen(
    ui: MarketUiState,
    onBuscar: (String) -> Unit,
    onSelectProvider: (String?) -> Unit,
    onVer: (Producto) -> Unit,
    onAgregar: (Producto) -> Unit,
    irCarrito: () -> Unit
) {
    val showingProviderSelection = ui.selectedProviderEmail == null && ui.admins.isNotEmpty()
    val tabs = listOf("Proveedores", "Productos")
    var selectedTab by rememberSaveable { mutableStateOf(if (showingProviderSelection) 0 else 1) }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = selectedTab) { tabs.size }
    val scope = rememberCoroutineScope()

    // Cuando cambia el proveedor seleccionado en el estado, sincronizamos la pestaña
    LaunchedEffect(ui.selectedProviderEmail) {
        if (ui.selectedProviderEmail != null && selectedTab != 1) {
            selectedTab = 1
            scope.launch { pagerState.animateScrollToPage(1) }
        } else if (ui.selectedProviderEmail == null && selectedTab != 0 && ui.admins.isNotEmpty()) {
            selectedTab = 0
            scope.launch { pagerState.animateScrollToPage(0) }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (selectedTab != pagerState.currentPage) selectedTab = pagerState.currentPage
    }

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
            // TabRow sincronizado con pager
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index; scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }

            // Indicador de página actual (debug)
            Text(
                text = "Página ${selectedTab + 1}/${tabs.size} - Desliza horizontalmente →",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
                pageSpacing = 0.dp
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (page) {
                    0 -> {
                        // MODO DASHBOARD DE PROVEEDORES
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (ui.admins.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No hay proveedores disponibles", color = MaterialTheme.colorScheme.secondary)
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        "Selecciona un proveedor para ver su catálogo",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(16.dp)
                                    )

                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(ui.admins) { admin ->
                                            ProviderCard(
                                                name = admin.name,
                                                email = admin.email,
                                                onClick = {
                                                    onSelectProvider(admin.email)
                                                    // El cambio de tab lo gestiona el LaunchedEffect
                                                }
                                            )
                                        }
                                        // Opción adicional
                                        item {
                                            ProviderCard(
                                                name = "Red Privada General",
                                                email = "Todos los demás",
                                                onClick = {
                                                    onSelectProvider("admin@huertohogar.com")
                                                    // El cambio de tab lo gestiona el LaunchedEffect
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // MODO LISTA DE PRODUCTOS (de un proveedor o todos si no hay admins)
                        var query by remember { mutableStateOf(ui.query) }

                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                HuertoSearchField(
                                    query = query,
                                    onQueryChange = { query = it; onBuscar(it) },
                                    placeholder = "Buscar en este catálogo..."
                                )
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 12.dp)
                            ) {
                                if (ui.productosFiltrados.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
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
