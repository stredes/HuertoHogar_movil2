package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.RutTextField
import com.example.huertohogar_mobil.utils.RutValidator
import com.example.huertohogar_mobil.viewmodel.AuthViewModel

@Composable
fun CompletarPerfilScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onPerfilCompletado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user

    if (user == null) {
        LaunchedEffect(Unit) { onPerfilCompletado() }
        return
    }

    var rut by remember { mutableStateOf("") }
    var rutError by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Advertencia",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Perfil Incompleto",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ Acción Requerida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Para continuar usando la aplicación, debes completar tu información de perfil.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔒 Tu RUT nos ayuda a:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "• Verificar que eres un usuario real",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "• Asegurar que no perteneces a instituciones militares o policiales",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "• Garantizar una única cuenta por persona",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Ingresa tu RUT",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            RutTextField(
                value = rut,
                onValueChange = {
                    rut = it
                    rutError = null
                    errorMsg = null
                },
                isError = rutError != null,
                errorMessage = rutError,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                HuertoButton(
                    text = "Completar Perfil",
                    onClick = {
                        rutError = null
                        errorMsg = null

                        if (rut.isEmpty()) {
                            rutError = "El RUT es obligatorio"
                            return@HuertoButton
                        }

                        if (!RutValidator.validarRut(rut)) {
                            rutError = "El RUT ingresado no es válido"
                            return@HuertoButton
                        }

                        if (RutValidator.esRutInstitucional(rut)) {
                            rutError = "No se permiten RUT institucionales (Militares/Policiales)"
                            return@HuertoButton
                        }

                        isLoading = true

                        val updatedUser = user.copy(rut = rut.trim().uppercase())
                        viewModel.updateUserProfile(updatedUser) { success ->
                            isLoading = false
                            if (success) {
                                onPerfilCompletado()
                            } else {
                                errorMsg = "Error al actualizar perfil. El RUT podría estar ya registrado."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No podrás usar la aplicación hasta completar esta información",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

