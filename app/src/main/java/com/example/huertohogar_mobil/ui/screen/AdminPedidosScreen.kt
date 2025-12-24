package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPedidosScreen(
    onBack: () -> Unit,
    viewModel: AdminPedidosViewModel = hiltViewModel()
) {
    val pedidosPendientes by viewModel.pedidosPendientes.collectAsState()

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Pedidos Pendientes",
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        }
    ) { paddingValues ->
        if (pedidosPendientes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay pedidos pendientes")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pedidosPendientes) { pedido ->
                    AdminPedidoCard(
                        pedido = pedido,
                        onAccion = { accion: String ->
                            when (accion) {
                                "CONFIRMAR" -> viewModel.confirmarPedido(pedido.pedidoId)
                                "RECHAZAR" -> viewModel.rechazarPedido(pedido.pedidoId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminPedidoCard(
    pedido: Pedido,
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
                onAccion = onAccion
            )
        }
    }
}

@Composable
private fun AdminAccionesPedido(
    pedido: Pedido,
    onAccion: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (pedido.getEstadoEnum() == EstadoPedido.PENDIENTE) {
            HuertoButton(
                text = "Confirmar",
                onClick = { onAccion("CONFIRMAR") },
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = { onAccion("RECHAZAR") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Rechazar")
            }
        }
    }
}

private fun formatAdminFecha(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
