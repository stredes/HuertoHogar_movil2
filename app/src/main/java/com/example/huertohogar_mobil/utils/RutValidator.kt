package com.example.huertohogar_mobil.utils

object RutValidator {

    /**
     * Valida un RUT chileno con su dígito verificador
     */
    fun validarRut(rut: String): Boolean {
        val rutLimpio = limpiarRut(rut)
        if (rutLimpio.length < 2) return false

        val numero = rutLimpio.dropLast(1)
        val digitoVerificador = rutLimpio.last().uppercaseChar()

        if (!numero.all { it.isDigit() }) return false

        val digitoCalculado = calcularDigitoVerificador(numero)
        return digitoCalculado == digitoVerificador
    }

    /**
     * Calcula el dígito verificador según el algoritmo chileno
     */
    fun calcularDigitoVerificador(numero: String): Char {
        val numeroInvertido = numero.reversed()
        var suma = 0
        var multiplicador = 2

        for (digito in numeroInvertido) {
            suma += digito.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }

        val resto = 11 - (suma % 11)

        return when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> resto.toString().first()
        }
    }

    /**
     * Limpia el RUT de puntos, guiones y espacios
     */
    fun limpiarRut(rut: String): String {
        return rut.replace(".", "")
            .replace("-", "")
            .trim()
            .uppercase()
    }

    /**
     * Formatea el RUT con puntos y guión (12.345.678-9)
     */
    fun formatearRut(rut: String): String {
        val rutLimpio = limpiarRut(rut)
        if (rutLimpio.length < 2) return rut

        val numero = rutLimpio.dropLast(1)
        val dv = rutLimpio.last()

        val numeroFormateado = numero.reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()

        return "$numeroFormateado-$dv"
    }

    /**
     * Verifica si un RUT pertenece a instituciones militares o policiales
     * Los RUT institucionales en Chile suelen tener rangos específicos
     */
    fun esRutInstitucional(rut: String): Boolean {
        val rutLimpio = limpiarRut(rut)
        if (rutLimpio.length < 2) return false

        val numero = rutLimpio.dropLast(1)

        try {
            val rutNumero = numero.toLong()

            // Rangos conocidos de instituciones (aproximados):
            // Carabineros: 60.000.000 - 60.999.999
            // PDI: 61.000.000 - 61.999.999
            // Fuerzas Armadas: 50.000.000 - 50.999.999
            // Gobierno/Instituciones: 60.000.000 - 69.999.999

            return rutNumero in 50_000_000..69_999_999
        } catch (e: Exception) {
            return false
        }
    }
}

