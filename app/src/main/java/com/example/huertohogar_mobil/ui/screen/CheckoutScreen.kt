package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.MarketUiState
import com.example.huertohogar_mobil.viewmodel.PedidoViewModel

@Composable
fun CheckoutScreen(
    ui: MarketUiState,
    onBack: () -> Unit,
    onPedidoCreado: () -> Unit,
    pedidoViewModel: PedidoViewModel = hiltViewModel()
) {
    var metodoPago by remember { mutableStateOf("EFECTIVO") }
    var datosTransferencia by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var isCreatingOrder by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Obtener el proveedor del carrito
    val proveedorEmail = remember(ui.carrito, ui.productos) {
        val primerProductoId = ui.carrito.keys.firstOrNull()
        val primerProducto = ui.productos.find { it.id == primerProductoId }
        primerProducto?.providerEmail ?: "admin@huertohogar.com"
    }

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Finalizar Compra",
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        }
    ) { pv ->
        Column(
            Modifier
                .padding(pv)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resumen de productos
            Text("Resumen de compra", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ui.carrito.forEach { (id, qty) ->
                        val p: Producto? = ui.productos.firstOrNull { it.id == id }
                        if (p != null) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("$qty x ${p.nombre}")
                                Text(formatoCLP(p.precioCLP * qty))
                            }
                        }
                    }
                    HorizontalDivider()
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total:", fontWeight = FontWeight.Bold)
                        Text(formatoCLP(ui.totalCLP), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Dirección de entrega
            Text("Dirección de entrega", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Ingresa tu dirección") },
                placeholder = { Text("Ej: Calle 123, Depto 45, Comuna") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Método de pago
            Text("Método de pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    MetodoPagoOption(
                        texto = "Efectivo en entrega",
                        seleccionado = metodoPago == "EFECTIVO",
                        onClick = { metodoPago = "EFECTIVO" }
                    )
                    HorizontalDivider()
                    MetodoPagoOption(
                        texto = "Transferencia bancaria",
                        seleccionado = metodoPago == "TRANSFERENCIA",
                        onClick = { metodoPago = "TRANSFERENCIA" }
                    )
                }
            }

            // Datos de transferencia (si aplica)
            if (metodoPago == "TRANSFERENCIA") {
                OutlinedTextField(
                    value = datosTransferencia,
                    onValueChange = { datosTransferencia = it },
                    label = { Text("Datos de transferencia") },
                    placeholder = { Text("Ej: Banco, N° de cuenta, N° de operación") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            // Información del proveedor
            Text(
                text = "Proveedor: $proveedorEmail",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Botón de finalizar
            HuertoButton(
                text = if (isCreatingOrder) "Creando pedido..." else "Confirmar y crear pedido",
                onClick = {
                    if (direccion.isBlank()) {
                        errorMessage = "Debes ingresar una dirección de entrega"
                        showError = true
                        return@HuertoButton
                    }
                    if (metodoPago == "TRANSFERENCIA" && datosTransferencia.isBlank()) {
                        errorMessage = "Debes ingresar los datos de la transferencia"
                        showError = true
                        return@HuertoButton
                    }

                    isCreatingOrder = true

                    // Convertir el carrito a Map<Producto, Int>
                    val carritoConProductos = ui.carrito.mapNotNull { (id, qty) ->
                        val producto = ui.productos.find { it.id == id }
                        producto?.let { it to qty }
                    }.toMap()

                    // Crear el pedido
                    pedidoViewModel.crearPedido(
                        carrito = carritoConProductos,
                        proveedorEmail = proveedorEmail,
                        total = ui.totalCLP,
                        metodoPago = metodoPago,
                        datosTransferencia = if (metodoPago == "TRANSFERENCIA") datosTransferencia else null,
                        direccion = direccion
                    )

                    // Notificar que el pedido fue creado
                    onPedidoCreado()
                },
                enabled = !isCreatingOrder,
                modifier = Modifier.fillMaxWidth()
            )

            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MetodoPagoOption(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionado, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(selected = seleccionado, onClick = onClick)
        Text(texto)
    }
}
