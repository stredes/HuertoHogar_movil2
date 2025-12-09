package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoIconButton
import com.example.huertohogar_mobil.ui.components.HuertoOutlinedButton
import com.example.huertohogar_mobil.ui.components.HuertoTextArea
import com.example.huertohogar_mobil.ui.components.HuertoTextField
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.ui.components.ProductImage
import com.example.huertohogar_mobil.viewmodel.MarketUiState
import com.example.huertohogar_mobil.viewmodel.MarketViewModel

@Composable
fun DetalleScreen(
    ui: MarketUiState,
    producto: Producto,
    isAdmin: Boolean = false,
    currentUserEmail: String? = null,
    viewModel: MarketViewModel = hiltViewModel(), 
    onBack: () -> Unit,
    irCarrito: () -> Unit,
    onAgregar: () -> Unit,
    onEditar: () -> Unit = {},
    onEliminar: () -> Unit = {}
) {
    var showContactDialog by remember { mutableStateOf(false) }

    // Lógica para determinar si el usuario actual es dueño o tiene permisos root
    val isOwner = !producto.providerEmail.isNullOrBlank() && 
                  producto.providerEmail.equals(currentUserEmail, ignoreCase = true)
    
    // Si isAdmin es true, pero no es owner, asumimos que es otro admin competidor, 
    // a menos que sea root. (Asumiremos que si currentUserEmail es 'root', es root).
    // O si el producto no tiene provider (sistema), cualquiera admin edita.
    val isRoot = currentUserEmail == "root"
    val hasEditPermissions = isAdmin && (isOwner || isRoot || producto.providerEmail.isNullOrBlank())

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
             // Visualización de la imagen
            ProductImage(
                producto = producto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Text(producto.descripcion, style = MaterialTheme.typography.bodyLarge)
            
            if (!producto.providerEmail.isNullOrBlank()) {
                 Text(
                    text = "Vendido por: ${producto.providerEmail}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                 )
            }
            
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
            
            // Si el producto no es mío, muestro botón de contacto (aunque sea admin)
            // Lógica: Si NO tengo permisos de edición (porque no soy el dueño), o si explícitamente no es mío.
            // Si soy admin y veo el producto de otro, quiero contactarlo.
            if (!producto.providerEmail.isNullOrBlank() && !isOwner) {
                HuertoOutlinedButton(
                    text = "Contactar al Vendedor",
                    onClick = { showContactDialog = true },
                    icon = { Icon(Icons.Filled.Chat, contentDescription = null) }
                )
            }
            
            // Sección Admin: Solo visible si tengo permisos reales sobre el producto
            if (hasEditPermissions) {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Text("Panel de Administración", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
    
    if (showContactDialog) {
        ProductContactDialog(
            providerEmail = producto.providerEmail ?: "",
            productName = producto.nombre,
            onDismiss = { showContactDialog = false },
            onSend = { nombre, msg ->
                viewModel.enviarContacto(nombre, producto.providerEmail ?: "", "[$nombre pregunta por ${producto.nombre}]: $msg")
                showContactDialog = false
            }
        )
    }
}

@Composable
fun ProductContactDialog(
    providerEmail: String,
    productName: String,
    onDismiss: () -> Unit,
    onSend: (String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("Hola, me interesa tu producto $productName.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contactar a $providerEmail") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HuertoTextField(value = nombre, onValueChange = { nombre = it }, label = "Tu Nombre")
                HuertoTextArea(value = mensaje, onValueChange = { mensaje = it }, label = "Mensaje")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSend(nombre, mensaje) },
                enabled = nombre.isNotBlank() && mensaje.isNotBlank()
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
