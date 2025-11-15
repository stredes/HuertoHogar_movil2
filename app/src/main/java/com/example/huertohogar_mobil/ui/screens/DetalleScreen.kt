// Pantalla detalle
package com.example.huertohogar_mobil.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.data.Producto
import com.example.huertohogar_mobil.viewmodel.MarketUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    ui: MarketUiState,
    producto: Producto,
    onBack: () -> Unit,
    irCarrito: () -> Unit,
    onAgregar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(producto.nombre, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    BadgedBox(badge = {
                        if (ui.countCarrito > 0) Badge { Text("${ui.countCarrito}") }
                    }) {
                        IconButton(onClick = irCarrito) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                        }
                    }
                }
            )
        }
    ) { pv ->
        Column(
            Modifier
                .padding(pv)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            producto.imagenResId?.let {
                Image(
                    painter = painterResource(it),
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Text(producto.descripcion)
            Text("${formatoCLP(producto.precioCLP)} / ${producto.unidad}", fontWeight = FontWeight.SemiBold)
            Button(
                onClick = onAgregar,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Agregar al carrito")
            }
        }
    }
}
