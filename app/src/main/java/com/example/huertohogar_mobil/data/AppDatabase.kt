package com.example.huertohogar_mobil.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.model.User

@Database(entities = [Producto::class, User::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
    abstract fun userDao(): UserDao
}
