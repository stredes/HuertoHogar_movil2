package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.RootViewModel

@Composable
fun RootProviderProductsScreen(
    email: String,
    viewModel: RootViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val allProducts by viewModel.productos.collectAsStateWithLifecycle()
    // Filtrar productos por el email del proveedor
    val providerProducts = allProducts.filter { it.providerEmail == email }

    Scaffold(
        topBar = {
            HuertoTopBar(title = "Productos de: $email", canNavigateBack = true, onNavigateBack = onBack)
        }
    ) { padding ->
        if (providerProducts.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Este proveedor no tiene productos.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(providerProducts) { prod ->
                    // Reusamos el item de Root, permitiendo eliminar desde aquí si se desea
                    ProductoRootItem(
                        producto = prod,
                        onEdit = { /* Opcional: Permitir edición */ },
                        onDelete = { viewModel.eliminarProducto(prod) }
                    )
                }
            }
        }
    }
}
