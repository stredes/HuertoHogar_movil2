package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoTextField
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.RootViewModel

@Composable
fun CreateAdminScreen(
    viewModel: RootViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var rootPass by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            HuertoTopBar(title = "Nuevo Admin", canNavigateBack = true, onNavigateBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Datos del Administrador", style = MaterialTheme.typography.titleMedium)
            
            HuertoTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre de la Empresa/Admin",
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )
            
            HuertoTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo Admin (@empresa.com)",
                leadingIcon = { Icon(Icons.Default.Person, null) } // Reusamos icono
            )

            HuertoTextField(
                value = pass,
                onValueChange = { pass = it },
                label = "Contraseña Temporal",
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, null) }
            )

            HorizontalDivider()

            Text("Autorización Root", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text("Ingresa tu clave root para confirmar:", style = MaterialTheme.typography.bodySmall)

            HuertoTextField(
                value = rootPass,
                onValueChange = { rootPass = it },
                label = "Clave Root",
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, null) }
            )

            if (errorMsg != null) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            HuertoButton(
                text = "Crear Administrador",
                onClick = {
                    if (viewModel.validarRoot(rootPass)) {
                        if (nombre.isNotBlank() && email.isNotBlank() && pass.isNotBlank()) {
                            viewModel.crearAdmin(
                                nombre, email, pass,
                                onSuccess = { onBack() },
                                onError = { errorMsg = "El correo ya existe." }
                            )
                        } else {
                            errorMsg = "Completa todos los campos."
                        }
                    } else {
                        errorMsg = "Clave Root incorrecta."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
