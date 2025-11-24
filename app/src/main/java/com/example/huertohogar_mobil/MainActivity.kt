package com.example.huertohogar_mobil // Mantén el nombre de tu paquete actual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
// 👇 Asegúrate de importar TU navegación y TU tema.
// Si te salen en rojo, borra la línea y vuélvela a escribir para que el autocompletado encuentre la ruta correcta.
import com.example.huertohogar_mobil.navigation.AppNavigation
import com.example.huertohogar_mobil.ui.theme.HuertoHogarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Envolvemos todo en tu tema personalizado
            HuertoHogarTheme {
                // Llamamos al mapa de navegación que creamos.
                // Él decidirá si mostrar el Login o el Catálogo.
                AppNavigation()
            }
        }
    }
}