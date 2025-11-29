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
            painter = painterResource(id = R.drawable.leche_1l), // Reemplaza con una imagen del equipo
            contentDescription = "Equipo HuertoHogar",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        SectionHeader(
            title = "Sobre Nosotros",
            subtitle = "Somos un equipo apasionado por la comida fresca y saludable. Nuestra misión es llevar los mejores productos del campo a tu hogar, con la comodidad que te mereces."
        )
    }
}
