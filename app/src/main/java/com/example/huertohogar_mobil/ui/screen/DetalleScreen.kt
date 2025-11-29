package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoIconButton
import com.example.huertohogar_mobil.ui.components.HuertoOutlinedButton
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.ui.components.ProductImage
import com.example.huertohogar_mobil.viewmodel.MarketUiState

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
            HuertoTopBar(
                title = producto.nombre,
                canNavigateBack = true,
                onNavigateBack = onBack,
                actions = {
                    BadgedBox(badge = {
                        if (ui.countCarrito > 0) Badge { Text("${ui.countCarrito}") }
                    }) {
                        HuertoIconButton(onClick = irCarrito) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
             // Visualización de la imagen (URI o Recurso)
            ProductImage(
                producto = producto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Text(producto.descripcion, style = MaterialTheme.typography.bodyLarge)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${formatoCLP(producto.precioCLP)} / ${producto.unidad}", 
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            HuertoButton(
                text = "Agregar al carrito",
                onClick = onAgregar,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) }
            )
            
            // Sección Admin: Editar y Eliminar
            if (isAdmin) {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Text("Panel de Administrador", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Wrap components manually for weight since HuertoOutlinedButton takes modifier
                    HuertoOutlinedButton(
                        text = "Editar",
                        onClick = onEditar,
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                    )
                    
                    HuertoOutlinedButton(
                        text = "Eliminar",
                        onClick = onEliminar,
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.Filled.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }
}
