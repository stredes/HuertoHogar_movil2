package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.utils.RutValidator

@Composable
fun RutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "RUT",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    var textoFormateado by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (value != RutValidator.limpiarRut(textoFormateado)) {
            textoFormateado = if (value.length >= 2) {
                RutValidator.formatearRut(value)
            } else {
                value
            }
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = textoFormateado,
            onValueChange = { nuevoValor ->
                val limpio = RutValidator.limpiarRut(nuevoValor)
                if (limpio.length <= 9 && limpio.all { it.isDigit() || it == 'K' }) {
                    textoFormateado = if (limpio.length >= 2) {
                        RutValidator.formatearRut(limpio)
                    } else {
                        limpio
                    }
                    onValueChange(limpio)
                }
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            placeholder = { Text("12.345.678-9") }
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        if (!isError && textoFormateado.isNotEmpty() && enabled && !readOnly) {
            val esValido = RutValidator.validarRut(textoFormateado)
            val esInstitucional = if (esValido) RutValidator.esRutInstitucional(textoFormateado) else false

            when {
                esInstitucional -> {
                    Text(
                        text = "⚠️ RUT institucional detectado (Militar/Policial)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                esValido -> {
                    Text(
                        text = "✓ RUT válido",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                else -> {
                    Text(
                        text = "✗ RUT inválido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}

