package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoLoader
import com.example.huertohogar_mobil.ui.components.HuertoTextField
import com.example.huertohogar_mobil.ui.components.SectionHeader
import com.example.huertohogar_mobil.viewmodel.AuthViewModel

@Composable
fun RegistrarseScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onRegistroExitoso: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            onRegistroExitoso()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader(title = "Registrarse")
        Spacer(modifier = Modifier.height(16.dp))
        
        HuertoTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre"
        )
        Spacer(modifier = Modifier.height(8.dp))
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
            // Note: Assuming HuertoTextField defaults to normal text, usually password needs visual transformation.
            // For now we keep it simple as per the component definition, 
            // or we might want to update HuertoTextField to accept visualTransformation parameter 
            // (I added it in the definition previously).
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
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
                text = "Registrarse",
                onClick = { viewModel.register(name, email, password) }
            )
        }
    }
}
