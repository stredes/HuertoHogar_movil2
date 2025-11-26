package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    amigoId: Int,
    amigoNombre: String?, // Pasado por argumento
    currentUserEmail: String?,
    viewModel: SocialViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    // Inicializar usuario y cargar chat
    LaunchedEffect(Unit) {
        if (currentUserEmail != null) {
            viewModel.setCurrentUser(currentUserEmail)
            viewModel.cargarChat(amigoId)
        }
    }

    val mensajes by viewModel.chatMessages.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var texto by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(amigoNombre ?: "Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (texto.isNotBlank()) {
                            viewModel.enviarMensaje(amigoId, texto)
                            texto = ""
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            reverseLayout = true // Mensajes nuevos abajo (técnica visual) pero ordenaremos la lista normal
            // Mejor: reverseLayout=false y scroll to bottom, pero para simplicidad usaremos lista normal
            // Corrección: reverseLayout requiere lista invertida. Usaremos lista normal con Arrangement.Bottom
        ) {
            // Room devuelve orden ascendente (antiguo -> nuevo). 
            // Para chat es mejor mostrarlos tal cual.
            items(mensajes) { msg ->
                val esMio = msg.remitenteId == currentUser?.id
                ChatBubble(msg, esMio)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(mensaje: MensajeChat, esMio: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (esMio) 16.dp else 0.dp,
                        bottomEnd = if (esMio) 0.dp else 16.dp
                    )
                )
                .background(
                    if (esMio) MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Text(
                text = mensaje.contenido,
                color = if (esMio) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
