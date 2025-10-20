// M�dulo Hilt DI
package com.example.huertohogar_mobil.di

import com.example.huertohogar_mobil.data.FakeProductoRepository
import com.example.huertohogar_mobil.data.ProductoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de dependencias de HuertoHogar.
 * - Expone ProductoRepository como una instancia única (Singleton).
 * - Actualmente usa FakeProductoRepository (en memoria con drawables locales).
 *   Cuando tengas Room/API, cambia la implementación aquí y el resto de la app no se toca.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideProductoRepository(): ProductoRepository = FakeProductoRepository()
}
