package com.example.huertohogar_mobil.di

import com.example.huertohogar_mobil.data.PedidoRepository
import com.example.huertohogar_mobil.data.ProductoRepository
import com.example.huertohogar_mobil.data.SocialRepository
import com.example.huertohogar_mobil.data.remote.RemotePedidoRepository
import com.example.huertohogar_mobil.data.remote.RemoteProductoRepository
import com.example.huertohogar_mobil.data.remote.RemoteSocialRepository
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
        remoteRepository: RemoteProductoRepository
    ): ProductoRepository

    @Binds
    @Singleton
    abstract fun bindSocialRepository(
        remoteRepository: RemoteSocialRepository
    ): SocialRepository

    @Binds
    @Singleton
    abstract fun bindPedidoRepository(
        remoteRepository: RemotePedidoRepository
    ): PedidoRepository

    // TODO: bind remote repositories when ready (Auth/Producto/Pedido/Cart/Messages/Social/Contact)
}
