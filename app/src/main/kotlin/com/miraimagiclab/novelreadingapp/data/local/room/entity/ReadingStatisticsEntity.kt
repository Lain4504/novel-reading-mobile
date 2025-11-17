package com.miraimagiclab.novelreadingapp.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.miraimagiclab.novelreadingapp.data.local.room.converter.CountConverter
import com.miraimagiclab.novelreadingapp.data.local.room.converter.ListConverter
import com.miraimagiclab.novelreadingapp.data.local.room.converter.LocalDateTimeConverter
import com.miraimagiclab.novelreadingapp.data.statistics.Count
import java.time.LocalDate

@TypeConverters(
    LocalDateTimeConverter::class,
    CountConverter::class,
    ListConverter::class
)
@Entity(tableName = "reading_statistics")
data class ReadingStatisticsEntity(
    @PrimaryKey
    val date: LocalDate,
    @ColumnInfo(name = "reading_time_count")
    val readingTimeCount: Count,
    @ColumnInfo(name = "foreground_time")
    val foregroundTime: Int,
    @ColumnInfo(name = "favorite_books")
    val favoriteBooks: List<String>,
    @ColumnInfo(name = "started_books")
    val startedBooks: List<String>,
    @ColumnInfo(name = "finished_books")
    val finishedBooks: List<String>
)
