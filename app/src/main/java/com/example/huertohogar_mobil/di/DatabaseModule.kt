package com.example.huertohogar_mobil.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.huertohogar_mobil.data.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Migración de versión 15 a 16: Agregar campo RUT a tabla users
    private val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar columna rut con valor por defecto vacío
            database.execSQL("ALTER TABLE users ADD COLUMN rut TEXT NOT NULL DEFAULT ''")
        }
    }

    // Migración de versión 16 a 17: Agregar índices a tablas Amistad y MensajeChat
    private val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Agregar índices a tabla amistades
            database.execSQL("CREATE INDEX IF NOT EXISTS index_amistades_usuarioId ON amistades(usuarioId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_amistades_amigoId ON amistades(amigoId)")

            // Agregar índices a tabla mensajes_chat
            database.execSQL("CREATE INDEX IF NOT EXISTS index_mensajes_chat_remitenteId ON mensajes_chat(remitenteId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_mensajes_chat_destinatarioId ON mensajes_chat(destinatarioId)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_mensajes_chat_remitenteId_destinatarioId_timestamp_contenido ON mensajes_chat(remitenteId, destinatarioId, timestamp, contenido)")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "huertohogar_db"
        )
        // Agregar las migraciones para no perder datos
        .addMigrations(MIGRATION_15_16, MIGRATION_16_17)
        // Permite la reconstrucción destructiva de la DB al cambiar la versión.
        // Esto es necesario para aplicar el índice único en MensajeChat
        // y solucionar el problema de duplicación de una vez por todas.
        .fallbackToDestructiveMigration() 
        .build()
    }

    @Provides
    fun provideProductoDao(database: AppDatabase): ProductoDao = database.productoDao()

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideCarritoDao(database: AppDatabase): CarritoDao = database.carritoDao()
    
    @Provides
    fun provideMensajeDao(database: AppDatabase): MensajeDao = database.mensajeDao()

    @Provides
    fun provideSocialDao(database: AppDatabase): SocialDao = database.socialDao()

    @Provides
    fun providePedidoDao(database: AppDatabase): PedidoDao = database.pedidoDao()
}
