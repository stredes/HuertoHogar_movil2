package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.utils.ResponsiveUtils

@Composable
fun HuertoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fullWidth: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    // Altura responsiva según tamaño de pantalla
    val buttonHeight = ResponsiveUtils.getButtonHeight()

    val finalModifier = if (fullWidth) {
        modifier.fillMaxWidth().height(buttonHeight)
    } else {
        modifier.height(buttonHeight)
    }

    Button(
        onClick = onClick,
        modifier = finalModifier,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.size(8.dp))
        }
        Text(text = text)
    }
}
