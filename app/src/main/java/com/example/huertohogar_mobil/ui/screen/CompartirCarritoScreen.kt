package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.ui.components.HuertoAvatar
import com.example.huertohogar_mobil.ui.components.HuertoCard
import com.example.huertohogar_mobil.ui.components.HuertoIconButton
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.MarketUiState
import com.example.huertohogar_mobil.viewmodel.MarketViewModel
import com.example.huertohogar_mobil.viewmodel.SocialViewModel

@Composable
fun CompartirCarritoScreen(
    marketUiState: MarketUiState,
    socialViewModel: SocialViewModel = hiltViewModel(),
    marketViewModel: MarketViewModel = hiltViewModel(), // Para obtener detalle de productos si fuera necesario
    onBack: () -> Unit,
    onCompartidoExitoso: () -> Unit
) {
    val amigos by socialViewModel.amigos.collectAsStateWithLifecycle()
    val currentUser by socialViewModel.currentUser.collectAsStateWithLifecycle()
    
    // Si no hay usuario logueado, intentar obtenerlo
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            // Asumimos que se puede obtener el email del AuthViewModel o similar,
            // pero aquí confiaremos en que el SocialViewModel ya tiene o puede tener el usuario
            // Si es null, la lista de amigos estará vacía.
        }
    }

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Compartir Lista de Compras",
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text(
                "Selecciona un amigo para enviarle tu lista:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (amigos.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No tienes amigos conectados para compartir.", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(amigos) { amigo ->
                        AmigoShareItem(
                            user = amigo,
                            onShare = {
                                val listaTexto = generarTextoLista(marketUiState)
                                socialViewModel.enviarMensaje(amigo.id, listaTexto)
                                onCompartidoExitoso()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AmigoShareItem(user: User, onShare: () -> Unit) {
    HuertoCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HuertoAvatar(name = user.name)
            Spacer(modifier = Modifier.width(12.dp))
            Text(user.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            HuertoIconButton(onClick = onShare) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar Lista")
            }
        }
    }
}

fun generarTextoLista(ui: MarketUiState): String {
    val sb = StringBuilder()
    sb.append("🛒 **Mi Lista de Compras HuertoHogar** 🛒\n\n")
    
    val items = ui.carrito.mapNotNull { (id, qty) ->
        ui.productos.firstOrNull { it.id == id }?.let { it to qty }
    }
    
    items.forEach { (prod, qty) ->
        sb.append("- ${prod.nombre} x$qty (${prod.unidad})\n")
    }
    
    sb.append("\nTotal estimado: $${ui.totalCLP}")
    return sb.toString()
}
