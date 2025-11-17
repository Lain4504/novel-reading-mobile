package com.miraimagiclab.novelreadingapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.miraimagiclab.novelreadingapp.data.userdata.UserDataRepository
import com.miraimagiclab.novelreadingapp.data.web.WebBookDataSourceManager
import io.lain4504.novelreadingapp.api.userdata.UserDataRepositoryApi
import io.lain4504.novelreadingapp.api.web.WebBookDataSourceManagerApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserDataRepositoryApi(
        userDataRepository: UserDataRepository
    ): UserDataRepositoryApi

    @Binds
    @Singleton
    abstract fun bindWebBookDataSourceManagerApi(
        webBookDataSourceManager: WebBookDataSourceManager
    ): WebBookDataSourceManagerApi
}

