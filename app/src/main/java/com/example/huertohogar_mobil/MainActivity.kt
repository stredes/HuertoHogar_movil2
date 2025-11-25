package com.example.huertohogar_mobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.huertohogar_mobil.navigation.AppNavigation
import com.example.huertohogar_mobil.ui.theme.HuertoHogarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // <- Anotación necesaria para que Hilt funcione en esta Actividad y sus Composables
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HuertoHogarTheme {
                AppNavigation()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HuertoHogarTheme {
        AppNavigation()
    }
}