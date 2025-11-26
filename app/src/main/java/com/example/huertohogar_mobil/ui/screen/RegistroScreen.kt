package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.huertohogar_mobil.ui.components.BotonHuerto
import com.example.huertohogar_mobil.ui.components.CampoTexto
import com.example.huertohogar_mobil.ui.theme.GreenHuerto
import com.example.huertohogar_mobil.viewmodel.RegistroViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(navController: NavController) {
    val viewModel: RegistroViewModel = viewModel()
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volver") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Registro",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = GreenHuerto
            )

            Spacer(modifier = Modifier.height(32.dp))

            CampoTexto(
                valor = nombre,
                onValorCambio = { nombre = it },
                etiqueta = "Nombre Completo"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoTexto(
                valor = correo,
                onValorCambio = { correo = it },
                etiqueta = "Correo (@duocuc.cl)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoTexto(
                valor = password,
                onValorCambio = { password = it },
                etiqueta = "Contraseña",
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoTexto(
                valor = confirmarPassword,
                onValorCambio = { confirmarPassword = it },
                etiqueta = "Confirmar Contraseña",
                visualTransformation = PasswordVisualTransformation()
            )

            if (mensajeError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = mensajeError, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(24.dp))

            BotonHuerto(texto = "Registrarse") {
                if (password != confirmarPassword) {
                    mensajeError = "Las contraseñas no coinciden"
                    return@BotonHuerto
                }
                scope.launch {
                    val exito = viewModel.register(nombre, correo, password)
                    if (exito) {
                        navController.popBackStack() // Volver a Login
                    } else {
                        mensajeError = "Error en el registro. Inténtalo de nuevo."
                    }
                }
            }
        }
    }
}
