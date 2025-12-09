package com.example.huertohogar_mobil.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoTextArea
import com.example.huertohogar_mobil.ui.components.HuertoTextField
import com.example.huertohogar_mobil.ui.components.SectionHeader
import com.example.huertohogar_mobil.viewmodel.MarketViewModel

@Composable
fun ContactoScreen(
    viewModel: MarketViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader(
            title = "Contáctanos",
            subtitle = "¿Tienes dudas? Escríbenos y te responderemos."
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        HuertoTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre"
        )
        Spacer(modifier = Modifier.height(12.dp))
        HuertoTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email"
        )
        Spacer(modifier = Modifier.height(12.dp))
        HuertoTextArea(
            value = message,
            onValueChange = { message = it },
            label = "Mensaje",
            modifier = Modifier.height(150.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        HuertoButton(
            text = "Enviar Mensaje",
            onClick = { 
                if (name.isNotBlank() && email.isNotBlank() && message.isNotBlank()) {
                    // FIX: Enviar siempre al Admin hardcoded, usando los datos del form como contenido
                    val adminEmail = "admin@huertohogar.com"
                    val fullMessage = "De: $name <$email>\n\n$message"
                    
                    viewModel.enviarContacto(name, adminEmail, fullMessage)
                    
                    Toast.makeText(context, "Mensaje enviado correctamente", Toast.LENGTH_SHORT).show()
                    // Limpiar campos
                    name = ""
                    email = ""
                    message = ""
                } else {
                    Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
