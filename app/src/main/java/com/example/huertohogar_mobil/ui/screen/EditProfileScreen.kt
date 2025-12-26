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
import com.example.huertohogar_mobil.ui.components.RutTextField
import com.example.huertohogar_mobil.utils.RutValidator
import com.example.huertohogar_mobil.viewmodel.AuthViewModel

@Composable
fun EditProfileScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user

    // Si estamos cargando, mostrar indicador en vez de salir
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (user == null) {
        // Solo salir si NO está cargando y NO hay usuario
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var name by remember { mutableStateOf(user.name) }
    var rut by remember { mutableStateOf(user.rut) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var rutError by remember { mutableStateOf<String?>(null) }

    // Solo usuarios normales requieren RUT
    val requiereRut = user.role == "user"

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

            // Solo mostrar campo RUT para usuarios normales
            if (requiereRut) {
                RutTextField(
                    value = rut,
                    onValueChange = {
                        rut = it
                        rutError = null
                    },
                    isError = rutError != null,
                    errorMessage = rutError,
                    readOnly = rut.isNotEmpty(), // Solo lectura si ya tiene RUT
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
                    rutError = null

                    if (name.isBlank()) {
                        errorMsg = "El nombre no puede estar vacío"
                        return@HuertoButton
                    }

                    // Validar RUT obligatorio solo para usuarios normales
                    if (requiereRut) {
                        if (rut.isEmpty()) {
                            rutError = "El RUT es obligatorio para continuar usando la aplicación"
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
                    }

                    if (password.isNotEmpty() && password != confirmPassword) {
                        errorMsg = "Las contraseñas no coinciden"
                        return@HuertoButton
                    }

                    val finalPassword = if (password.isNotEmpty()) password else user.passwordHash
                    
                    val updatedUser = user.copy(
                        name = name,
                        passwordHash = finalPassword,
                        rut = rut
                    )

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
