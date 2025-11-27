// Pantalla detalle
package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.ProductDetailsContent
import com.example.huertohogar_mobil.viewmodel.MarketUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    ui: MarketUiState,
    producto: Producto,
    isAdmin: Boolean = false,
    onBack: () -> Unit,
    irCarrito: () -> Unit,
    onAgregar: () -> Unit,
    onEditar: () -> Unit = {},
    onEliminar: () -> Unit = {}
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
        ProductDetailsContent(
            modifier = Modifier.padding(pv),
            producto = producto,
            isAdmin = isAdmin,
            onAgregar = onAgregar,
            onEditar = onEditar,
            onEliminar = onEliminar
        )
    }
}
