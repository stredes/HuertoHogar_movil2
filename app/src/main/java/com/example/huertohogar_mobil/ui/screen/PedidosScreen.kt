package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPedidosScreen(
    onBack: () -> Unit,
    viewModel: PedidoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pedidoParaPago by remember { mutableStateOf<Pedido?>(null) }

    var pedidoACancelar by remember { mutableStateOf<Pedido?>(null) }
    var motivoCancelacion by remember { mutableStateOf("") }

    val tabs = listOf("Activos", "Historial")
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = selectedTab) { tabs.size }
    val scope = rememberCoroutineScope()

    // Mantener sincronizados TabRow y PagerState cuando el usuario desliza
    LaunchedEffect(pagerState.currentPage) {
        if (selectedTab != pagerState.currentPage) selectedTab = pagerState.currentPage
    }

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
            // Tabs sincronizadas con el pager
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // Indicador de página actual (debug)
            Text(
                text = "Página ${selectedTab + 1}/${tabs.size} - Desliza horizontalmente →",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            // HorizontalPager FUERA del PullToRefreshBox
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
                pageSpacing = 0.dp
            ) { page ->
                // PullToRefreshBox DENTRO de cada página
                PullToRefreshBox(
                    isRefreshing = uiState.isLoading,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (page) {
                        0 -> MostrarPedidosActivos(uiState, viewModel, onSeleccionarPago = { pedidoParaPago = it }, onCancelarPedido = { pedidoACancelar = it })
                        1 -> MostrarHistorialPedidos(uiState)
                    }
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
                onConfirmar = { metodo: String, datosTransferencia: String? ->
                    viewModel.seleccionarMetodoPago(pedidoParaPago!!.pedidoId, metodo, datosTransferencia)
                    viewModel.marcarComoPagado(pedidoParaPago!!.pedidoId)
                    pedidoParaPago = null
                }
            )
        }

        if (pedidoACancelar != null) {
            AlertDialog(
                onDismissRequest = {
                    pedidoACancelar = null
                    motivoCancelacion = ""
                },
                title = { Text("Cancelar pedido") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ingresa el motivo de cancelación (requerido por backend)")
                        OutlinedTextField(
                            value = motivoCancelacion,
                            onValueChange = { motivoCancelacion = it },
                            placeholder = { Text("Motivo") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    HuertoButton(
                        text = "Enviar",
                        onClick = {
                            pedidoACancelar?.let { pedido ->
                                val motivo = motivoCancelacion.ifBlank { "Cancelado por comprador" }
                                viewModel.cancelarPedido(pedido.pedidoId, motivo)
                            }
                            pedidoACancelar = null
                            motivoCancelacion = ""
                        }
                    )
                },
                dismissButton = {
                    TextButton(onClick = {
                        pedidoACancelar = null
                        motivoCancelacion = ""
                    }) { Text("Cerrar") }
                }
            )
        }
    }
}

@Composable
private fun MostrarPedidosActivos(
    uiState: PedidoUiState,
    viewModel: PedidoViewModel,
    onSeleccionarPago: (Pedido) -> Unit,
    onCancelarPedido: (Pedido) -> Unit
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
                    onAccion = { accion: String ->
                        when (accion) {
                            "SELECCIONAR_PAGO" -> onSeleccionarPago(pedido)
                            "CONFIRMAR_ENTREGA" -> viewModel.confirmarEntrega(pedido.pedidoId)
                            "CANCELAR" -> onCancelarPedido(pedido)
                        }
                    },
                    onCancelar = onCancelarPedido
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

    var pedidoARechazar by remember { mutableStateOf<Pedido?>(null) }
    var motivoCancelacion by remember { mutableStateOf("") }

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
                        onAccion = { accion: String ->
                            when (accion) {
                                "CONFIRMAR" -> viewModel.confirmarPedido(pedido.pedidoId)
                                "LISTO_DESPACHO" -> viewModel.marcarListoDespacho(pedido.pedidoId)
                                "EN_CAMINO" -> viewModel.marcarEnCamino(pedido.pedidoId)
                                "CANCELAR" -> pedidoARechazar = pedido
                            }
                        },
                        onCancelar = { pedidoARechazar = it }
                    )
                }
            }
        }
    }

    if (pedidoARechazar != null) {
        AlertDialog(
            onDismissRequest = {
                pedidoARechazar = null
                motivoCancelacion = ""
            },
            title = { Text("Cancelar pedido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ingresa el motivo de cancelación (requerido por backend)")
                    OutlinedTextField(
                        value = motivoCancelacion,
                        onValueChange = { motivoCancelacion = it },
                        placeholder = { Text("Motivo") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                HuertoButton(
                    text = "Enviar",
                    onClick = {
                        pedidoARechazar?.let { pedido ->
                            val motivo = motivoCancelacion.ifBlank { "Cancelado por proveedor" }
                            viewModel.cancelarPedido(pedido.pedidoId, motivo)
                        }
                        pedidoARechazar = null
                        motivoCancelacion = ""
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    pedidoARechazar = null
                    motivoCancelacion = ""
                }) { Text("Cerrar") }
            }
        )
    }
}

@Composable
fun PedidoCard(
    pedido: Pedido,
    esProveedor: Boolean,
    onAccion: (String) -> Unit,
    onCancelar: (Pedido) -> Unit = {}
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
            PedidoAcciones(
                pedido = pedido,
                esProveedor = esProveedor,
                onAccion = onAccion,
                onCancelar = onCancelar
            )
        }
    }
}

@Composable
fun EstadoChip(estado: String) {
    val estadoEnum = try {
        EstadoPedido.valueOf(estado)
    } catch (_: Exception) {
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
fun PedidoAcciones(
    pedido: Pedido,
    esProveedor: Boolean,
    onAccion: (String) -> Unit,
    onCancelar: (Pedido) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (esProveedor) {
            when (pedido.getEstadoEnum()) {
                EstadoPedido.PENDIENTE -> {
                    HuertoButton(
                        text = "Confirmar",
                        onClick = { onAccion("CONFIRMAR") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { onCancelar(pedido) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Rechazar") }
                }
                EstadoPedido.PAGADO -> {
                    HuertoButton(
                        text = "Listo",
                        onClick = { onAccion("LISTO_DESPACHO") },
                        modifier = Modifier.weight(1f)
                    )
                }
                EstadoPedido.LISTO_DESPACHO -> {
                    HuertoButton(
                        text = "En camino",
                        onClick = { onAccion("EN_CAMINO") },
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> { /* sin acciones */ }
            }
        } else {
            when (pedido.getEstadoEnum()) {
                EstadoPedido.PENDIENTE -> {
                    OutlinedButton(
                        onClick = { onCancelar(pedido) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar") }
                }
                EstadoPedido.LISTO_DESPACHO -> {
                    HuertoButton(
                        text = "Recibido",
                        onClick = { onAccion("CONFIRMAR_ENTREGA") },
                        modifier = Modifier.weight(1f)
                    )
                }
                EstadoPedido.EN_CAMINO -> {
                    HuertoButton(
                        text = "Recibido",
                        onClick = { onAccion("CONFIRMAR_ENTREGA") },
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> { /* sin acciones */ }
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
    } catch (_: Exception) {
        emptyList()
    }
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
            val fechaEntrega = pedido.fechaEntrega
            if (fechaEntrega != null && fechaEntrega > 0) {
                Text(
                    text = "Entregado: ${formatFecha(fechaEntrega)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

internal fun formatFecha(timestamp: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(timestamp))
}
