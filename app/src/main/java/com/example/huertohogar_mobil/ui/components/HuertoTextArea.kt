package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HuertoTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 3,
    maxLines: Int = 5
) {
    HuertoTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        singleLine = false
    )
}
