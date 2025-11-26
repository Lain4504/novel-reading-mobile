package com.miraimagiclab.novelreadingapp.di

import com.miraimagiclab.novelreadingapp.data.local.room.LightNovelReaderDatabase
import com.miraimagiclab.novelreadingapp.data.local.room.dao.BookInformationDao
import com.miraimagiclab.novelreadingapp.data.local.room.dao.BookRecordDao
import com.miraimagiclab.novelreadingapp.data.local.room.dao.BookVolumesDao
import com.miraimagiclab.novelreadingapp.data.local.room.dao.ChapterContentDao
import com.miraimagiclab.novelreadingapp.data.local.room.dao.ReadingStatisticsDao
import com.miraimagiclab.novelreadingapp.data.local.room.dao.UserDataDao
import com.miraimagiclab.novelreadingapp.data.local.room.dao.UserReadingDataDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Singleton
    @Provides
    fun provideBookInformationDao(db: LightNovelReaderDatabase): BookInformationDao =
        db.bookInformationDao()

    @Singleton
    @Provides
    fun provideBookVolumesDao(db: LightNovelReaderDatabase): BookVolumesDao =
        db.bookVolumesDao()

    @Singleton
    @Provides
    fun provideChapterContentDao(db: LightNovelReaderDatabase): ChapterContentDao =
        db.chapterContentDao()

    @Singleton
    @Provides
    fun provideUserReadingDataDao(db: LightNovelReaderDatabase): UserReadingDataDao =
        db.userReadingDataDao()

    @Singleton
    @Provides
    fun provideUserDataDao(db: LightNovelReaderDatabase): UserDataDao =
        db.userDataDao()

    @Provides
    @Singleton
    fun provideReadingStatisticsDao(db: LightNovelReaderDatabase): ReadingStatisticsDao {
        return db.readingStatisticsDao()
    }

    @Provides
    @Singleton
    fun provideBookRecordsDao(db: LightNovelReaderDatabase): BookRecordDao {
        return db.bookRecordDao()
    }
}