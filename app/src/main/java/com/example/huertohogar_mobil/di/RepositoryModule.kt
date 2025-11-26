package com.example.huertohogar_mobil.di

import com.example.huertohogar_mobil.data.FirebaseProductsRepository
import com.example.huertohogar_mobil.data.ProductsRepository
import com.example.huertohogar_mobil.data.ProductoRepository
import com.example.huertohogar_mobil.data.RoomProductoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductsRepository(
        firebaseProductsRepository: FirebaseProductsRepository
    ): ProductsRepository

    @Binds
    @Singleton
    abstract fun bindProductoRepository(
        roomProductoRepository: RoomProductoRepository
    ): ProductoRepository
}
