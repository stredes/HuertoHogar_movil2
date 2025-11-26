package com.example.huertohogar_mobil.utils

import java.text.NumberFormat
import java.util.Locale

fun formatoCLP(valor: Int): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(valor)
}