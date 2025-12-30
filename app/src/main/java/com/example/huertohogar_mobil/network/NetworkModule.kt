package com.example.huertohogar_mobil.network

import com.example.huertohogar_mobil.BuildConfig
import com.example.huertohogar_mobil.network.api.AuthApi
import com.example.huertohogar_mobil.network.api.CartApi
import com.example.huertohogar_mobil.network.api.ContactApi
import com.example.huertohogar_mobil.network.api.MessagesApi
import com.example.huertohogar_mobil.network.api.PedidosApi
import com.example.huertohogar_mobil.network.api.ProductsApi
import com.example.huertohogar_mobil.network.api.SocialApi
import com.example.huertohogar_mobil.network.api.UploadsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(@Named("auth") authOkHttpClient: OkHttpClient): Retrofit {
        val baseUrl = BuildConfig.BASE_URL.trimEnd('/') + "/" + BuildConfig.API_VERSION.trim('/') + "/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("main")
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        refreshAuthenticator: RefreshAuthenticator
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(refreshAuthenticator)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named("main")
    fun provideRetrofit(@Named("main") client: OkHttpClient): Retrofit {
        val baseUrl = BuildConfig.BASE_URL.trimEnd('/') + "/" + BuildConfig.API_VERSION.trim('/') + "/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(@Named("auth") authRetrofit: Retrofit): AuthApi = authRetrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideRefreshAuthenticator(
        tokenDataStore: com.example.huertohogar_mobil.data.datastore.TokenDataStore,
        authApi: AuthApi
    ): RefreshAuthenticator = RefreshAuthenticator(tokenDataStore, authApi)

    @Provides @Singleton fun provideProductsApi(@Named("main") retrofit: Retrofit): ProductsApi = retrofit.create(ProductsApi::class.java)
    @Provides @Singleton fun providePedidosApi(@Named("main") retrofit: Retrofit): PedidosApi = retrofit.create(PedidosApi::class.java)
    @Provides @Singleton fun provideCartApi(@Named("main") retrofit: Retrofit): CartApi = retrofit.create(CartApi::class.java)
    @Provides @Singleton fun provideMessagesApi(@Named("main") retrofit: Retrofit): MessagesApi = retrofit.create(MessagesApi::class.java)
    @Provides @Singleton fun provideSocialApi(@Named("main") retrofit: Retrofit): SocialApi = retrofit.create(SocialApi::class.java)
    @Provides @Singleton fun provideContactApi(@Named("main") retrofit: Retrofit): ContactApi = retrofit.create(ContactApi::class.java)
    @Provides @Singleton fun provideUploadsApi(@Named("main") retrofit: Retrofit): UploadsApi = retrofit.create(UploadsApi::class.java)
}
