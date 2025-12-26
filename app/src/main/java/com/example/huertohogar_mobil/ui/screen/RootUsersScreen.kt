package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
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
import com.example.huertohogar_mobil.ui.components.HuertoIconButton
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.ui.components.HuertoSearchField
import com.example.huertohogar_mobil.viewmodel.RootViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider

private enum class RolFiltro { TODOS, ADMIN, USUARIO }

@Composable
fun RootUsersScreen(
    viewModel: RootViewModel = hiltViewModel(),
    onNavigateEdit: (Int) -> Unit,
    onNavigateCreate: () -> Unit,
    onNavigateDashboard: () -> Unit,
    onBack: () -> Unit
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf(RolFiltro.TODOS) }

    val totalAdmins = users.count { it.role == "admin" }
    val totalUsers = users.count { it.role != "admin" && it.role != "root" }

    val usersFiltrados = users.filter { u ->
        val pasaBusqueda = query.isBlank() || u.name.contains(query, true) || u.email.contains(query, true)
        val pasaRol = when (filtro) {
            RolFiltro.TODOS -> true
            RolFiltro.ADMIN -> u.role == "admin"
            RolFiltro.USUARIO -> u.role != "admin" && u.role != "root"
        }
        pasaBusqueda && pasaRol
    }

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Gestión de Usuarios",
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateCreate,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Usuario")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Usuarios del sistema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onNavigateDashboard) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Ir al Dashboard")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dashboard")
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Card(
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total", style = MaterialTheme.typography.labelLarge)
                            Text(users.size.toString(), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Admins", style = MaterialTheme.typography.labelLarge)
                            Text(totalAdmins.toString(), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Usuarios", style = MaterialTheme.typography.labelLarge)
                            Text(totalUsers.toString(), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }

            HuertoSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Buscar por nombre o email"
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filtro == RolFiltro.TODOS,
                        onClick = { filtro = RolFiltro.TODOS },
                        label = { Text("Todos") }
                    )
                }
                item {
                    FilterChip(
                        selected = filtro == RolFiltro.ADMIN,
                        onClick = { filtro = RolFiltro.ADMIN },
                        label = { Text("Admins") }
                    )
                }
                item {
                    FilterChip(
                        selected = filtro == RolFiltro.USUARIO,
                        onClick = { filtro = RolFiltro.USUARIO },
                        label = { Text("Usuarios") }
                    )
                }
            }

            HorizontalDivider()

            if (usersFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron usuarios para este filtro.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(usersFiltrados) { user ->
                        UserItem(
                            user = user,
                            onEdit = { onNavigateEdit(user.id) },
                            onDelete = { viewModel.eliminarUsuario(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    HuertoCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Rol: ${user.role}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HuertoIconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            HuertoIconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
