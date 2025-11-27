@file:Suppress("DEPRECATION")
package com.example.huertohogar_mobil.ui.screen

import java.text.NumberFormat
import java.util.Locale

fun formatoCLP(v: Int): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(v)
