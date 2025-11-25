package com.example.huertohogar_mobil.di

import android.content.Context
import com.example.huertohogar_mobil.data.HuertoHogarDatabase
import com.example.huertohogar_mobil.data.ProductoDao
import com.example.huertohogar_mobil.data.ProductoRepository
import com.example.huertohogar_mobil.data.ProductoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HuertoHogarDatabase {
        return HuertoHogarDatabase.getDatabase(context)
    }

    @Provides
    fun provideProductoDao(database: HuertoHogarDatabase): ProductoDao {
        return database.productoDao()
    }

    // NOTA: No proveemos UsuarioDao porque de momento ningún ViewModel lo necesita.
    // Si en el futuro lo necesitas, puedes añadir un @Provides similar al de ProductoDao.
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductoRepository(impl: ProductoRepositoryImpl): ProductoRepository
}