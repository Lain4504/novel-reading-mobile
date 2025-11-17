package com.miraimagiclab.novelreadingapp.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.miraimagiclab.novelreadingapp.network.NetworkConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApolloModule {
    
    @Provides
    @Singleton
    fun provideApolloClient(
        @ApplicationContext context: Context
    ): ApolloClient {
        val cacheFactory = SqlNormalizedCacheFactory(context, "apollo_cache.db")

        return ApolloClient.Builder()
            .serverUrl(NetworkConfig.GRAPHQL_ENDPOINT)
            .normalizedCache(cacheFactory)
            .addHttpHeader("Content-Type", "application/json")
            .build()
    }
}

