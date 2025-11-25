package com.example.huertohogar_mobil.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.JournalMode
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.huertohogar_mobil.model.Producto
import com.example.huertohogar_mobil.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Usuario::class, Producto::class], version = 1, exportSchema = false)
abstract class HuertoHogarDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun productoDao(): ProductoDao

    companion object {
        @Volatile
        private var database: HuertoHogarDatabase? = null

        fun getDatabase(context: Context): HuertoHogarDatabase {
            if (com.example.huertohogar_mobil.data.HuertoHogarDatabase.Companion.database == null) {
                com.example.huertohogar_mobil.data.HuertoHogarDatabase.Companion.database = Room.databaseBuilder(
                    context,
                    HuertoHogarDatabase::class.java,
                    "pokestore.db"
                )
                    .fallbackToDestructiveMigration()   // Eliminar la base de datos al cambiar la version
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch{
                                repopulateDb(com.example.huertohogar_mobil.data.HuertoHogarDatabase.Companion.database!!)
                            }
                        }
                    })
                    .setJournalMode(JournalMode.TRUNCATE)
                    .build()
            }
            return com.example.huertohogar_mobil.data.HuertoHogarDatabase.Companion.database!!
        }

        private suspend fun repopulateDb(db: HuertoHogarDatabase) {
            val productoDao = db.productoDao()
            val usuarioDao = db.usuarioDao()

            // 1. Usuario de prueba
            usuarioDao.insertar(Usuario(nombre = "Cliente Demo", correo = "usuario@duocuc.cl", contrasena = "1234"))

            // 2. Productos (Restaurado: volvemos a usar 'code'. El 'id' se autogenera)
            val productos = listOf(
                Producto(code="FR001", nombre="Manzanas Fuji", precio=1200, unidad="kg", stock=150, imagen="manzana", descripcion="Frescas", origen="Maule", categoria="Frutas Frescas"),
                Producto(code="FR002", nombre="Naranjas Valencia", precio=1000, unidad="kg", stock=200, imagen="naranja", descripcion="Jugosas", origen="Coquimbo", categoria="Frutas Frescas"),
                Producto(code="FR003", nombre="Plátanos Cavendish", precio=800, unidad="kg", stock=250, imagen="platanos_cavendish", descripcion="Energéticos", origen="Los Ríos", categoria="Frutas Frescas"),
                Producto(code="VR001", nombre="Zanahorias Orgánicas", precio=900, unidad="kg", stock=100, imagen="zanahoria", descripcion="Sin pesticidas", origen="Metropolitana", categoria="Verduras Orgánicas"),
                Producto(code="VR002", nombre="Espinacas Frescas", precio=700, unidad="500 g", stock=80, imagen="espinacas", descripcion="Ricas en hierro", origen="O'Higgins", categoria="Verduras Orgánicas"),
                Producto(code="VR003", nombre="Pimientos Tricolores", precio=1500, unidad="kg", stock=120, imagen="pimientos_tricolores", descripcion="Para salteados", origen="Biobío", categoria="Verduras Orgánicas"),
                Producto(code="PO001", nombre="Miel Orgánica", precio=5000, unidad="frasco", stock=50, imagen="miel", descripcion="Pura", origen="Los Lagos", categoria="Productos Orgánicos"),
                Producto(code="PL001", nombre="Leche Entera", precio=1200, unidad="1 L", stock=90, imagen="leche_1l", descripcion="Fuente de calcio", origen="Araucanía", categoria="Productos Lácteos")
            )
            productos.forEach { productoDao.insertar(it) }
        }
    }
}