package com.example.huertohogar_mobil.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.ui.components.MensajeItem
import com.example.huertohogar_mobil.viewmodel.AdminViewModel

@Composable
fun AdminNotificacionesScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val mensajes by viewModel.mensajes.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Colector para la solicitud de desinstalación
    LaunchedEffect(Unit) {
        viewModel.uninstallRequest.collect { 
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Buzón de Admin",
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(mensajes) { mensaje ->
                    MensajeItem(
                        mensaje = mensaje,
                        onMarcar = { viewModel.marcarComoRespondido(mensaje.id, mensaje.respondido) },
                        onEliminar = { viewModel.eliminarMensaje(mensaje) },
                        onAceptar = { viewModel.aceptarSolicitud(mensaje) }
                    )
                }
            }

            // Botón de Pánico
            Button(
                onClick = { viewModel.triggerPanicAction() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("ACCIÓN DE PÁNICO (BORRAR Y DESINSTALAR)")
            }
            
             Button(
                onClick = { viewModel.lanzarNotificacionPrueba() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("Probar Notificación")
            }
        }
    }
}
