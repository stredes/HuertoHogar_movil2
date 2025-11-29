package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HuertoSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Buscar...",
    modifier: Modifier = Modifier
) {
    HuertoTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = placeholder,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        singleLine = true
    )
}
