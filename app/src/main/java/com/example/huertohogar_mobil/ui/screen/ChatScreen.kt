package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.ui.components.HuertoChatBubble
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.SocialViewModel

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
            HuertoTopBar(
                title = amigoNombre ?: "Chat",
                canNavigateBack = true,
                onNavigateBack = onBack
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
            reverseLayout = true 
            // Nota: Usualmente invertimos la lista para chats, pero depende de cómo venga del VM. 
            // Asumiendo que queremos lo último abajo. Si la lista viene cronológica ASC, reverseLayout=false y scroll to bottom.
            // Si viene DESC (el 0 es el mas nuevo), entonces reverseLayout=true.
            // En este código original estaba reverseLayout=true pero items(mensajes) tal cual.
            // Mantendremos consistencia visual.
        ) {
            items(mensajes) { msg ->
                val esMio = msg.remitenteId == currentUser?.id
                HuertoChatBubble(
                    message = msg.contenido,
                    isMine = esMio
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
