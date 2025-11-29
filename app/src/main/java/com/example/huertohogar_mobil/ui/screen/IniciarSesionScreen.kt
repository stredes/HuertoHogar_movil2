package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoLoader
import com.example.huertohogar_mobil.ui.components.HuertoTextField
import com.example.huertohogar_mobil.viewmodel.AuthViewModel

@Composable
fun IniciarSesionScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginExitoso: () -> Unit = {},
    onIrARegistro: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            onLoginExitoso()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Huerto Hogar", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
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
            Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (uiState.isLoading) {
            HuertoLoader()
        } else {
            HuertoButton(
                text = "Ingresar",
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxSize(0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onIrARegistro) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }
        }
    }
}
