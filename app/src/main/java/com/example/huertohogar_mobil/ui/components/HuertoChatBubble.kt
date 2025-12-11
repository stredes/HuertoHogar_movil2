package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.model.EstadoMensaje
import com.example.huertohogar_mobil.model.TipoContenido

@Composable
fun HuertoChatBubble(
    message: String,
    isMine: Boolean,
    estado: Int = EstadoMensaje.ENVIADO,
    tipoContenido: String = TipoContenido.TEXTO
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
                // Renderizado condicional según el tipo de contenido
                when (tipoContenido) {
                    TipoContenido.TEXTO -> {
                        Text(
                            text = message,
                            color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TipoContenido.IMAGEN -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Image, contentDescription = "Imagen", tint = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📷 Imagen adjunta",
                                fontStyle = FontStyle.Italic,
                                color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    TipoContenido.VIDEO -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video", tint = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎥 Video adjunto",
                                fontStyle = FontStyle.Italic,
                                color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    TipoContenido.AUDIO -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = "Audio", tint = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎤 Audio adjunto",
                                fontStyle = FontStyle.Italic,
                                color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    TipoContenido.UBICACION -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Ubicación", tint = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📍 Ubicación: $message",
                                fontStyle = FontStyle.Italic,
                                color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    else -> {
                         Text(
                            text = message,
                            color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Mostrar estado solo si es mi mensaje
                if (isMine) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val icon = when(estado) {
                        EstadoMensaje.ENVIANDO -> Icons.Default.AccessTime
                        EstadoMensaje.ENVIADO -> Icons.Default.Check
                        EstadoMensaje.RECIBIDO -> Icons.Default.DoneAll
                        EstadoMensaje.LEIDO -> Icons.Default.DoneAll
                        EstadoMensaje.ERROR -> Icons.Default.Warning
                        else -> Icons.Default.Check
                    }
                    
                    val iconColor = when(estado) {
                        EstadoMensaje.ERROR -> Color.Red
                        EstadoMensaje.LEIDO -> Color(0xFF64B5F6) // Azul claro
                        else -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = iconColor
                    )
                }
            }
        }
    }
}
