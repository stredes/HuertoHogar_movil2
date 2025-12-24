package com.example.huertohogar_mobil.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.huertohogar_mobil.model.*

@Database(
    entities = [
        Producto::class, 
        User::class, 
        CarritoItem::class, 
        MensajeContacto::class,
        Amistad::class,
        MensajeChat::class,
        Solicitud::class,
        Pedido::class
    ],
    version = 15,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
    abstract fun userDao(): UserDao
    abstract fun carritoDao(): CarritoDao
    abstract fun mensajeDao(): MensajeDao
    abstract fun socialDao(): SocialDao
    abstract fun pedidoDao(): PedidoDao
}
