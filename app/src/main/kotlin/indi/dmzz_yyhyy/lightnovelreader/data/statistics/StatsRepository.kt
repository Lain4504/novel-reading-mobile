package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import com.google.gson.annotations.SerializedName
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ReadingStatisticsDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.Count
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    @SerializedName("s") val sessions: Int,
    @SerializedName("t") val totalSeconds: Int,
    @SerializedName("f") val firstSeen: LocalTime,
    @SerializedName("l") val lastSeen: LocalTime,
)

@Singleton
class StatsRepository @Inject constructor(
    private val readingStatisticsDao: ReadingStatisticsDao
) {
    private val buffer = mutableMapOf<Int, Pair<LocalTime, Int>>()
    private val bufferMutex = Mutex()


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
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds % 60

        if (minutes > 0) {
            println("Triggered minuted > 0, updating")

            updateReadingStatistics(
                ReadingStatsUpdate(
                    bookId = bookId,
                    secondDelta = minutes * 60,
                    localTime = startTime,
                    sessionDelta = 0
                )
            )
        }

        buffer[bookId] = if (remainingSeconds > 0) {
            Pair(LocalTime.now(), remainingSeconds)
        } else {
            println("Triggered clear buffer to 0")
            Pair(LocalTime.now(), 0)
        }

    }

    suspend fun forceFlushAll() = bufferMutex.withLock {
        buffer.keys.toList().forEach { bookId ->
            flushBookBuffer(bookId)
            buffer[bookId]?.let {
                println("Triggered FlushAll, remain ${it.second} seconds")
                updateReadingStatistics(
                    ReadingStatsUpdate(
                        bookId = bookId,
                        secondDelta = it.second,
                        localTime = it.first,
                        sessionDelta = 0
                    )
                )
            }
            buffer.remove(bookId)
        }
    }


    private suspend fun getOrCreateDailyStats(date: LocalDate) =
        readingStatisticsDao.getReadingStatisticsForDate(date) ?: createDefaultEntity(date)

    suspend fun getReadingEntitiesBetweenDates(start: LocalDate, end: LocalDate): List<ReadingStatisticsEntity> {
        return readingStatisticsDao.getReadingStatisticsBetweenDates(start, end)
    }

    private fun createDefaultEntity(date: LocalDate) = ReadingStatisticsEntity(
        date = date,
        readingTimeCount = Count(),
        bookRecords = emptyMap(),
        avgSpeed = 0,
        favoriteBooks = emptyList(),
        startedBooks = emptyList(),
        finishedBooks = emptyList()
    )

    suspend fun updateReadingStatistics(update: ReadingStatsUpdate) {
        val date = LocalDate.now()
        val existing = getOrCreateDailyStats(date)
        val updatedEntity = existing.copy(
            readingTimeCount = updateCount(existing.readingTimeCount, update),
            bookRecords = updateBookRecord(
                original = existing.bookRecords,
                update = update.copy(secondDelta = update.secondDelta)
            ),
            avgSpeed = update.currentSpeed ?: existing.avgSpeed
        )
        println("Triggered updateReadingStats records ${updatedEntity.bookRecords}")

        readingStatisticsDao.insertReadingStatistics(updatedEntity)
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

    private fun updateBookRecord(
        original: Map<Int, BookRecord>,
        update: ReadingStatsUpdate
    ) = original.toMutableMap().apply {
        val current = get(update.bookId) ?: BookRecord(
            totalSeconds = 0,
            firstSeen = update.localTime,
            lastSeen = update.localTime,
            sessions = 0
        )

        put(update.bookId, current.copy(
            sessions = current.sessions + update.sessionDelta,
            totalSeconds = current.totalSeconds + update.secondDelta,
            lastSeen = maxOf(current.lastSeen, update.localTime)
        ))
        println("Triggered updateBookRecord ${put(update.bookId, current.copy(
            sessions = current.sessions + update.sessionDelta,
            totalSeconds = current.totalSeconds + update.secondDelta,
            lastSeen = maxOf(current.lastSeen, update.localTime)
        ))}")

    }

    private fun updateList(
        original: List<Int>,
        bookId: Int,
        condition: Boolean
    ) = if (condition) (original + bookId).distinct() else original
}