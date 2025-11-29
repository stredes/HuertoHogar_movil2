package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.model.MensajeContacto
import com.example.huertohogar_mobil.ui.components.HuertoCard
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.AdminViewModel

@Composable
fun AdminNotificacionesScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val mensajes by viewModel.mensajes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Mensajes de Contacto",
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        }
    ) { padding ->
        if (mensajes.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay mensajes pendientes", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mensajes, key = { it.id }) { msj ->
                    MensajeCard(
                        mensaje = msj,
                        onToggle = { viewModel.marcarComoRespondido(msj.id, msj.respondido) },
                        onDelete = { viewModel.eliminarMensaje(msj) }
                    )
                }
            }
        }
    }
}

@Composable
fun MensajeCard(
    mensaje: MensajeContacto,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColor = if (mensaje.respondido) 
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) 
    else 
        MaterialTheme.colorScheme.surfaceVariant

    HuertoCard(
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(mensaje.nombre, fontWeight = FontWeight.Bold)
                    Text(mensaje.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(mensaje.fecha, style = MaterialTheme.typography.labelSmall)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(mensaje.mensaje)
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onToggle) {
                    Icon(
                        imageVector = if(mensaje.respondido) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = if(mensaje.respondido) Color.Green else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (mensaje.respondido) "Respondido" else "Marcar Respondido")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
