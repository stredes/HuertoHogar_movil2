package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.screen.formatoCLP

@Composable
fun CarritoItem(
    producto: Producto,
    quantity: Int,
    onSumar: (Producto) -> Unit,
    onRestar: (Producto) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.SemiBold)
                Text("${formatoCLP(producto.precioCLP)} / ${producto.unidad}")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { onRestar(producto) }) { Text("−") }
                Text("$quantity", modifier = Modifier.width(28.dp))
                IconButton(onClick = { onSumar(producto) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Más")
                }
            }
        }
    }
}
