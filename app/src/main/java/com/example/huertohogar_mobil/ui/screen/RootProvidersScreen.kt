package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.ui.components.HuertoCard
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.ui.components.HuertoSearchField
import com.example.huertohogar_mobil.viewmodel.RootViewModel

@Composable
fun RootProvidersScreen(
    viewModel: RootViewModel = hiltViewModel(),
    onProviderClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val admins by viewModel.admins.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    val providers = admins.filter { it.role != "root" && it.email != "root" }
        .filter { p -> query.isBlank() || p.name.contains(query, true) || p.email.contains(query, true) }
    val total = providers.size

    Scaffold(
        topBar = {
            HuertoTopBar(title = "Proveedores (Admins)", canNavigateBack = true, onNavigateBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Proveedores", style = MaterialTheme.typography.labelLarge)
                        Text(total.toString(), style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }

            HuertoSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Buscar por nombre o email"
            )

            HorizontalDivider()

            if (providers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay proveedores registrados.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(providers) { provider ->
                        ProviderItem(provider = provider, onClick = { onProviderClick(provider.email) })
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderItem(provider: User, onClick: () -> Unit) {
    HuertoCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = provider.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = provider.email, style = MaterialTheme.typography.bodyMedium)
                Text(text = "Rol: ${provider.role}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Ver Productos")
        }
    }
}
