package com.example.huertohogar_mobil.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.huertohogar_mobil.ui.theme.LocalWindowSizeClass
import com.example.huertohogar_mobil.ui.theme.WindowSize

/**
 * Utilidades para diseño responsivo en Jetpack Compose
 * Proporciona funciones para obtener dimensiones adaptables según el tamaño de pantalla
 */
object ResponsiveUtils {

    /**
     * Obtiene el padding horizontal recomendado según el tamaño de pantalla
     * - Compact (phone): 16.dp
     * - Medium (tablet pequeña): 24.dp
     * - Expanded (tablet grande): 32.dp
     */
    @Composable
    fun getHorizontalPadding(): Dp {
        val windowSize = LocalWindowSizeClass.current.windowSize
        return when (windowSize) {
            WindowSize.COMPACT -> 16.dp
            WindowSize.MEDIUM -> 24.dp
            WindowSize.EXPANDED -> 32.dp
        }
    }

    /**
     * Obtiene el padding vertical recomendado según el tamaño de pantalla
     * - Compact: 12.dp
     * - Medium: 16.dp
     * - Expanded: 20.dp
     */
    @Composable
    fun getVerticalPadding(): Dp {
        val windowSize = LocalWindowSizeClass.current.windowSize
        return when (windowSize) {
            WindowSize.COMPACT -> 12.dp
            WindowSize.MEDIUM -> 16.dp
            WindowSize.EXPANDED -> 20.dp
        }
    }

    /**
     * Obtiene el ancho máximo recomendado para contenedores principales
     * - Compact: maxWidth 100% (sin límite)
     * - Medium: 600.dp (tablet)
     * - Expanded: 900.dp (tablet grande)
     */
    @Composable
    fun getMaxWidth(): Dp? {
        val windowSize = LocalWindowSizeClass.current.windowSize
        return when (windowSize) {
            WindowSize.COMPACT -> null // Sin límite
            WindowSize.MEDIUM -> 600.dp
            WindowSize.EXPANDED -> 900.dp
        }
    }

    /**
     * Obtiene el espaciado entre elementos según el tamaño de pantalla
     * - Compact: 8.dp
     * - Medium: 12.dp
     * - Expanded: 16.dp
     */
    @Composable
    fun getElementSpacing(): Dp {
        val windowSize = LocalWindowSizeClass.current.windowSize
        return when (windowSize) {
            WindowSize.COMPACT -> 8.dp
            WindowSize.MEDIUM -> 12.dp
            WindowSize.EXPANDED -> 16.dp
        }
    }

    /**
     * Obtiene el tamaño de fuente para títulos según el tamaño de pantalla
     * - Compact: 28.sp
     * - Medium: 32.sp
     * - Expanded: 36.sp
     */
    @Composable
    fun getTitleFontSize(): androidx.compose.ui.unit.TextUnit {
        val windowSize = LocalWindowSizeClass.current.windowSize
        return when (windowSize) {
            WindowSize.COMPACT -> 28.sp
            WindowSize.MEDIUM -> 32.sp
            WindowSize.EXPANDED -> 36.sp
        }
    }

    /**
     * Obtiene el tamaño de fuente para cuerpo según el tamaño de pantalla
     * - Compact: 14.sp
     * - Medium: 16.sp
     * - Expanded: 18.sp
     */
    @Composable
    fun getBodyFontSize(): androidx.compose.ui.unit.TextUnit {
        val windowSize = LocalWindowSizeClass.current.windowSize
        return when (windowSize) {
            WindowSize.COMPACT -> 14.sp
            WindowSize.MEDIUM -> 16.sp
            WindowSize.EXPANDED -> 18.sp
        }
    }

    /**
     * Obtiene la altura mínima para botones según el tamaño de pantalla
     * - Compact: 48.dp
     * - Medium: 52.dp
     * - Expanded: 56.dp
     */
    @Composable
    fun getButtonHeight(): Dp {
        val windowSize = LocalWindowSizeClass.current.windowSize
        return when (windowSize) {
            WindowSize.COMPACT -> 48.dp
            WindowSize.MEDIUM -> 52.dp
            WindowSize.EXPANDED -> 56.dp
        }
    }

    /**
     * Obtiene el número de columnas para grillas según el tamaño de pantalla
     * - Compact: 1 columna (lista vertical)
     * - Medium: 2 columnas
     * - Expanded: 3 columnas
     */
    @Composable
    fun getGridColumns(): Int {
        val windowSize = LocalWindowSizeClass.current.windowSize
        return when (windowSize) {
            WindowSize.COMPACT -> 1
            WindowSize.MEDIUM -> 2
            WindowSize.EXPANDED -> 3
        }
    }

    /**
     * Verifica si es una pantalla pequeña (phone)
     */
    @Composable
    fun isCompactScreen(): Boolean = LocalWindowSizeClass.current.isCompact

    /**
     * Verifica si es una pantalla mediana (tablet pequeña)
     */
    @Composable
    fun isMediumScreen(): Boolean = LocalWindowSizeClass.current.isMedium

    /**
     * Verifica si es una pantalla grande (tablet)
     */
    @Composable
    fun isExpandedScreen(): Boolean = LocalWindowSizeClass.current.isExpanded
}

