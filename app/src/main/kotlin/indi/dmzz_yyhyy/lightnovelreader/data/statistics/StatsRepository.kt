package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ReadingStatisticsDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.Count
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class ReadingStatsUpdate(
    val bookId: Int,
    val seconds: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isStart: Boolean = false,
    val isFinish: Boolean = false,
    val currentSpeed: Int? = null
)

data class BookRecord(
    val totalSeconds: Long,
    val firstSeen: Long,
    val lastSeen: Long,
    val tags: Set<String>
)

@Singleton
class StatsRepository @Inject constructor(
    private val readingStatisticsDao: ReadingStatisticsDao
) {
    private suspend fun getOrCreateDailyStats(date: LocalDate) =
        readingStatisticsDao.getReadingStatisticsForDate(date) ?: createDefaultEntity(date)

    private fun createDefaultEntity(date: LocalDate) = ReadingStatisticsEntity(
        date = date,
        readingTimeCount = Count(),
        bookRecords = emptyMap(),
        favoriteBooks = emptyList(),
        startedBooks = emptyList(),
        finishedBooks = emptyList(),
        avgSpeed = 0
    )

    suspend fun updateReadingStatistics(update: ReadingStatsUpdate) {
        val date = LocalDate.now()
        val existing = getOrCreateDailyStats(date)

        val updatedEntity = existing.copy(
            readingTimeCount = updateCount(existing.readingTimeCount, update),
            bookRecords = updateBookRecord(existing.bookRecords, update),
            favoriteBooks = updateList(existing.favoriteBooks, update.bookId, update.isFavorite),
            startedBooks = updateList(existing.startedBooks, update.bookId, update.isStart),
            finishedBooks = updateList(existing.finishedBooks, update.bookId, update.isFinish),
            avgSpeed = update.currentSpeed ?: existing.avgSpeed
        )

        readingStatisticsDao.insertReadingStatistics(updatedEntity)
    }

    suspend fun getReadingTimesBetweenDates(start: LocalDate, end: LocalDate): Map<LocalDate, Int> {
        val entities = readingStatisticsDao.getReadingStatisticsBetweenDates(start, end)
        return entities.associate { entity ->
            entity.date to entity.readingTimeCount.getTotalMinutes()
        }.withDefault { 0 }
    }


    private fun updateCount(count: Count, update: ReadingStatsUpdate): Count {
        val diff: Int = (update.seconds / 60).toInt()
        if (diff > 0) {
            val hour = Instant.ofEpochMilli(update.endTime).atZone(ZoneId.systemDefault()).hour
            val minutes = count.getMinute(hour) + diff
            if (minutes in 0..60) count.setMinute(hour, minutes)
        }
        return count
    }

    private fun updateBookRecord(
        original: Map<Int, BookRecord>,
        update: ReadingStatsUpdate
    ) = original.toMutableMap().apply {
        val current = get(update.bookId) ?: BookRecord(0, update.startTime, update.endTime, setOf())
        put(update.bookId, current.copy(
            totalSeconds = current.totalSeconds + update.seconds,
            firstSeen = minOf(current.firstSeen, update.startTime),
            lastSeen = maxOf(current.lastSeen, update.endTime)
        ))
    }

    private fun updateList(
        original: List<Int>,
        bookId: Int,
        condition: Boolean = true
    ) = if (condition) (original + bookId).distinct() else original
}