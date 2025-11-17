package com.miraimagiclab.novelreadingapp.di

import android.content.Context
import com.miraimagiclab.novelreadingapp.data.local.room.LightNovelReaderDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): LightNovelReaderDatabase =
        LightNovelReaderDatabase.Companion.getInstance(context)
}