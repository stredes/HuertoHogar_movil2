package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.huertohogar_mobil.model.Producto

@Composable
fun ProductImage(
    producto: Producto,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(modifier = modifier) {
        val context = LocalContext.current
        
        // Determinar el modelo para Coil (URI o Resource ID)
        val data = if (!producto.imagenUri.isNullOrEmpty()) {
            producto.imagenUri
        } else {
            if (producto.imagenRes != 0) producto.imagenRes else android.R.drawable.ic_menu_gallery
        }
        
        val request = ImageRequest.Builder(context)
            .data(data)
            .crossfade(true)
            .build()

        AsyncImage(
            model = request,
            contentDescription = producto.nombre,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(android.R.drawable.ic_menu_report_image),
            placeholder = painterResource(android.R.drawable.ic_menu_gallery)
        )
    }
}
