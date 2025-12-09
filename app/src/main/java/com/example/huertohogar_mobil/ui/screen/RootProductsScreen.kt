package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.HuertoCard
import com.example.huertohogar_mobil.ui.components.HuertoIconButton
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.RootViewModel

@Composable
fun RootProductsScreen(
    viewModel: RootViewModel = hiltViewModel(),
    onEdit: (String) -> Unit,
    onBack: () -> Unit,
    providerEmailFilter: String? = null // Parametro opcional para filtrar por proveedor
) {
    val productos by viewModel.productos.collectAsStateWithLifecycle()
    
    // Filtrar productos si se pasa un email de proveedor
    val listaMostrada = if (providerEmailFilter != null) {
        productos.filter { it.providerEmail == providerEmailFilter }
    } else {
        productos
    }

    Scaffold(
        topBar = {
            val titulo = if (providerEmailFilter != null) "Productos de $providerEmailFilter" else "Gestión de Productos (Global)"
            HuertoTopBar(title = titulo, canNavigateBack = true, onNavigateBack = onBack)
        }
    ) { padding ->
        if (listaMostrada.isEmpty()) {
             Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontraron productos.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listaMostrada) { prod ->
                    ProductoRootItem(
                        producto = prod,
                        onEdit = { onEdit(prod.id) },
                        onDelete = { viewModel.eliminarProducto(prod) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductoRootItem(producto: Producto, onEdit: () -> Unit, onDelete: () -> Unit) {
    HuertoCard {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Bold)
                Text("${producto.precioCLP} CLP - ${producto.unidad}", style = MaterialTheme.typography.bodySmall)
                if (!producto.providerEmail.isNullOrEmpty()) {
                    Text("Prov: ${producto.providerEmail}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            HuertoIconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            HuertoIconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
