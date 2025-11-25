package com.miraimagiclab.novelreadingapp.di

import com.miraimagiclab.novelreadingapp.data.auth.AuthInterceptor
import com.miraimagiclab.novelreadingapp.data.auth.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    @PublicClient
    fun providePublicHttpClient(): HttpClient =
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = false
                    }
                )
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedHttpClient(
        tokenManager: TokenManager
    ): HttpClient =
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = false
                    }
                )
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            // Install authentication interceptor
            install(AuthInterceptor(tokenManager).plugin)
        }

    // Default HttpClient for backward compatibility (uses public client)
    @Provides
    @Singleton
    fun provideHttpClient(@PublicClient publicClient: HttpClient): HttpClient = publicClient
}

