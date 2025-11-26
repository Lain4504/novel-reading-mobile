package com.miraimagiclab.novelreadingapp.di

import android.content.Context
import com.miraimagiclab.novelreadingapp.data.auth.AuthApiService
import com.miraimagiclab.novelreadingapp.data.auth.TokenManager
import com.miraimagiclab.novelreadingapp.data.auth.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    @Provides
    @Singleton
    fun provideTokenStorage(
        @ApplicationContext context: Context
    ): TokenStorage = TokenStorage(context)

    @Provides
    @Singleton
    fun provideAuthApiService(
        @PublicClient httpClient: HttpClient,
        @AuthenticatedClient authenticatedHttpClient: HttpClient
    ): AuthApiService = AuthApiService(httpClient, authenticatedHttpClient)

    @Provides
    @Singleton
    fun provideTokenManager(
        tokenStorage: TokenStorage,
        @PublicClient httpClient: HttpClient
    ): TokenManager = TokenManager(tokenStorage, httpClient)
}
