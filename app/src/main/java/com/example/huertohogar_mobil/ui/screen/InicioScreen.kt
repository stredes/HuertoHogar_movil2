package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.R
import com.example.huertohogar_mobil.ui.components.HuertoButton
import com.example.huertohogar_mobil.ui.components.HuertoOutlinedButton

@Composable
fun InicioScreen(
    onNavigateToProductos: () -> Unit,
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.icono),
            contentDescription = "Logo HuertoHogar",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "¡Bienvenido a HuertoHogar!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "En HuertoHogar, creemos en la importancia de una alimentación sana y sostenible. Cultivamos nuestros productos con amor y dedicación, para que puedas disfrutar de la frescura del campo en la comodidad de tu hogar.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        HuertoButton(
            text = "Ver Productos",
            onClick = onNavigateToProductos
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HuertoOutlinedButton(
            text = "Cerrar Sesión",
            onClick = onLogout,
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) }
        )
    }
}
