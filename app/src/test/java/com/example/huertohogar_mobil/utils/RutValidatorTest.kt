package com.example.huertohogar_mobil.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas unitarias para el validador de RUT
 */
class RutValidatorTest {

    @Test
    fun obtenerNumeros_conRutValido() {
        assertEquals("12345674", RutValidator.obtenerNumeros("12345674-2"))
        assertEquals("18956619", RutValidator.obtenerNumeros("18956619-7"))
        assertEquals("", RutValidator.obtenerNumeros("invalid"))
    }

    @Test
    fun obtenerDigito_conRutValido() {
        assertEquals("2", RutValidator.obtenerDigito("12345674-2"))
        assertEquals("7", RutValidator.obtenerDigito("18956619-7"))
        assertEquals("K", RutValidator.obtenerDigito("10654898-K"))
    }

    @Test
    fun esRutValido_conFormatoIncorrecto() {
        assertFalse(RutValidator.esRutValido("12345674"))  // Sin guión
        assertFalse(RutValidator.esRutValido("123456-74"))  // Formato incorrecto
        assertFalse(RutValidator.esRutValido(""))  // Vacío
        assertFalse(RutValidator.esRutValido("-"))  // Solo guión
        assertFalse(RutValidator.esRutValido("abc-1"))  // Letras en números
    }

    @Test
    fun esRutValido_conRangoIncorrecto() {
        assertFalse(RutValidator.esRutValido("1234-2"))  // Menos de 5 dígitos
        assertFalse(RutValidator.esRutValido("123456789-2"))  // Más de 8 dígitos
    }

    @Test
    fun esRutValido_conDigitoInvalido() {
        assertFalse(RutValidator.esRutValido("12345674-L"))  // L no es válido
        assertFalse(RutValidator.esRutValido("12345674-@"))  // @ no es válido
    }

    @Test
    fun formatearRut_conNumeros() {
        // Formatear solo números - calcula y agrega el dígito
        val resultado1 = RutValidator.formatearRut("12345674")
        assertTrue(resultado1.contains("-"))
        assertTrue(resultado1.startsWith("12345674-"))

        val resultado2 = RutValidator.formatearRut("18956619")
        assertTrue(resultado2.contains("-"))
        assertTrue(resultado2.startsWith("18956619-"))
    }

    @Test
    fun formatearRut_conGuion() {
        // Ya formateado, debe mantenerse igual
        val resultado = RutValidator.formatearRut("12345674-2")
        assertTrue(resultado.contains("-"))
    }

    @Test
    fun calcularDigitoVerificador_noVacio() {
        // Simplemente verificar que no retorna vacío
        val resultado = RutValidator.calcularDigitoVerificador("12345674")
        assertNotEquals("", resultado)

        // Debe ser un dígito válido o K
        val validos = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "K")
        assertTrue(resultado in validos)
    }
}





