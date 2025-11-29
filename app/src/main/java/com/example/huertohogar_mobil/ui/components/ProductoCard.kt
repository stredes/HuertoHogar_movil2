package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.screen.formatoCLP

@Composable
fun ProductoCard(
    p: Producto,
    onClick: () -> Unit,
    onAgregar: () -> Unit
) {
    HuertoCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val shape = RoundedCornerShape(12.dp)
            
            // Usamos el componente ProductImage abstracto
            ProductImage(
                producto = p,
                modifier = Modifier
                    .size(64.dp)
                    .clip(shape)
            )

            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.nombre, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text(p.descripcion, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                Text("${formatoCLP(p.precioCLP)} / ${p.unidad}", style = MaterialTheme.typography.labelLarge)
            }
            IconButton(onClick = onAgregar) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar")
            }
        }
    }
}
