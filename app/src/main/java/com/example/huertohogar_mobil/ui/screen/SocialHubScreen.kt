package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.model.Solicitud
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.ui.components.HuertoAvatar
import com.example.huertohogar_mobil.ui.components.HuertoCard
import com.example.huertohogar_mobil.ui.components.HuertoIconButton
import com.example.huertohogar_mobil.ui.components.HuertoSearchField
import com.example.huertohogar_mobil.ui.components.SectionHeader
import com.example.huertohogar_mobil.viewmodel.SocialViewModel

@Composable
fun SocialHubScreen(
    currentUserEmail: String?, // Email del usuario actual para inicializar
    viewModel: SocialViewModel = hiltViewModel(),
    onChatClick: (Int) -> Unit
) {
    // Inicializar el usuario actual en el VM
    LaunchedEffect(currentUserEmail) {
        if (currentUserEmail != null) {
            viewModel.setCurrentUser(currentUserEmail)
        }
    }

    val amigos by viewModel.amigos.collectAsStateWithLifecycle()
    val solicitudes by viewModel.solicitudesPendientes.collectAsStateWithLifecycle()
    val resultadosBusqueda by viewModel.searchResults.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionHeader(title = "Comunidad HuertoHogar", centered = false)
        Spacer(modifier = Modifier.height(16.dp))

        // Buscador
        HuertoSearchField(
            query = query,
            onQueryChange = { 
                query = it
                viewModel.buscarPersonas(it)
            },
            placeholder = "Buscar personas..."
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Resultados de Búsqueda
            if (query.isNotEmpty()) {
                item {
                    Text("Resultados de búsqueda", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                items(resultadosBusqueda) { user ->
                    PersonaItem(
                        user = user, 
                        isFriend = false, 
                        onAction = { viewModel.enviarSolicitudAmistad(user) }, // Cambiado a enviar solicitud
                        actionIcon = Icons.Default.PersonAdd
                    )
                }
                if (resultadosBusqueda.isEmpty()) {
                    item { Text("No se encontraron usuarios.", style = MaterialTheme.typography.bodyMedium) }
                }
            } else {
                // 2. Solicitudes Pendientes (Buzón)
                if (solicitudes.isNotEmpty()) {
                    item {
                        Text("Solicitudes de Amistad", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    items(solicitudes) { solicitud ->
                        SolicitudItem(
                            solicitud = solicitud,
                            onAccept = { viewModel.aceptarSolicitud(solicitud) },
                            onReject = { viewModel.rechazarSolicitud(solicitud) }
                        )
                    }
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
                }

                // 3. Mis Amigos
                item {
                    Text("Mis Amigos", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                if (amigos.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("Aún no tienes amigos. ¡Busca a alguien!", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                } else {
                    items(amigos) { amigo ->
                        PersonaItem(
                            user = amigo, 
                            isFriend = true, 
                            onAction = { onChatClick(amigo.id) },
                            actionIcon = Icons.AutoMirrored.Filled.Message
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonaItem(
    user: User, 
    isFriend: Boolean, 
    onAction: () -> Unit,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    HuertoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HuertoAvatar(name = user.name)
            
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
            }
            HuertoIconButton(onClick = onAction) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SolicitudItem(
    solicitud: Solicitud,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    HuertoCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HuertoAvatar(name = solicitud.senderName)
            
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(solicitud.senderName, fontWeight = FontWeight.Bold)
                Text("Quiere ser tu amigo", style = MaterialTheme.typography.bodySmall)
            }
            
            Row {
                IconButton(onClick = onAccept) {
                    Icon(Icons.Default.Check, contentDescription = "Aceptar", tint = Color.Green)
                }
                IconButton(onClick = onReject) {
                    Icon(Icons.Default.Close, contentDescription = "Rechazar", tint = Color.Red)
                }
            }
        }
    }
}
