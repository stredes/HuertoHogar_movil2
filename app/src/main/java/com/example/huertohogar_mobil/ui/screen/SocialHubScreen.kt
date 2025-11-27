package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.model.User
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
    val resultadosBusqueda by viewModel.searchResults.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Comunidad HuertoHogar",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Buscador
        OutlinedTextField(
            value = query,
            onValueChange = { 
                query = it
                viewModel.buscarPersonas(it)
            },
            label = { Text("Buscar personas...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (query.isNotEmpty()) {
            Text("Resultados de búsqueda", style = MaterialTheme.typography.labelLarge)
            LazyColumn {
                items(resultadosBusqueda) { user ->
                    PersonaItem(user, isFriend = false, onAction = { viewModel.agregarAmigo(user) })
                }
            }
        } else {
            Text("Mis Amigos", style = MaterialTheme.typography.labelLarge)
            if (amigos.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("Aún no tienes amigos. ¡Busca a alguien!", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                LazyColumn {
                    items(amigos) { amigo ->
                        PersonaItem(amigo, isFriend = true, onAction = { onChatClick(amigo.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun PersonaItem(user: User, isFriend: Boolean, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar simple con inicial
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.name.firstOrNull()?.toString() ?: "?",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onAction) {
                Icon(
                    imageVector = if (isFriend) Icons.AutoMirrored.Filled.Message else Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
