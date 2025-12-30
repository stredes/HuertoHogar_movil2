package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.huertohogar_mobil.viewmodel.AdminPedidosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuzonProveedorScreen(
    onBack: () -> Unit,
    viewModel: AdminPedidosViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val pedidosPendientes by viewModel.pedidosPendientes.collectAsStateWithLifecycle()
    val todosLosPedidos by viewModel.todosLosPedidos.collectAsStateWithLifecycle()
    val gananciasTotal by viewModel.gananciasTotal.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Buzón de Proveedor",
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
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Gestión de Pedidos") },
                    icon = {
                        Badge {
                            Text("${pedidosPendientes.size}")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Historial") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Estadísticas") }
                )
            }

            // Contenido según tab seleccionado con PullToRefresh
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    0 -> PedidosPendientesTab(
                        pedidos = pedidosPendientes,
                        onConfirmar = { viewModel.confirmarPedido(it) },
                        onRechazar = { viewModel.rechazarPedido(it) },
                        onMarcarListo = { viewModel.marcarListoDespacho(it) },
                        onMarcarEnCamino = { viewModel.marcarEnCamino(it) }
                    )
                    1 -> HistorialPedidosTab(pedidos = todosLosPedidos)
                    2 -> EstadisticasTab(
                        pedidos = todosLosPedidos,
                        gananciasTotal = gananciasTotal
                    )
                }
            }
        }
    }
}

@Composable
private fun PedidosPendientesTab(
    pedidos: List<Pedido>,
    onConfirmar: (String) -> Unit,
    onRechazar: (String) -> Unit,
    onMarcarListo: (String) -> Unit,
    onMarcarEnCamino: (String) -> Unit
) {
    val pedidosActivos = pedidos.filter {
        it.estado !in listOf("ENTREGADO", "CANCELADO")
    }

    if (pedidosActivos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "No hay pedidos activos",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Todos los pedidos han sido procesados",
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
            items(pedidosActivos, key = { it.pedidoId }) { pedido ->
                PedidoProveedorCard(
                    pedido = pedido,
                    onConfirmar = { onConfirmar(pedido.pedidoId) },
                    onRechazar = { onRechazar(pedido.pedidoId) },
                    onMarcarListo = { onMarcarListo(pedido.pedidoId) },
                    onMarcarEnCamino = { onMarcarEnCamino(pedido.pedidoId) }
                )
            }
        }
    }
}

@Composable
private fun HistorialPedidosTab(pedidos: List<Pedido>) {
    val pedidosPorCliente = remember(pedidos) {
        pedidos.groupBy { it.compradorEmail }
            .map { (email, pedidosCliente) ->
                email to pedidosCliente.sortedByDescending { it.fechaPedido }
            }
            .sortedByDescending { it.second.firstOrNull()?.fechaPedido ?: 0 }
    }

    if (pedidosPorCliente.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay pedidos en el historial")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pedidosPorCliente) { (clienteEmail, pedidosCliente) ->
                ClienteHistorialCard(
                    clienteEmail = clienteEmail,
                    pedidos = pedidosCliente
                )
            }
        }
    }
}

@Composable
private fun EstadisticasTab(pedidos: List<Pedido>, gananciasTotal: Int) {
    val pedidosEntregados = remember(pedidos) {
        pedidos.filter { it.estado == "ENTREGADO" }
    }

    val pedidosEnProceso = remember(pedidos) {
        pedidos.filter { it.estado !in listOf("ENTREGADO", "CANCELADO") }
    }

    val pedidosCancelados = remember(pedidos) {
        pedidos.filter { it.estado == "CANCELADO" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Ganancias Totales",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        formatoCLP(gananciasTotal),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "De ${pedidosEntregados.size} pedidos entregados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Pedidos",
                    value = pedidos.size.toString(),
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "En Proceso",
                    value = pedidosEnProceso.size.toString(),
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Entregados",
                    value = pedidosEntregados.size.toString(),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Cancelados",
                    value = pedidosCancelados.size.toString(),
                    icon = Icons.Default.Cancel,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                "Pedidos Recientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(pedidos.take(10)) { pedido ->
            PedidoMiniCard(pedido)
        }
    }
}

@Composable
private fun PedidoProveedorCard(
    pedido: Pedido,
    onConfirmar: () -> Unit,
    onRechazar: () -> Unit,
    onMarcarListo: () -> Unit,
    onMarcarEnCamino: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = pedido.compradorNombre ?: "Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pedido.compradorEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            val detalles = parseDetallePedido(pedido.detalleJson)
            Text("Productos:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            detalles.forEach { detalle ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${detalle.cantidad}x ${detalle.nombre}")
                    Text(formatoCLP(detalle.precio * detalle.cantidad))
                }
            }

            HorizontalDivider()

            pedido.direccionEntrega?.let { direccion ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        direccion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            pedido.metodoPago?.let { metodo ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Pago: $metodo")
                }
            }

            Text(
                text = "Total: ${formatoCLP(pedido.totalCLP)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Fecha: ${formatFecha(pedido.fechaPedido)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AccionesProveedor(
                pedido = pedido,
                onConfirmar = onConfirmar,
                onRechazar = onRechazar,
                onMarcarListo = onMarcarListo,
                onMarcarEnCamino = onMarcarEnCamino
            )
        }
    }
}

@Composable
private fun AccionesProveedor(
    pedido: Pedido,
    onConfirmar: () -> Unit,
    onRechazar: () -> Unit,
    onMarcarListo: () -> Unit,
    onMarcarEnCamino: () -> Unit
) {
    val estadoEnum = pedido.getEstadoEnum()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (estadoEnum) {
            EstadoPedido.PENDIENTE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HuertoButton(
                        text = "Confirmar Pedido",
                        onClick = onConfirmar,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = onRechazar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Rechazar")
                    }
                }
                Text(
                    "⏳ Nuevo pedido recibido - Revisa y confirma",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            EstadoPedido.CONFIRMADO, EstadoPedido.ESPERANDO_PAGO -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "Esperando que el cliente confirme el pago",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            EstadoPedido.PAGADO -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                "✅ Pago Confirmado",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Empaca el pedido y márcalo como listo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    HuertoButton(
                        text = "📦 Marcar Listo para Despacho",
                        onClick = onMarcarListo,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            EstadoPedido.LISTO_DESPACHO -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "📦 Pedido Listo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "El pedido está empacado. Envíalo con el delivery",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    HuertoButton(
                        text = "🚚 Marcar En Camino (Delivery Salió)",
                        onClick = onMarcarEnCamino,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            EstadoPedido.EN_CAMINO -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "🚚 Pedido En Camino",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            "El delivery está llevando el pedido al cliente. Se notificará automáticamente cuando el cliente confirme la recepción.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            EstadoPedido.ENTREGADO -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "✅ Pedido Entregado y Completado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            EstadoPedido.CANCELADO -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "❌ Pedido Cancelado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClienteHistorialCard(
    clienteEmail: String,
    pedidos: List<Pedido>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = pedidos.firstOrNull()?.compradorNombre ?: "Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = clienteEmail,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Badge {
                    Text("${pedidos.size} pedidos")
                }
            }

            HorizontalDivider()

            pedidos.take(3).forEach { pedido ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            formatFecha(pedido.fechaPedido),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            pedido.estado,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        formatoCLP(pedido.totalCLP),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (pedidos.size > 3) {
                Text(
                    "y ${pedidos.size - 3} más...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PedidoMiniCard(pedido: Pedido) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pedido.compradorNombre ?: pedido.compradorEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    formatFecha(pedido.fechaPedido),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatoCLP(pedido.totalCLP),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    pedido.estado,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
