package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookRecordDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ReadingStatisticsDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.Count
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

data class ReadingStatsUpdate(
    val bookId: Int,
    val secondDelta: Int = 0,
    val sessionDelta: Int = 0,
    val currentSpeed: Int? = 0,
    val localTime: LocalTime = LocalTime.now(),
    val startedBooks: List<Int> = emptyList(),
    val favoriteBooks: List<Int> = emptyList()
)

data class BookRecord(
    val sessions: Int,
    val totalSeconds: Int,
    val firstSeen: LocalTime,
    val lastSeen: LocalTime,
)

@Singleton
class StatsRepository @Inject constructor(
    private val readingStatisticsDao: ReadingStatisticsDao,
    private val bookRecordDao: BookRecordDao
) {
    private val buffer = mutableMapOf<Int, Pair<LocalTime, Int>>()
    private val bufferMutex = Mutex()
    private val dateNow = LocalDate.now()


    suspend fun accumulateReadingTime(bookId: Int, seconds: Int) = bufferMutex.withLock {
        val current = buffer[bookId] ?: Pair(LocalTime.now(), 0)
        val newTotal = current.second + seconds
        buffer[bookId] = current.copy(second = newTotal)
        println("-> accumulate bookId=$bookId, sec=$newTotal")

        if (newTotal >= 60 || Duration.between(current.first, LocalTime.now()).seconds >= 60) {
            flushBookBuffer(bookId)
        }
    }

    private suspend fun flushBookBuffer(bookId: Int) {
        val (startTime, totalSeconds) = buffer[bookId] ?: return

        updateReadingStatistics(
            ReadingStatsUpdate(
                bookId = bookId,
                secondDelta = totalSeconds,
                localTime = startTime,
                sessionDelta = if (totalSeconds > 0) 1 else 0
            )
        )

        buffer[bookId] = Pair(LocalTime.now(), 0)
    }

    suspend fun forceFlushAll() = bufferMutex.withLock {
        buffer.keys.toList().forEach { bookId ->
            flushBookBuffer(bookId)
            buffer.remove(bookId)
        }
    }

    private suspend fun getOrCreateDailyStats(date: LocalDate) =
        readingStatisticsDao.getReadingStatisticsForDate(date) ?: createDefaultEntity(date)

    suspend fun getReadingEntitiesBetweenDates(start: LocalDate, end: LocalDate): List<ReadingStatisticsEntity> {
        return readingStatisticsDao.getReadingStatisticsBetweenDates(start, end)
    }

    suspend fun getBookRecordsForDate(date: LocalDate): List<BookRecordEntity> {
        return bookRecordDao.getBookRecordsForDate(date)
    }

    suspend fun getBookRecordsBetweenDates(start: LocalDate, end: LocalDate): Map<LocalDate, List<BookRecordEntity>> {
        return bookRecordDao.getBookRecordsBetweenDates(start, end)
            .groupBy { it.date }
    }


    private fun createDefaultEntity(date: LocalDate) = ReadingStatisticsEntity(
        date = date,
        readingTimeCount = Count(),
        avgSpeed = 0,
        favoriteBooks = emptyList(),
        startedBooks = emptyList(),
        finishedBooks = emptyList()
    )

    suspend fun updateReadingStatistics(update: ReadingStatsUpdate) {
        val existingStats = getOrCreateDailyStats(dateNow)

        val updatedStats = existingStats.copy(
            readingTimeCount = updateCount(existingStats.readingTimeCount, update),
            avgSpeed = update.currentSpeed ?: existingStats.avgSpeed
        )

        val existingBookRecord = bookRecordDao.getBookRecordByIdAndDate(update.bookId, dateNow)
        println("existingBookRecord is $existingBookRecord")

        val newBookRecord = existingBookRecord?.copy(
            totalSeconds = existingBookRecord.totalSeconds + update.secondDelta,
            sessions = existingBookRecord.sessions + update.sessionDelta,
            lastSeen = update.localTime
        ) ?: BookRecordEntity(
            bookId = update.bookId,
            date = dateNow,
            totalSeconds = update.secondDelta,
            sessions = update.sessionDelta,
            firstSeen = update.localTime,
            lastSeen = update.localTime
        )
        println("newBookRecord is $newBookRecord")

        readingStatisticsDao.insertReadingStatistics(updatedStats)
        bookRecordDao.insertBookRecord(newBookRecord)

        buffer[update.bookId]?.let {
            if (it.second <= update.secondDelta) {
                buffer.remove(update.bookId)
            }
        }
    }


    suspend fun updateBookStatus(
        bookId: Int,
        isFavorite: Boolean = false,
        isFirstReading: Boolean = false,
        isFinishedReading: Boolean = false
    ) {
        val date = LocalDate.now()
        val existing = getOrCreateDailyStats(date)

        val updatedEntity = existing.copy(
            favoriteBooks = updateList(existing.favoriteBooks, bookId, isFavorite),
            startedBooks = updateList(existing.startedBooks, bookId, isFirstReading),
            finishedBooks = updateList(existing.finishedBooks, bookId, isFinishedReading)
        )
        println("Triggered updateBookStatus with Entity $updatedEntity")

        readingStatisticsDao.insertReadingStatistics(updatedEntity)
    }

    private fun updateCount(count: Count, update: ReadingStatsUpdate): Count {
        val minutesDelta = update.secondDelta / 60
        if (minutesDelta > 0) {
            val hour = update.localTime.hour
            val totalMinutes = count.getMinute(hour) + minutesDelta
            println("Triggered updateCount, [$totalMinutes min] + -> $hour")
            count.setMinute(hour, totalMinutes.coerceAtMost(60))
        }
        return count
    }

    private fun updateList(
        original: List<Int>,
        bookId: Int,
        condition: Boolean
    ) = if (condition) (original + bookId).distinct() else original
}