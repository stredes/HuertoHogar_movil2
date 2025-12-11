package com.example.huertohogar_mobil.di

import com.example.huertohogar_mobil.data.ProductoRepository
import com.example.huertohogar_mobil.data.RoomProductoRepository
import com.example.huertohogar_mobil.data.SocialRepository
import com.example.huertohogar_mobil.data.SocialRepositoryImpl
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
    abstract fun bindProductoRepository(
        roomRepository: RoomProductoRepository
    ): ProductoRepository

    @Binds
    @Singleton
    abstract fun bindSocialRepository(
        socialRepositoryImpl: SocialRepositoryImpl
    ): SocialRepository
}
