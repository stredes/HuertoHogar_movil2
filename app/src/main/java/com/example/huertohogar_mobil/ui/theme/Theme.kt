package com.example.huertohogar_mobil.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Esquema Oscuro (Usamos colores claros/pastel para contraste)
private val DarkColorScheme = darkColorScheme(
    primary = GreenHuerto,    // Usamos tu verde
    secondary = YellowHuerto, // Amarillo para destacar
    tertiary = BrownHuerto
)

// Esquema Claro (El principal de tu app)
private val LightColorScheme = lightColorScheme(
    // AQUÍ ESTABA EL ERROR: Reemplazamos Purple40 por GreenHuerto
    primary = GreenHuerto,
    secondary = BrownHuerto,
    tertiary = YellowHuerto,

    /* Otros colores por defecto que puedes descomentar si quieres afinar:
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun HuertoHogarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color (Android 12+) toma colores del fondo de pantalla del usuario.
    // Puedes ponerlo en 'false' si quieres forzar SIEMPRE tu verde corporativo.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}