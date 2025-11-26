package com.example.huertohogar_mobil.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = Color(0xFF002200), // Verde muy oscuro para texto sobre container
    
    secondary = EarthSecondary,
    onSecondary = EarthOnSecondary,
    secondaryContainer = EarthSecondaryContainer,
    
    tertiary = SunTertiary,
    tertiaryContainer = SunTertiaryContainer,
    
    background = CreamBackground,
    surface = SurfaceWhite,
    surfaceVariant = SurfaceVariant, // Color de fondo de cards por defecto
    onSurface = TextBlack,
    onBackground = TextBlack,
    
    error = Color(0xFFBA1A1A)
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = Color(0xFF00390A),
    primaryContainer = Color(0xFF005313),
    
    secondary = EarthSecondaryDark,
    onSecondary = Color(0xFF5F1500),
    
    background = BackgroundDark,
    surface = SurfaceDark,
    onSurface = Color(0xFFE2E2E2),
    onBackground = Color(0xFFE2E2E2)
)

@Composable
fun HuertoHogarMobilTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography, // Aseguramos que use nuestra tipografía
        content = content
    )
}
