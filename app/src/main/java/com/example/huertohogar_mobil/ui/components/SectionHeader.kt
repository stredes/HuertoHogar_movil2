package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    centered: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
