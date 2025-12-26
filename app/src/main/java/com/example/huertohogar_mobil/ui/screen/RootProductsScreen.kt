package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.HuertoCard
import com.example.huertohogar_mobil.ui.components.HuertoIconButton
import com.example.huertohogar_mobil.ui.components.HuertoSearchField
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.RootViewModel

@Composable
fun RootProductsScreen(
    viewModel: RootViewModel = hiltViewModel(),
    onEdit: (String) -> Unit,
    onBack: () -> Unit,
    providerEmailFilter: String? = null
) {
    val productos by viewModel.productos.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var filtroProveedor by remember { mutableStateOf(providerEmailFilter) }

    val proveedores = productos.mapNotNull { it.providerEmail }.distinct().sorted()

    val listaMostrada = productos
        .filter { p -> filtroProveedor.isNullOrBlank() || p.providerEmail == filtroProveedor }
        .filter { p -> query.isBlank() || p.nombre.contains(query, true) }

    Scaffold(
        topBar = {
            val titulo = if (filtroProveedor != null) "Productos de $filtroProveedor" else "Gestión de Productos (Global)"
            HuertoTopBar(title = titulo, canNavigateBack = true, onNavigateBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                item {
                    Card(
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total", style = MaterialTheme.typography.labelLarge)
                            Text(productos.size.toString(), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Filtrados", style = MaterialTheme.typography.labelLarge)
                            Text(listaMostrada.size.toString(), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }

            HuertoSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Buscar por nombre"
            )

            if (proveedores.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    item {
                        FilterChip(
                            selected = filtroProveedor == null,
                            onClick = { filtroProveedor = null },
                            label = { Text("Todos") }
                        )
                    }
                    items(proveedores.size) { index ->
                        val correo = proveedores[index]
                        FilterChip(
                            selected = filtroProveedor == correo,
                            onClick = { filtroProveedor = correo },
                            label = { Text(correo.take(20)) }
                        )
                    }
                }
            }

            HorizontalDivider()

            if (listaMostrada.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron productos.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
