package com.example.huertohogar_mobil.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.theme.BrownHuerto
import com.example.huertohogar_mobil.ui.theme.GreenHuerto
import com.example.huertohogar_mobil.ui.theme.YellowHuerto
import com.example.huertohogar_mobil.R

@Composable
fun ProductoCard(producto: Producto) {
    val context = LocalContext.current
    val imageRes = obtieneImagen(context, producto.imagen)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = producto.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 16.dp)
            )

            // Textos
            Column {
                // Badge simulado (Código)
                Text(
                    text = producto.code,
                    fontSize = 10.sp,
                    color = BrownHuerto,
                    modifier = Modifier.padding(bottom = 4.dp),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = producto.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenHuerto
                )

                Text(
                    text = "$${producto.precio} / ${producto.unidad}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun obtieneImagen(context: Context, imagen: String?): Int{
    val nombre = imagen?.replace(".jng", "") ?: "icono_huerto"
    val resouceId = context.resources.getIdentifier(nombre, "drawable", context.packageName)

    return if(resouceId == 0) R.drawable.icono_huerto else resouceId
}

