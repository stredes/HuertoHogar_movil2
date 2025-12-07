package com.example.huertohogar_mobil.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.huertohogar_mobil.model.*

@Database(
    entities = [
        Producto::class, 
        User::class, 
        CarritoItem::class, 
        MensajeContacto::class,
        Amistad::class,
        MensajeChat::class,
        Solicitud::class
    ], 
    version = 8, 
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
    abstract fun userDao(): UserDao
    abstract fun carritoDao(): CarritoDao
    abstract fun mensajeDao(): MensajeDao
    abstract fun socialDao(): SocialDao
    abstract fun solicitudDao(): SolicitudDao
}
