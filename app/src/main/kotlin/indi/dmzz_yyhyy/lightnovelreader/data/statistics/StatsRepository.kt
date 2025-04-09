package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookRecordDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ReadingStatisticsDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
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

@Singleton
class StatsRepository @Inject constructor(
    private val readingStatisticsDao: ReadingStatisticsDao,
    private val bookRecordDao: BookRecordDao
) {
    private val buffer = mutableMapOf<Int, Pair<LocalTime, Int>>()
    private val bufferMutex = Mutex()
    val dateStatsMap = mutableMapOf<LocalDate, ReadingStatisticsEntity>()
    val dateRecordsMap = mutableMapOf<LocalDate, List<BookRecordEntity>>()
    val bookRecordsMap = mutableMapOf<Int, List<BookRecordEntity>>()
    private val cacheMutex = Mutex()

    suspend fun accumulateReadingTime(bookId: Int, seconds: Int) = bufferMutex.withLock {
        val current = buffer[bookId] ?: Pair(LocalTime.now(), 0)
        val newTotal = current.second + 60 /*FIXME*/
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
                sessionDelta = 0
            )
        )

        buffer.clear()
    }

    suspend fun forceFlushAll() = bufferMutex.withLock {
        buffer.keys.toList().forEach { bookId ->
            flushBookBuffer(bookId)
            buffer.remove(bookId)
        }
    }

    private fun <K, V> MutableMap<K, V>.putWithLimit(key: K, value: V) {
        if (size >= 50) {
            keys.firstOrNull()?.let { remove(it) }
        }
        this[key] = value
    }

    suspend fun getReadingStatistics(start: LocalDate, end: LocalDate? = null): Map<LocalDate, ReadingStatisticsEntity> {
        return if (end == null) {
            val cached = dateStatsMap[start]
            if (cached != null) {
                mapOf(start to cached)
            } else {
                val entity = readingStatisticsDao.getReadingStatisticsForDate(start) ?: createStatsEntity(start)
                dateStatsMap[start] = entity
                mapOf(start to entity)
            }
        } else {
            val allDates = generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .toList()

            val cachedStats = allDates.associateWithNotNull { dateStatsMap[it] }
            val missingDates = allDates.filter { it !in cachedStats }

            val fetchedStats = if (missingDates.isNotEmpty()) {
                readingStatisticsDao.getReadingStatisticsBetweenDates(missingDates.first(), missingDates.last())
                    .groupBy { it.date }
                    .mapValues { it.value.first() }
                    .also { dateStatsMap.putAll(it) }
            } else {
                emptyMap()
            }

            cachedStats + fetchedStats
        }
    }

    private inline fun <T, K : Any> Iterable<T>.associateWithNotNull(transform: (T) -> K?): Map<T, K> =
        mapNotNull { item -> transform(item)?.let { item to it } }.toMap()


    suspend fun getBookRecords(
        start: LocalDate,
        end: LocalDate? = null
    ): Map<LocalDate, List<BookRecordEntity>> {
        val raw = if (end == null) {
            val records = bookRecordDao.getBookRecordsForDate(start)
            mapOf(start to records)
        } else {
             bookRecordDao.getBookRecordsBetweenDates(start, end).groupBy { it.date }
        }
        val bookRecordsList = raw.mapValues { (_, records) ->
            records.filter { it.id != -721 } // We exclude total records with id -721 on fetch
        }.filterValues { it.isNotEmpty() }

        return bookRecordsList
    }

    private fun createTotalRecordEntity(): BookRecordEntity = BookRecordEntity(
        id = -721,
        date = LocalDate.now(),
        bookId = -721,
        sessions = 0,
        totalTime = 0,
        firstSeen = LocalTime.now(),
        lastSeen = LocalTime.now()
    )

    private fun createRecordEntity(bookId: Int): BookRecordEntity = BookRecordEntity(
        id = null,
        date = LocalDate.now(),
        bookId = bookId,
        sessions = 0,
        totalTime = 0,
        firstSeen = LocalTime.now(),
        lastSeen = LocalTime.now()
    )


    private fun createStatsEntity(date: LocalDate) = ReadingStatisticsEntity(
        date = date,
        readingTimeCount = Count(),
        avgSpeed = 0,
        favoriteBooks = emptyList(),
        startedBooks = emptyList(),
        finishedBooks = emptyList()
    )

    suspend fun getTotalBookRecord(): BookRecordEntity {
        val a = bookRecordDao.getTotalRecord() ?: createTotalRecordEntity()
        println("getTotalBookRecord -> $a (${bookRecordDao.getTotalRecord()})")
        return a
    }

    private suspend fun updateTotalRecord(update: ReadingStatsUpdate) {
        val totalRecord = getTotalBookRecord()

        val updatedTotal = totalRecord.copy(
            sessions = totalRecord.sessions + update.sessionDelta,
            totalTime = totalRecord.totalTime + update.secondDelta,
            lastSeen = LocalTime.now()
        )
        println("POST updateTotalRecord\n$updatedTotal")

        bookRecordDao.insertBookRecord(updatedTotal)
    }

    suspend fun updateReadingStatistics(update: ReadingStatsUpdate) {
        val today = LocalDate.now()
        val statsEntity = getReadingStatistics(today)[today] ?: createStatsEntity(today)

        val updatedStats = statsEntity.copy(
            readingTimeCount = updateCount(statsEntity.readingTimeCount, update)
            // avgSpeed = update.currentSpeed ?: statsEntity.avgSpeed
        )

        val existingBookRecord = bookRecordDao.getBookRecordByIdAndDate(update.bookId, today) ?: createRecordEntity(update.bookId)
        println("existingBookRecord is $existingBookRecord")

        val newBookRecord = existingBookRecord.copy(
            totalTime = existingBookRecord.totalTime + update.secondDelta,
            sessions = existingBookRecord.sessions + update.sessionDelta,
            lastSeen = update.localTime
        )

        println("newBookRecord is $newBookRecord")
        updateTotalRecord(update)

        cacheMutex.withLock {
            dateStatsMap.putWithLimit(today, updatedStats)
            dateRecordsMap.putWithLimit(today, listOf(newBookRecord))
        }

        readingStatisticsDao.insertReadingStatistics(updatedStats)
        bookRecordDao.insertBookRecord(newBookRecord)

        buffer.clear()
    }

    suspend fun updateBookStatus(
        bookId: Int,
        isFavorite: Boolean = false,
        isFirstReading: Boolean = false,
        isFinishedReading: Boolean = false
    ) {
        val date = LocalDate.now()
        val existing = getReadingStatistics(date)
        val currentEntity = existing[date] ?: createStatsEntity(date)

        val updatedEntity = currentEntity.copy(
            favoriteBooks = updateList(currentEntity.favoriteBooks, bookId, isFavorite),
            startedBooks = updateList(currentEntity.startedBooks, bookId, isFirstReading),
            finishedBooks = updateList(currentEntity.finishedBooks, bookId, isFinishedReading)
        )
        println("Triggered updateBookStatus with Entity $updatedEntity")

        cacheMutex.withLock {
            dateStatsMap.putWithLimit(date, updatedEntity)
        }

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