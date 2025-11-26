package com.example.huertohogar_mobil.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.viewmodel.MarketViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarProductoScreen(
    viewModel: MarketViewModel = hiltViewModel(),
    productoEditar: Producto? = null, // Si es null, es modo CREAR
    onBack: () -> Unit
) {
    // Inicializamos estados. Si hay productoEditar, usamos sus valores.
    var nombre by remember { mutableStateOf(productoEditar?.nombre ?: "") }
    var precio by remember { mutableStateOf(productoEditar?.precioCLP?.toString() ?: "") }
    var unidad by remember { mutableStateOf(productoEditar?.unidad ?: "") }
    var descripcion by remember { mutableStateOf(productoEditar?.descripcion ?: "") }
    
    // Para la imagen, inicializamos con la URI guardada si existe
    var imagenUri by remember { 
        mutableStateOf<Uri?>(productoEditar?.imagenUri?.let { Uri.parse(it) }) 
    }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val esEdicion = productoEditar != null

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imagenUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Producto" else "Agregar Producto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Lógica de visualización: URI nueva > URI existente > Resource existente
                if (imagenUri != null) {
                    AsyncImage(
                        model = imagenUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    ) {
                        Text("Cambiar")
                    }
                } else if (productoEditar != null && productoEditar.imagenRes != 0) {
                    // Mostrar imagen de recurso si estamos editando y no hay URI
                    AsyncImage(
                        model = productoEditar.imagenRes,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                     Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    ) {
                        Text("Cambiar")
                    }
                } else {
                    Button(onClick = { launcher.launch("image/*") }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Seleccionar Imagen")
                    }
                }
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Producto") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = precio,
                    onValueChange = { if (it.all { char -> char.isDigit() }) precio = it },
                    label = { Text("Precio (CLP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = unidad,
                    onValueChange = { unidad = it },
                    label = { Text("Unidad (kg, unid)") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    if (nombre.isNotEmpty() && precio.isNotEmpty() && unidad.isNotEmpty()) {
                        scope.launch {
                            // Solo copiamos si la URI cambió y no es null
                            // Si es la misma que ya teníamos guardada (String), no necesitamos copiarla de nuevo
                            // Pero como manejamos Uri objeto, comparamos:
                            val uriStringOriginal = productoEditar?.imagenUri
                            val uriActual = imagenUri
                            
                            var uriFinal: String? = uriStringOriginal

                            // Si hay una nueva URI seleccionada que es distinta a la original
                            if (uriActual != null && uriActual.toString() != uriStringOriginal) {
                                uriFinal = copiarImagenAMemoriaInterna(context, uriActual)
                            }

                            if (esEdicion) {
                                viewModel.editarProducto(
                                    id = productoEditar!!.id,
                                    nombre = nombre,
                                    precio = precio.toIntOrNull() ?: 0,
                                    unidad = unidad,
                                    desc = descripcion,
                                    uri = uriFinal,
                                    originalImgRes = productoEditar.imagenRes
                                )
                            } else {
                                viewModel.crearProducto(
                                    nombre = nombre,
                                    precio = precio.toIntOrNull() ?: 0,
                                    unidad = unidad,
                                    desc = descripcion,
                                    uri = uriFinal
                                )
                            }
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nombre.isNotEmpty() && precio.isNotEmpty()
            ) {
                Text(if (esEdicion) "Guardar Cambios" else "Guardar Producto")
            }
        }
    }
}

// Función auxiliar para copiar la imagen
suspend fun copiarImagenAMemoriaInterna(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val archivoDestino = File(context.filesDir, "img_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(archivoDestino)
            
            inputStream.copyTo(outputStream)
            
            inputStream.close()
            outputStream.close()
            
            archivoDestino.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
