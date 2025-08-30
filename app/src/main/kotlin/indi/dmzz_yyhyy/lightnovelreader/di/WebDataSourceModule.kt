package indi.dmzz_yyhyy.lightnovelreader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebDataSourceModule {
    @Singleton
    @Provides
    fun provideWebDataSource(webBookDataSourceManager: WebBookDataSourceManager): WebBookDataSource {
        println("initWebDataSource")
        return webBookDataSourceManager.getWebDataSource()
    }
}