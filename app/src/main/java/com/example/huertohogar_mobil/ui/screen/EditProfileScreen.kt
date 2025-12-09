package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoTextField
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.AuthViewModel

@Composable
fun EditProfileScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user

    if (user == null) {
        // Si no hay usuario, volver (no debería pasar si se navega correctamente)
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var name by remember { mutableStateOf(user.name) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = "Mi Perfil",
                canNavigateBack = true,
                onNavigateBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HuertoTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre",
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )

            HuertoTextField(
                value = user.email,
                onValueChange = { },
                label = "Email",
                enabled = false, // Email no editable por ahora
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
            )

            Text("Cambiar Contraseña (Opcional)", style = MaterialTheme.typography.titleMedium)

            HuertoTextField(
                value = password,
                onValueChange = { password = it },
                label = "Nueva Contraseña",
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
            )

            HuertoTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar Contraseña",
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
            )

            if (errorMsg != null) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
            }
            if (successMsg != null) {
                Text(successMsg!!, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.weight(1f))

            HuertoButton(
                text = "Guardar Cambios",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    errorMsg = null
                    successMsg = null

                    if (name.isBlank()) {
                        errorMsg = "El nombre no puede estar vacío"
                        return@HuertoButton
                    }

                    if (password.isNotEmpty() && password != confirmPassword) {
                        errorMsg = "Las contraseñas no coinciden"
                        return@HuertoButton
                    }

                    val finalPassword = if (password.isNotEmpty()) password else user.passwordHash
                    
                    val updatedUser = user.copy(name = name, passwordHash = finalPassword)

                    viewModel.updateUserProfile(updatedUser) { success ->
                        if (success) {
                            successMsg = "Perfil actualizado correctamente"
                        } else {
                            errorMsg = "Error al actualizar perfil"
                        }
                    }
                }
            )
        }
    }
}
