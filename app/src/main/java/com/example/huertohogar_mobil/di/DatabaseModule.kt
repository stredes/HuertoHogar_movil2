package com.example.huertohogar_mobil.di

import android.content.Context
import androidx.room.Room
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "huertohogar_db"
        )
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
    fun provideSolicitudDao(database: AppDatabase): SolicitudDao = database.solicitudDao()
}
