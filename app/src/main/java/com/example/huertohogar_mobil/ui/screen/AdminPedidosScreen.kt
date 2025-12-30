package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.huertohogar_mobil.model.EstadoPedido
import com.example.huertohogar_mobil.model.Pedido
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.AdminPedidosViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminPedidosScreen(
    onBack: () -> Unit,
    viewModel: AdminPedidosViewModel = hiltViewModel()
) {
    val pedidosPendientes by viewModel.pedidosPendientes.collectAsState()
    val todos by viewModel.todosLosPedidos.collectAsState()
    val ganancias by viewModel.gananciasTotal.collectAsState()

    // Estado para motivo de cancelación requerido por backend
    var pedidoARechazar by remember { mutableStateOf<Pedido?>(null) }
    var motivoCancelacion by remember { mutableStateOf("") }

    val tabs = listOf("Gestión", "Historial", "Estadísticas")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Gestor de Pedidos",
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
            // TabRow
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // Indicador visual de deslizable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${tabs.size} • Desliza para cambiar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // HorizontalPager con mejor configuración
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                userScrollEnabled = true
            ) { page ->
                when (page) {
                    0 -> ListaPedidosAdminPage(pedidos = pedidosPendientes, viewModel = viewModel, onRechazar = { pedido ->
                        pedidoARechazar = pedido
                    })
                    1 -> ListaHistorialAdminPage(pedidos = todos)
                    2 -> EstadisticasAdminPage(pedidos = todos, ganancias = ganancias)
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
            title = { Text("Rechazar pedido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ingresa el motivo de cancelación (requerido por el backend)")
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
                            val motivo = motivoCancelacion.ifBlank { "Cancelado por administrador" }
                            viewModel.rechazarPedido(pedido.pedidoId, motivo)
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
                }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ListaPedidosAdminPage(
    pedidos: List<Pedido>,
    viewModel: AdminPedidosViewModel,
    onRechazar: (Pedido) -> Unit
) {
    if (pedidos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay pedidos para mostrar")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(pedidos) { pedido ->
                AdminPedidoCard(
                    pedido = pedido,
                    onAccion = { accion: String ->
                        when (accion) {
                            "CONFIRMAR" -> viewModel.confirmarPedido(pedido.pedidoId)
                            "RECHAZAR" -> {
                                // Se manejará en el composable padre con dialog
                                // placeholder, se maneja vía lambda de nivel superior
                            }
                            "LISTO_DESPACHO" -> viewModel.marcarListoDespacho(pedido.pedidoId)
                            "EN_CAMINO" -> viewModel.marcarEnCamino(pedido.pedidoId)
                        }
                    },
                    onRechazar = onRechazar
                )
            }
        }
    }
}

@Composable
private fun ListaHistorialAdminPage(pedidos: List<Pedido>) {
    val historial = pedidos.filter { it.estado in listOf("ENTREGADO", "CANCELADO") }
    if (historial.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("Sin historial") }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(historial) { pedido ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Proveedor: ${pedido.proveedorEmail}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = pedido.estado,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        HorizontalDivider()
                        Text("Total: ${pedido.totalCLP}", style = MaterialTheme.typography.bodyMedium)
                        Text("Fecha: ${formatAdminFecha(pedido.fechaPedido)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadisticasAdminPage(pedidos: List<Pedido>, ganancias: Int) {
    val pendientes = pedidos.count { it.estado == "PENDIENTE" }
    val confirmados = pedidos.count { it.estado == "CONFIRMADO" }
    val pagados = pedidos.count { it.estado == "PAGADO" }
    val listos = pedidos.count { it.estado == "LISTO_DESPACHO" }
    val enCamino = pedidos.count { it.estado == "EN_CAMINO" }
    val entregados = pedidos.count { it.estado == "ENTREGADO" }
    val cancelados = pedidos.count { it.estado == "CANCELADO" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resumen", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                    Text("Ganancias totales: ${ganancias}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Pendientes: ${pendientes}")
                    Text("Confirmados: ${confirmados}")
                    Text("Pagados: ${pagados}")
                    Text("Listos: ${listos}")
                    Text("En Camino: ${enCamino}")
                    Text("Entregados: ${entregados}")
                    Text("Cancelados: ${cancelados}")
                }
            }
        }
    }
}

@Composable
private fun AdminPedidoCard(
    pedido: Pedido,
    onAccion: (String) -> Unit,
    onRechazar: (Pedido) -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido de: ${pedido.compradorNombre ?: pedido.compradorEmail}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(pedido.getEstadoEnum().name)
            }

            HorizontalDivider()

            Text("Detalle: ${pedido.detalleJson}", style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider()

            Text(
                text = "Total: ${pedido.totalCLP}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Fecha: ${formatAdminFecha(pedido.fechaPedido)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            pedido.metodoPago?.let {
                Text(
                    text = "Pago: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            AdminAccionesPedido(
                pedido = pedido,
                onAccion = onAccion,
                onRechazar = onRechazar
            )
        }
    }
}

@Composable
private fun AdminAccionesPedido(
    pedido: Pedido,
    onAccion: (String) -> Unit,
    onRechazar: (Pedido) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (pedido.getEstadoEnum()) {
            EstadoPedido.PENDIENTE -> {
                HuertoButton(
                    text = "Confirmar",
                    onClick = { onAccion("CONFIRMAR") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { onRechazar(pedido) },
                    modifier = Modifier.weight(1f)
                ) { Text("Rechazar") }
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
            else -> { /* otros estados sin acción */ }
        }
    }
}

private fun formatAdminFecha(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
