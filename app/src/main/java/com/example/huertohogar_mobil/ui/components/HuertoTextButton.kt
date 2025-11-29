package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun HuertoTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = MaterialTheme.colorScheme.primary,
    textStyle: TextStyle? = null,
    icon: @Composable (() -> Unit)? = null
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = textColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.size(8.dp))
        }
        Text(
            text = text,
            style = textStyle ?: MaterialTheme.typography.labelLarge
        )
    }
}
