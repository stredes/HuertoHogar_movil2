package com.example.huertohogar_mobil.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huertohogar_mobil.data.Producto
import com.example.huertohogar_mobil.viewmodel.MarketUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    ui: MarketUiState,
    onBuscar: (String) -> Unit,
    onVer: (Producto) -> Unit,
    onAgregar: (Producto) -> Unit,
    irCarrito: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HuertoHogar", fontWeight = FontWeight.Bold) },
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
        Column(Modifier.padding(pv)) {
            var query by remember { mutableStateOf(ui.query) }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; onBuscar(it) },
                label = { Text("Buscar producto...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ui.productosFiltrados, key = { it.id }) { p ->
                    ProductoCard(
                        p = p,
                        onClick = { onVer(p) },
                        onAgregar = { onAgregar(p) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductoCard(
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
