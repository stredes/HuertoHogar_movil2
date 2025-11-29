package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HuertoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    }
}
