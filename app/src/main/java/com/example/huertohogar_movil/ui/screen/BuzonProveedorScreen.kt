// ...existing code...
// Quitar los imports erróneos de PedidosScreen
// ...existing code...

// Eliminar la función EstadoChip, que es la causa del stub corrupto de kapt.

// Eliminar esta definición duplicada:
// private fun formatFecha(timestamp: Long): String {
//     return java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(timestamp))
// }

// Mantener solo la definición al final del archivo.
