package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.BookRecordConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import java.time.LocalDate
import java.time.LocalTime

@TypeConverters(
    BookRecordConverter::class,
    LocalDateTimeConverter::class
)

@Entity(
    tableName = "book_records",
    foreignKeys = [ForeignKey(
        entity = ReadingStatisticsEntity::class,
        parentColumns = ["date"],
        childColumns = ["date"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class BookRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = -1,
    val bookId: Int,
    val date: LocalDate,
    @ColumnInfo(name = "total_seconds")
    val totalSeconds: Int,
    @ColumnInfo(name = "sessions")
    val sessions: Int,
    @ColumnInfo(name = "first_seen")
    val firstSeen: LocalTime,
    @ColumnInfo(name = "last_seen")
    val lastSeen: LocalTime
)