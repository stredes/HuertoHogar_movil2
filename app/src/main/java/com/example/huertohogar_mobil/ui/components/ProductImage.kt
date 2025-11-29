package com.example.huertohogar_mobil.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.huertohogar_mobil.model.Producto

@Composable
fun ProductImage(
    producto: Producto,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(modifier = modifier) {
        if (producto.imagenUri != null) {
            AsyncImage(
                model = producto.imagenUri,
                contentDescription = producto.nombre,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else if (producto.imagenRes != 0) {
            Image(
                painter = painterResource(producto.imagenRes),
                contentDescription = producto.nombre,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
