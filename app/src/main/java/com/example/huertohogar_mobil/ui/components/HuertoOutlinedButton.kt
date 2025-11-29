package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HuertoOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        if (icon != null) {
            icon()
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        }
        Text(text = text)
    }
}

import androidx.compose.foundation.layout.size
