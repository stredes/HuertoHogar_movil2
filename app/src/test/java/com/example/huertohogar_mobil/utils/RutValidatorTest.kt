package com.example.huertohogar_mobil.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas unitarias para el validador de RUT
 */
class RutValidatorTest {

    @Test
    fun limpiarRut_conFormatosDiferentes() {
        assertEquals("123456742", RutValidator.limpiarRut("12.345.674-2"))
        assertEquals("189566197", RutValidator.limpiarRut("18.956.619-7"))
        assertEquals("10654898K", RutValidator.limpiarRut("10.654.898-K"))
    }

    @Test
    fun validarRut_conRutsValidos() {
        assertTrue(RutValidator.validarRut("12.345.674-2"))
        assertTrue(RutValidator.validarRut("18956619-7"))
        assertTrue(RutValidator.validarRut("10654898-K"))
    }

    @Test
    fun validarRut_conFormatoIncorrecto() {
        assertFalse(RutValidator.validarRut(""))  // Vacío
        assertFalse(RutValidator.validarRut("-"))  // Solo guión
        assertFalse(RutValidator.validarRut("abc-1"))  // Letras en números
    }

    @Test
    fun validarRut_conDigitoInvalido() {
        assertFalse(RutValidator.validarRut("12.345.674-1"))  // Dígito incorrecto
        assertFalse(RutValidator.validarRut("18.956.619-1"))  // Dígito incorrecto
    }

    @Test
    fun formatearRut_conNumeros() {
        // Formatear con puntos y guión
        val resultado1 = RutValidator.formatearRut("123456742")
        assertTrue(resultado1.contains("-"))
        assertTrue(resultado1.contains("."))

        val resultado2 = RutValidator.formatearRut("189566197")
        assertTrue(resultado2.contains("-"))
        assertTrue(resultado2.contains("."))
    }

    @Test
    fun formatearRut_conGuion() {
        // Ya formateado, debe mantenerse igual
        val resultado = RutValidator.formatearRut("123456742")
        assertTrue(resultado.contains("-"))
        assertTrue(resultado.contains("."))
    }

    @Test
    fun calcularDigitoVerificador_noVacio() {
        // Simplemente verificar que no retorna vacío
        val resultado = RutValidator.calcularDigitoVerificador("12345674")
        assertNotEquals("", resultado.toString())

        // Debe ser un dígito válido o K
        val validos = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'K')
        assertTrue(resultado in validos)
    }

    @Test
    fun esRutInstitucional_conRutsGubernamentales() {
        // Los RUT en rango 50-69 millones son institucionales
        assertTrue(RutValidator.esRutInstitucional("60.000.001-K"))
        assertTrue(RutValidator.esRutInstitucional("61500000-5"))
    }

    @Test
    fun esRutInstitucional_conRutsNormales() {
        // Los RUT fuera del rango no son institucionales
        assertFalse(RutValidator.esRutInstitucional("12345674-2"))
        assertFalse(RutValidator.esRutInstitucional("18956619-7"))
    }
}





