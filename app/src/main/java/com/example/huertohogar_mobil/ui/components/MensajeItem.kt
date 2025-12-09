package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.MensajeContacto

@Composable
fun MensajeItem(
    mensaje: MensajeContacto,
    onMarcar: (Boolean) -> Unit,
    onEliminar: () -> Unit,
    onAceptar: () -> Unit // Nuevo callback
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = mensaje.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(text = mensaje.email, style = MaterialTheme.typography.bodySmall)
                }
                Checkbox(checked = mensaje.respondido, onCheckedChange = onMarcar)
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensaje.mensaje, style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón Aceptar
            Button(
                onClick = onAceptar,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Aceptar")
            }
        }
    }
}
