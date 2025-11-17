package com.miraimagiclab.novelreadingapp.di

import android.content.Context
import com.ketch.Ketch
import com.ketch.NotificationConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.miraimagiclab.novelreadingapp.R
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KetchModule {
    @Singleton
    @Provides
    fun provideKetch(@ApplicationContext context: Context): Ketch =
        Ketch.builder().setNotificationConfig(
            config = NotificationConfig(
                enabled = true,
                smallIcon = R.drawable.icon_foreground
            )
        ).build(context)
}