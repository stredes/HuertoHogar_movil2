package com.example.huertohogar_mobil.di

import android.content.Context
import androidx.room.Room
import com.example.huertohogar_mobil.data.AppDatabase
import com.example.huertohogar_mobil.data.ProductoDao
import com.example.huertohogar_mobil.data.UserDao
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
        ).fallbackToDestructiveMigration() // Útil para desarrollo
         .build()
    }

    @Provides
    fun provideProductoDao(database: AppDatabase): ProductoDao {
        return database.productoDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
