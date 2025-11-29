package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.EstadoMensaje

@Composable
fun HuertoChatBubble(
    message: String,
    isMine: Boolean,
    estado: Int = EstadoMensaje.ENVIADO // Por defecto enviado para retrocompatibilidad
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 0.dp,
                        bottomEnd = if (isMine) 0.dp else 16.dp
                    )
                )
                .background(
                    if (isMine) MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = message,
                    color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
                
                // Mostrar estado solo si es mi mensaje
                if (isMine) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        imageVector = when(estado) {
                            EstadoMensaje.ENVIANDO -> Icons.Default.AccessTime
                            EstadoMensaje.ENVIADO -> Icons.Default.Check
                            EstadoMensaje.RECIBIDO -> Icons.Default.DoneAll // Podría usarse para "leído"
                            EstadoMensaje.ERROR -> Icons.Default.ErrorOutline
                            else -> Icons.Default.Check
                        },
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (estado == EstadoMensaje.ERROR) Color.Red else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
