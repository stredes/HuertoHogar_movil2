package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.model.EstadoPedido
import com.example.huertohogar_mobil.model.Pedido
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.PedidoViewModel
import com.example.huertohogar_mobil.viewmodel.PedidoUiState
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPedidosScreen(
    onBack: () -> Unit,
    viewModel: PedidoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pedidoParaPago by remember { mutableStateOf<Pedido?>(null) }
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    val tabs = listOf("Activos", "Historial")

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Mis Pedidos",
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = tabSeleccionado) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabSeleccionado == index,
                        onClick = { tabSeleccionado = index },
                        text = { Text(title) }
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (tabSeleccionado) {
                    0 -> MostrarPedidosActivos(uiState, viewModel) { pedidoParaPago = it }
                    1 -> MostrarHistorialPedidos(uiState)
                }
            }
        }

        // Mostrar errores
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Mostrar snackbar o diálogo de error si lo necesitas
            }
        }

        if (pedidoParaPago != null) {
            SeleccionarMetodoPagoDialog(
                pedido = pedidoParaPago!!,
                onDismiss = { pedidoParaPago = null },
                onConfirmar = { metodo, datosTransferencia ->
                    viewModel.seleccionarMetodoPago(pedidoParaPago!!.pedidoId, metodo, datosTransferencia)
                    viewModel.marcarComoPagado(pedidoParaPago!!.pedidoId)
                    pedidoParaPago = null
                }
            )
        }
    }
}

@Composable
private fun MostrarPedidosActivos(
    uiState: PedidoUiState,
    viewModel: PedidoViewModel,
    onSeleccionarPago: (Pedido) -> Unit
) {
    if (uiState.isLoading && uiState.misPedidosActivos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.misPedidosActivos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "No tienes pedidos activos",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Tus pedidos completados aparecerán en el historial",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.misPedidosActivos, key = { it.pedidoId }) { pedido ->
                PedidoCard(
                    pedido = pedido,
                    esProveedor = false,
                    onAccion = { accion ->
                        when (accion) {
                            "SELECCIONAR_PAGO" -> onSeleccionarPago(pedido)
                            "CONFIRMAR_ENTREGA" -> viewModel.confirmarEntrega(pedido.pedidoId)
                            "CANCELAR" -> viewModel.cancelarPedido(pedido.pedidoId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MostrarHistorialPedidos(uiState: PedidoUiState) {
    if (uiState.misPedidosHistorial.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Sin historial de pedidos",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Los pedidos completados aparecerán aquí",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.misPedidosHistorial, key = { it.pedidoId }) { pedido ->
                PedidoCardHistorial(pedido = pedido)
            }
        }
    }
}

@Composable
fun PedidosProveedorScreen(
    onBack: () -> Unit,
    viewModel: PedidoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Pedidos Recibidos",
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.pedidosRecibidos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No has recibido pedidos aún", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.pedidosRecibidos, key = { it.pedidoId }) { pedido ->
                    PedidoCard(
                        pedido = pedido,
                        esProveedor = true,
                        onAccion = { accion ->
                            when (accion) {
                                "CONFIRMAR" -> viewModel.confirmarPedido(pedido.pedidoId)
                                "LISTO_DESPACHO" -> viewModel.marcarListoDespacho(pedido.pedidoId)
                                "EN_CAMINO" -> viewModel.marcarEnCamino(pedido.pedidoId)
                                "CANCELAR" -> viewModel.cancelarPedido(pedido.pedidoId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PedidoCard(
    pedido: Pedido,
    esProveedor: Boolean,
    onAccion: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (esProveedor) "De: ${pedido.compradorNombre}" else "Proveedor: ${pedido.proveedorEmail}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                EstadoChip(estado = pedido.estado)
            }

            HorizontalDivider()

            // Detalles del pedido
            val detalles = parseDetallePedido(pedido.detalleJson)
            Text("Productos:", style = MaterialTheme.typography.titleSmall)
            detalles.forEach { detalle ->
                Text(
                    text = "• ${detalle.cantidad}x ${detalle.nombre} (${formatoCLP(detalle.precio)})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            HorizontalDivider()

            // Total
            Text(
                text = "Total: ${formatoCLP(pedido.totalCLP)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Fecha
            Text(
                text = "Fecha: ${formatFecha(pedido.fechaPedido)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Método de pago
            pedido.metodoPago?.let {
                Text(
                    text = "Pago: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Dirección de entrega
            pedido.direccionEntrega?.let { direccion ->
                if (direccion.isNotBlank()) {
                    Text(
                        text = "Dirección: $direccion",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Datos de transferencia (solo para proveedor si aplica)
            if (esProveedor && pedido.datosTransferencia?.isNotBlank() == true) {
                Text(
                    text = "Datos transferencia: ${pedido.datosTransferencia}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // Acciones según el estado y rol
            Spacer(modifier = Modifier.height(8.dp))
            AccionesPedido(
                pedido = pedido,
                esProveedor = esProveedor,
                onAccion = onAccion
            )
        }
    }
}

@Composable
private fun EstadoChip(estado: String) {
    val estadoEnum = try {
        EstadoPedido.valueOf(estado)
    } catch (e: Exception) {
        EstadoPedido.PENDIENTE
    }

    val (text, color) = when (estadoEnum) {
        EstadoPedido.PENDIENTE -> "Pendiente" to MaterialTheme.colorScheme.error
        EstadoPedido.CONFIRMADO -> "Confirmado" to MaterialTheme.colorScheme.primary
        EstadoPedido.ESPERANDO_PAGO -> "Esperando Pago" to MaterialTheme.colorScheme.tertiary
        EstadoPedido.PAGADO -> "Pagado" to MaterialTheme.colorScheme.secondary
        EstadoPedido.LISTO_DESPACHO -> "Listo" to MaterialTheme.colorScheme.primary
        EstadoPedido.EN_CAMINO -> "En Camino" to MaterialTheme.colorScheme.secondary
        EstadoPedido.ENTREGADO -> "Entregado" to MaterialTheme.colorScheme.outline
        EstadoPedido.CANCELADO -> "Cancelado" to MaterialTheme.colorScheme.error
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun AccionesPedido(
    pedido: Pedido,
    esProveedor: Boolean,
    onAccion: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val estadoEnum = pedido.getEstadoEnum()

        if (esProveedor) {
            // Acciones del proveedor
            when (estadoEnum) {
                EstadoPedido.PENDIENTE -> {
                    HuertoButton(
                        text = "Confirmar",
                        onClick = { onAccion("CONFIRMAR") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { onAccion("CANCELAR") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rechazar")
                    }
                }
                EstadoPedido.CONFIRMADO, EstadoPedido.ESPERANDO_PAGO -> {
                    Text(
                        "Esperando que el cliente seleccione método de pago",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EstadoPedido.PAGADO -> {
                    HuertoButton(
                        text = "Marcar Listo",
                        onClick = { onAccion("LISTO_DESPACHO") },
                        modifier = Modifier.weight(1f)
                    )
                }
                EstadoPedido.LISTO_DESPACHO -> {
                    HuertoButton(
                        text = "En Camino",
                        onClick = { onAccion("EN_CAMINO") },
                        modifier = Modifier.weight(1f)
                    )
                }
                EstadoPedido.EN_CAMINO, EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO -> {
                    // No hay acciones disponibles
                }
            }
        } else {
            // Acciones del comprador
            when (estadoEnum) {
                EstadoPedido.PENDIENTE -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            "⏳ Esperando confirmación del proveedor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                EstadoPedido.CONFIRMADO -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "✅ Pedido Confirmado",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Selecciona tu método de pago para continuar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        HuertoButton(
                            text = "💳 Confirmar Método de Pago",
                            onClick = { onAccion("SELECCIONAR_PAGO") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                EstadoPedido.PAGADO -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            "✅ Pago confirmado. El proveedor está preparando tu pedido",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                EstadoPedido.LISTO_DESPACHO -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            "📦 Tu pedido está listo y será enviado pronto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                EstadoPedido.EN_CAMINO -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "🚚 ¡Tu pedido está en camino!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "El delivery llegará pronto. Confirma la recepción cuando lo recibas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        HuertoButton(
                            text = "✅ Confirmar Recepción del Pedido",
                            onClick = { onAccion("CONFIRMAR_ENTREGA") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                EstadoPedido.ENTREGADO -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            "✅ Pedido Entregado y Completado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                EstadoPedido.CANCELADO -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            "❌ Pedido Cancelado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                else -> {
                    // Estados intermedios sin acción específica
                }
            }
        }
    }
}

// Función auxiliar para parsear el detalle del pedido
data class DetallePedido(
    val productoId: String,
    val nombre: String,
    val cantidad: Int,
    val precio: Int
)

fun parseDetallePedido(detalleJson: String): List<DetallePedido> {
    return try {
        val jsonArray = JSONArray(detalleJson)
        List(jsonArray.length()) { i ->
            val item = jsonArray.getJSONObject(i)
            DetallePedido(
                productoId = item.getString("productoId"),
                nombre = item.getString("nombre"),
                cantidad = item.getInt("cantidad"),
                precio = item.getInt("precio")
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun formatFecha(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun SeleccionarMetodoPagoDialog(
    pedido: Pedido,
    onDismiss: () -> Unit,
    onConfirmar: (metodo: String, datosTransferencia: String?) -> Unit
) {
    var metodoPago by remember { mutableStateOf("EFECTIVO") }
    var datosTransferencia by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Método de Pago")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Total a pagar: ${formatoCLP(pedido.totalCLP)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider()

                Text("Selecciona tu método de pago:")

                // Opción Efectivo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = metodoPago == "EFECTIVO",
                        onClick = { metodoPago = "EFECTIVO" }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Efectivo en entrega",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Pagarás al recibir el pedido",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Opción Transferencia
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = metodoPago == "TRANSFERENCIA",
                        onClick = { metodoPago = "TRANSFERENCIA" }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Transferencia bancaria",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Ingresa los datos de tu transferencia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Campo de datos de transferencia
                if (metodoPago == "TRANSFERENCIA") {
                    OutlinedTextField(
                        value = datosTransferencia,
                        onValueChange = { datosTransferencia = it },
                        label = { Text("Datos de transferencia") },
                        placeholder = { Text("Ej: Banco, N° cuenta, N° operación") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
        },
        confirmButton = {
            HuertoButton(
                text = "Confirmar",
                onClick = {
                    onConfirmar(
                        metodoPago,
                        if (metodoPago == "TRANSFERENCIA") datosTransferencia else null
                    )
                },
                enabled = metodoPago != "TRANSFERENCIA" || datosTransferencia.isNotBlank()
            )
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun PedidoCardHistorial(pedido: Pedido) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Proveedor: ${pedido.proveedorEmail}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${pedido.pedidoId.take(12)}...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                EstadoChip(estado = pedido.estado)
            }

            HorizontalDivider()

            // Detalles del pedido
            val detalles = parseDetallePedido(pedido.detalleJson)
            Text("Productos:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            detalles.forEach { detalle ->
                Text(
                    text = "• ${detalle.cantidad}x ${detalle.nombre}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider()

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ${formatoCLP(pedido.totalCLP)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatFecha(pedido.fechaPedido),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Información adicional si está disponible
            if (!pedido.metodoPago.isNullOrBlank()) {
                Text(
                    text = "Pago: ${pedido.metodoPago}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Fecha de entrega si existe
            if (pedido.fechaEntrega != null && pedido.fechaEntrega!! > 0) {
                Text(
                    text = "Entregado: ${formatFecha(pedido.fechaEntrega!!)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
