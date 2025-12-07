package com.example.huertohogar_mobil.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.ui.components.HuertoChatBubble
import com.example.huertohogar_mobil.ui.components.HuertoIconButton
import com.example.huertohogar_mobil.ui.components.HuertoTextField
import com.example.huertohogar_mobil.viewmodel.SocialViewModel
import com.example.huertohogar_mobil.model.TipoContenido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    amigoId: Int,
    amigoNombre: String?, 
    currentUserEmail: String?,
    viewModel: SocialViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        if (currentUserEmail != null) {
            viewModel.setCurrentUser(currentUserEmail)
            viewModel.cargarChat(amigoId)
        }
    }

    val mensajes by viewModel.chatMessages.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val connectedPeers by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val amigos by viewModel.amigos.collectAsStateWithLifecycle()
    
    // Buscar si el amigo está conectado
    val amigo = amigos.find { it.id == amigoId }
    val isConnected = amigo != null && connectedPeers.contains(amigo.email)

    var texto by remember { mutableStateOf("") }
    var showAttachments by remember { mutableStateOf(false) }

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.adjuntarFoto(it.toString(), amigoId)
        }
        showAttachments = false
    }

    // Launcher para seleccionar video
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.adjuntarVideo(it.toString(), amigoId)
        }
        showAttachments = false
    }
    
    // Launcher para seleccionar audio
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.adjuntarAudio(it.toString(), amigoId)
        }
        showAttachments = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = amigoNombre ?: "Chat", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        // Indicador de estado (punto verde/rojo)
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) Color.Green else Color.Red)
                        )
                    }
                },
                navigationIcon = {
                    HuertoIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    IconButton(onClick = { showAttachments = !showAttachments }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar")
                    }
                    
                    DropdownMenu(
                        expanded = showAttachments,
                        onDismissRequest = { showAttachments = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Fotos") },
                            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                            onClick = { 
                                imagePickerLauncher.launch("image/*")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Audios") },
                            leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null) },
                            onClick = { 
                                audioPickerLauncher.launch("audio/*")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Videos") },
                            leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) },
                            onClick = { 
                                videoPickerLauncher.launch("video/*")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ubicación") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            onClick = { 
                                // Simulación de ubicación actual
                                viewModel.adjuntarUbicacion(-33.4489, -70.6693, amigoId)
                                showAttachments = false
                            }
                        )
                    }
                }

                HuertoTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    modifier = Modifier.weight(1f),
                    placeholder = "Escribe un mensaje...",
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                HuertoIconButton(
                    onClick = {
                        if (texto.isNotBlank()) {
                            viewModel.enviarMensaje(amigoId, texto)
                            texto = ""
                        }
                    },
                    filled = true
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
        ) {
            items(mensajes.reversed()) { msg -> 
                val esMio = msg.remitenteId == currentUser?.id
                HuertoChatBubble(
                    message = msg.contenido,
                    isMine = esMio,
                    estado = msg.estado,
                    tipoContenido = msg.tipoContenido
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
