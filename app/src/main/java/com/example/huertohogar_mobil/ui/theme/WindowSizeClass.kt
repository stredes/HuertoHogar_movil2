package com.example.huertohogar_mobil.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Enumera los tamaños de pantalla soportados
 * Basado en Material3 breakpoints estándar
 */
enum class WindowSize {
    /**
     * Pantalla pequeña (phones)
     * < 600 dp de ancho
     */
    COMPACT,

    /**
     * Pantalla mediana (tablets pequeñas, phones landscape)
     * 600 - 840 dp de ancho
     */
    MEDIUM,

    /**
     * Pantalla grande (tablets grandes)
     * > 840 dp de ancho
     */
    EXPANDED
}

/**
 * Clase que encapsula información de tamaño de pantalla
 */
data class WindowSizeClass(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val windowSize: WindowSize
) {
    val isCompact: Boolean = windowSize == WindowSize.COMPACT
    val isMedium: Boolean = windowSize == WindowSize.MEDIUM
    val isExpanded: Boolean = windowSize == WindowSize.EXPANDED
}

/**
 * CompositionLocal para acceder al WindowSizeClass en cualquier composable
 */
val LocalWindowSizeClass = staticCompositionLocalOf<WindowSizeClass> {
    error("WindowSizeClass not provided")
}

/**
 * Composable que calcula y proporciona el WindowSizeClass
 * Debe envolver la raíz de tu aplicación
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val screenHeight = config.screenHeightDp.dp

    val windowSize = when {
        screenWidth < 600.dp -> WindowSize.COMPACT
        screenWidth < 840.dp -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }

    return WindowSizeClass(
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        windowSize = windowSize
    )
}

