package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoLoader
import com.example.huertohogar_mobil.ui.components.HuertoTextField
import com.example.huertohogar_mobil.ui.components.HuertoTextButton
import com.example.huertohogar_mobil.viewmodel.AuthViewModel
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import java.util.Locale

@Composable
fun IniciarSesionScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    // 👉 Callbacks separados por tipo de usuario
    onLoginRoot: () -> Unit,
    onLoginAdmin: () -> Unit,
    onLoginUser: () -> Unit,
    onIrARegistro: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navegar cuando exista usuario logueado, diferenciando por rol
    LaunchedEffect(uiState.user) {
        val user = uiState.user ?: return@LaunchedEffect

        // Asumimos que tu modelo User tiene la propiedad "role"
        val role = user.role?.lowercase(Locale.ROOT) ?: ""

        when (role) {
            "root" -> onLoginRoot()
            "admin" -> onLoginAdmin()
            else -> onLoginUser()
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            viewModel = viewModel,
            onDismiss = { showForgotPasswordDialog = false }
        )
    }

    // Scroll para que se pueda ver todo en pantallas pequeñas / con teclado abierto
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp // (lo puedes borrar si no lo usas)

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Respeta barras del sistema (notch, barra de navegación, etc.)
            .padding(WindowInsets.systemBars.asPaddingValues())
            // Ajusta contenido cuando aparece el teclado
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Limitamos el ancho máximo para que en tablets no quede gigante
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Contenedor central con ancho máximo razonable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp), // Máximo ancho para tablets
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Huerto Hogar",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                HuertoTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email"
                )

                Spacer(modifier = Modifier.height(8.dp))

                HuertoTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (uiState.isLoading) {
                    // Loader centrado en el espacio
                    HuertoLoader()
                } else {
                    // Botón ocupa ancho, no alto completo
                    HuertoButton(
                        text = "Ingresar",
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HuertoTextButton(
                        text = "¿Olvidaste tu contraseña?",
                        onClick = { showForgotPasswordDialog = true }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HuertoTextButton(
                        text = "¿No tienes cuenta? Regístrate aquí",
                        onClick = onIrARegistro
                    )
                }

                // Pequeño espacio extra para que no quede pegado al borde inferior
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    viewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: Email, 2: New Password
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (step == 1) "Recuperar Contraseña" else "Nueva Contraseña") },
        text = {
            Column {
                if (step == 1) {
                    Text("Ingresa tu email para verificar tu cuenta.")
                    Spacer(modifier = Modifier.height(8.dp))
                    HuertoTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email"
                    )
                } else {
                    Text("Ingresa tu nueva contraseña.")
                    Spacer(modifier = Modifier.height(8.dp))
                    HuertoTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "Nueva Contraseña",
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error!!, color = MaterialTheme.colorScheme.error)
                }
                if (message != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = message!!, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (step == 1) {
                        if (email.isBlank()) {
                            error = "Ingresa un email válido"
                        } else {
                            viewModel.verifyEmail(email) { exists ->
                                if (exists) {
                                    step = 2
                                    error = null
                                } else {
                                    error = "Email no encontrado"
                                }
                            }
                        }
                    } else {
                        if (newPassword.isBlank()) {
                            error = "La contraseña no puede estar vacía"
                        } else {
                            viewModel.resetPassword(email, newPassword) { success ->
                                if (success) {
                                    message = "Contraseña actualizada exitosamente"
                                    onDismiss()
                                } else {
                                    error = "Error al actualizar contraseña"
                                }
                            }
                        }
                    }
                }
            ) {
                Text(if (step == 1) "Verificar" else "Cambiar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
