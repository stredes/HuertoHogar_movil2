package com.example.huertohogar_mobil.ui.products

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.huertohogar_mobil.model.Product
import com.example.huertohogar_mobil.viewmodel.ProductsUiState
import com.example.huertohogar_mobil.viewmodel.ProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Mostrar Toast si hay mensajes de operación
    LaunchedEffect(operationStatus) {
        operationStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Productos Firebase") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Ejemplo rápido: Agregar producto "dummy" sin imagen para probar
                viewModel.addProduct("Producto Test ${System.currentTimeMillis()}", 1000, null)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ProductsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProductsUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProductsUiState.Success -> {
                    if (state.products.isEmpty()) {
                        Text("No hay productos", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn {
                            items(state.products) { product ->
                                ProductItem(
                                    product = product,
                                    onDelete = { viewModel.deleteProduct(product) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (product.imagenUrl.isNotEmpty()) {
                AsyncImage(
                    model = product.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.nombre, style = MaterialTheme.typography.titleMedium)
                Text(text = "$ ${product.precioCLP}", style = MaterialTheme.typography.bodyMedium)
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
