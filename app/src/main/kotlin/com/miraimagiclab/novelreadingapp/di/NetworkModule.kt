package com.miraimagiclab.novelreadingapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.miraimagiclab.novelreadingapp.network.GraphQLClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideGraphQLClient(): GraphQLClient {
        return GraphQLClient()
    }
}

