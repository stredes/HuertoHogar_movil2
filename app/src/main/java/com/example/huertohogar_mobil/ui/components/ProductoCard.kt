package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val shape = RoundedCornerShape(12.dp)
            p.imagenResId?.let {
                Image(
                    painter = painterResource(it),
                    contentDescription = p.nombre,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(shape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.nombre, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text(p.descripcion, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${formatoCLP(p.precioCLP)} / ${p.unidad}")
            }
            IconButton(onClick = onAgregar) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar")
            }
        }
    }
}
