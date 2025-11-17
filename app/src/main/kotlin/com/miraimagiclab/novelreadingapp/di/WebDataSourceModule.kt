package com.miraimagiclab.novelreadingapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.miraimagiclab.novelreadingapp.data.web.WebBookDataSourceManager
import com.miraimagiclab.novelreadingapp.data.web.WebBookDataSourceProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebDataSourceModule {
    @Singleton
    @Provides
    fun provideWebDataSourceProvider(webBookDataSourceManager: WebBookDataSourceManager): WebBookDataSourceProvider {
        return webBookDataSourceManager.getWebDataSourceProvider()
    }
}