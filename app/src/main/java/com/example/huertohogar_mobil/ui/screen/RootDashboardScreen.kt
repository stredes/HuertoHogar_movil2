package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.RootViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

@Composable
fun RootDashboardScreen(
    viewModel: RootViewModel = hiltViewModel(),
    onNavigateCreate: () -> Unit,
    onNavigateUsers: () -> Unit,
    onNavigateProducts: () -> Unit,
    onLogout: () -> Unit,
    onNavigateProviders: () -> Unit
) {
    val userCount by viewModel.userCount.collectAsStateWithLifecycle()
    val productCount by viewModel.productCount.collectAsStateWithLifecycle()
    val history by viewModel.syncHistory.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncError by viewModel.syncError.collectAsStateWithLifecycle()
    val lastCloudSync by viewModel.lastCloudSync.collectAsStateWithLifecycle()
    val lastLocalSync by viewModel.lastLocalSync.collectAsStateWithLifecycle()
    val autoSyncCloud by viewModel.autoSyncCloud.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val activityListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Mostrar Snackbar con el último evento o error
    val latest = history.firstOrNull()
    LaunchedEffect(latest, syncError) {
        when {
            syncError != null -> snackbarHostState.showSnackbar("Error: ${'$'}{syncError}")
            latest != null -> snackbarHostState.showSnackbar(latest)
        }
    }

    // Mantener la lista anclada arriba cuando hay nuevos eventos
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            scope.launch { activityListState.scrollToItem(0) }
        }
    }

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Dashboard Root",
                canNavigateBack = false
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Estadísticas", style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider()
                            Text(text = "Usuarios: ${userCount}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Productos: ${productCount}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Última sync nube: ${lastCloudSync ?: "Sincronizando..."}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Última sync local: ${lastLocalSync ?: "Esperando peers..."}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "Actividad reciente", style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                if (history.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = "Cargando...", color = MaterialTheme.colorScheme.secondary)
                                    }
                                } else {
                                    LazyColumn(
                                        state = activityListState,
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(history.take(30)) { entry ->
                                            Text(text = entry, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Gestión", style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider()
                            HuertoButton(text = "Gestionar Usuarios", onClick = onNavigateUsers, modifier = Modifier.fillMaxWidth())
                            HuertoButton(text = "Gestionar Productos", onClick = onNavigateProducts, modifier = Modifier.fillMaxWidth())
                            HuertoButton(text = "Ver Proveedores", onClick = onNavigateProviders, modifier = Modifier.fillMaxWidth())
                            HuertoButton(text = "Crear Nuevo Usuario", onClick = onNavigateCreate, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Sincronización", style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider()
                            HuertoButton(
                                text = "Sincronizar con Red",
                                onClick = { viewModel.sincronizarDatosConAdmins() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            HuertoButton(
                                text = "Sincronizar con Nube",
                                onClick = { viewModel.sincronizarSoloCloud() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Switch(
                                    checked = autoSyncCloud,
                                    onCheckedChange = { viewModel.setAutoSyncCloud(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                                )
                                Column {
                                    Text("Auto-sync nube", style = MaterialTheme.typography.bodyMedium)
                                    Text("Ejecuta sync nube automática", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                            HuertoButton(text = "Cerrar Sesión", onClick = onLogout, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                // Spacer final para padding inferior
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            if (isSyncing) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Card(shape = RoundedCornerShape(50), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Sincronizando...")
                        }
                    }
                }
            }
        }
    }
}
