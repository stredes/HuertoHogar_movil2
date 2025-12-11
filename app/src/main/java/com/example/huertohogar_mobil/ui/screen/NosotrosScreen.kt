package com.example.huertohogar_mobil.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.R
import com.example.huertohogar_mobil.ui.components.SectionHeader

@Composable
fun NosotrosScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.icono), // Usamos el icono general
            contentDescription = "Equipo Red Privada",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        SectionHeader(
            title = "Sobre La Red",
            subtitle = "Somos un grupo selecto dedicado a la gestión discreta de artículos exclusivos. Nuestra prioridad es la seguridad, la privacidad y la confianza entre nuestros miembros."
        )
    }
}
