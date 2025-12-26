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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "huertohogar_db"
        )
        // Agregar la migración para no perder datos
        .addMigrations(MIGRATION_15_16)
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
