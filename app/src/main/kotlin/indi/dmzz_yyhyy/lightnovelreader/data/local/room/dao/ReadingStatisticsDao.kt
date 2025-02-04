package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.BookRecordConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.CountConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.BookRecord
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.Count
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
@TypeConverters(
    LocalDateConverter::class,
    CountConverter::class,
    ListConverter::class,
    BookRecordConverter::class
)
interface ReadingStatisticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingStatistics(statistics: ReadingStatisticsEntity)

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getReadingStatisticsForDate(date: LocalDate): ReadingStatisticsEntity?

    @Query("SELECT * FROM reading_statistics")
    fun getAllReadingStatistics(): Flow<List<ReadingStatisticsEntity>>

    @Delete
    suspend fun deleteReadingStatistics(statistics: ReadingStatisticsEntity)

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getReadingTimeCountForDate(date: LocalDate): Count? =
        getReadingStatisticsForDate(date)?.readingTimeCount

    @Query("SELECT * FROM reading_statistics WHERE date BETWEEN :start AND :end")
    suspend fun getReadingStatisticsBetweenDates(start: LocalDate, end: LocalDate): List<ReadingStatisticsEntity>

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getBookRecordsForDate(date: LocalDate): Map<Int, BookRecord>? =
        getReadingStatisticsForDate(date)?.bookRecords

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getFavoriteBooksForDate(date: LocalDate): List<Int>? =
        getReadingStatisticsForDate(date)?.favoriteBooks

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getStartedBooksForDate(date: LocalDate): List<Int>? =
        getReadingStatisticsForDate(date)?.startedBooks

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getFinishedBooksForDate(date: LocalDate): List<Int>? =
        getReadingStatisticsForDate(date)?.startedBooks

    @Query("SELECT * FROM reading_statistics WHERE date = :date LIMIT 1")
    suspend fun getAverageSpeedForDate(date: LocalDate): Int? =
        getReadingStatisticsForDate(date)?.avgSpeed
}