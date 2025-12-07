package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoTextField
import com.example.huertohogar_mobil.ui.components.HuertoTopBar
import com.example.huertohogar_mobil.viewmodel.RootViewModel

@Composable
fun EditUserScreen(
    userId: Int?, // null para crear nuevo
    viewModel: RootViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    
    // Si userId existe, buscamos el usuario a editar
    val existingUser = remember(users, userId) { 
        if (userId != null) users.find { it.id == userId } else null 
    }

    var nombre by remember(existingUser) { mutableStateOf(existingUser?.name ?: "") }
    var email by remember(existingUser) { mutableStateOf(existingUser?.email ?: "") }
    var password by remember { mutableStateOf("") } // Solo si se quiere cambiar/crear
    var role by remember(existingUser) { mutableStateOf(existingUser?.role ?: "user") }
    var expandedRole by remember { mutableStateOf(false) }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            HuertoTopBar(
                title = if (userId == null) "Crear Usuario" else "Editar Usuario",
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
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre",
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) }
            )
            
            HuertoTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                enabled = userId == null // El email es clave única conceptual, mejor no cambiarlo en edición simple
            )
            
            HuertoTextField(
                value = password,
                onValueChange = { password = it },
                label = if (userId == null) "Contraseña" else "Nueva Contraseña (Opcional)",
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) }
            )
            
            // Selector de Rol
            Box {
                OutlinedTextField(
                    value = role,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol") },
                    trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().clickable { expandedRole = true },
                    enabled = false, // Hacemos que el click lo maneje el Box o Modifier
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                // Overlay click para abrir menu
                Box(modifier = Modifier.matchParentSize().clickable { expandedRole = true })

                DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                    listOf("user", "admin").forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r) },
                            onClick = {
                                role = r
                                expandedRole = false
                            }
                        )
                    }
                }
            }

            if (errorMsg != null) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            HuertoButton(
                text = "Guardar",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (nombre.isBlank() || email.isBlank()) {
                        errorMsg = "Nombre y Email son obligatorios"
                        return@HuertoButton
                    }
                    if (userId == null && password.isBlank()) {
                        errorMsg = "Contraseña obligatoria para nuevo usuario"
                        return@HuertoButton
                    }
                    
                    val finalPass = if (password.isNotBlank()) password else (existingUser?.passwordHash ?: "")
                    
                    val userToSave = User(
                        id = userId ?: 0,
                        name = nombre,
                        email = email,
                        passwordHash = finalPass,
                        role = role
                    )
                    
                    viewModel.guardarUsuario(
                        user = userToSave,
                        isNew = userId == null,
                        onSuccess = { onBack() },
                        onError = { errorMsg = "Error al guardar (email duplicado?)" }
                    )
                }
            )
        }
    }
}
