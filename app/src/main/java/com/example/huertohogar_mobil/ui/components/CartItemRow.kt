package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.screen.formatoCLP

@Composable
fun CartItemRow(
    producto: Producto,
    cantidad: Int,
    onSumar: () -> Unit,
    onRestar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.SemiBold)
                Text(
                    "${formatoCLP(producto.precioCLP)} / ${producto.unidad}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onRestar) { Text("−", style = MaterialTheme.typography.titleLarge) }
                Text("$cantidad", modifier = Modifier.width(28.dp), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = onSumar) {
                    Icon(Icons.Filled.Add, contentDescription = "Más")
                }
            }
        }
    }
}
