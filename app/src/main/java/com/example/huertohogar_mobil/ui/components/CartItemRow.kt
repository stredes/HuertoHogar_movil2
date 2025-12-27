package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
    onRestar: () -> Unit,
    onEliminar: (() -> Unit)? = null
) {
    HuertoCard(
        modifier = Modifier.fillMaxWidth()
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
                HuertoTextButton(
                    text = "−",
                    onClick = onRestar,
                    textStyle = MaterialTheme.typography.titleLarge
                )
                Text("$cantidad", modifier = Modifier.width(28.dp), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = onSumar) {
                    Icon(Icons.Filled.Add, contentDescription = "Más")
                }
                // Botón de eliminar
                if (onEliminar != null) {
                    IconButton(onClick = onEliminar) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
