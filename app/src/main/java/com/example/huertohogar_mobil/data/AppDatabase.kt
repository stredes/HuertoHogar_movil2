package com.example.huertohogar_mobil.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.huertohogar_mobil.model.Cliente
import com.example.huertohogar_mobil.model.DetallePedido
import com.example.huertohogar_mobil.model.Pedido
import com.example.huertohogar_mobil.model.Producto

@Database(entities = [Producto::class, Cliente::class, Pedido::class, DetallePedido::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun clienteDao(): ClienteDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun detallePedidoDao(): DetallePedidoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huertohogar_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
