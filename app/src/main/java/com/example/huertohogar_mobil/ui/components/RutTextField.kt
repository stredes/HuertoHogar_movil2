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
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { nuevoValor ->
                // ✅ SIMPLE: Solo aceptar números, sin formateo en tiempo real
                val soloNumeros = nuevoValor.filter { it.isDigit() }

                // Limitar a 9 caracteres máximo (8 dígitos + 1 dígito verificador)
                if (soloNumeros.length <= 9) {
                    onValueChange(soloNumeros)
                }
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(
                // ✅ SOLO TECLADO NUMÉRICO
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            placeholder = { Text("189566197") }
        )

        // 💡 Mostrar ayuda visual de formateo (sin alterar la entrada)
        if (value.isNotEmpty()) {
            val rutFormateado = RutValidator.formatearRutDisplay(value)
            Text(
                text = "Formato: $rutFormateado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        // ✅ Validación visual (solo si el RUT tiene contenido)
        if (!isError && value.isNotEmpty() && enabled && !readOnly) {
            val esValido = RutValidator.validarRut(value)
            val esInstitucional = if (esValido) RutValidator.esRutInstitucional(value) else false

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
                value.length >= 8 -> {
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



