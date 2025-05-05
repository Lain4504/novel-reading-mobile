package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookRecordDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ReadingStatisticsDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.utils.putWithLimit
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

data class ReadingStatsUpdate(
    val bookId: Int,
    val secondDelta: Int = 0,
    val sessionDelta: Int = 0,
    val localTime: LocalTime = LocalTime.now(),
    val startedBooks: List<Int> = emptyList(),
    val favoriteBooks: List<Int> = emptyList()
)

@Singleton
class StatsRepository @Inject constructor(
    private val readingStatisticsDao: ReadingStatisticsDao,
    private val bookRecordDao: BookRecordDao
) {
    private val bookReadTimeBuffer = mutableMapOf<Int, Pair<LocalTime, Int>>()
    private val dateStatsMap = mutableMapOf<LocalDate, ReadingStatisticsEntity>()
    private val dateRecordsMap = mutableMapOf<LocalDate, List<BookRecordEntity>>()

    suspend fun accumulateBookReadTime(bookId: Int, seconds: Int) {
        if (seconds < 0) {
            bookReadTimeBuffer.keys.toList().forEach { _ ->
                clearBookReadTimeBuffer(bookId)
                bookReadTimeBuffer.remove(bookId)
            }
            return
        }
        val current = bookReadTimeBuffer[bookId] ?: Pair(LocalTime.now(), 0)
        val newTotal = current.second + seconds
        bookReadTimeBuffer[bookId] = current.copy(second = newTotal)

        if (newTotal >= 60 || Duration.between(current.first, LocalTime.now()).seconds >= 60) {
            clearBookReadTimeBuffer(bookId)
        }
    }

    private suspend fun clearBookReadTimeBuffer(bookId: Int) {
        val (startTime, totalSeconds) = bookReadTimeBuffer[bookId] ?: return

        updateReadingStatistics(
            ReadingStatsUpdate(
                bookId = bookId,
                secondDelta = totalSeconds,
                localTime = startTime,
                sessionDelta = 0
            )
        )

        bookReadTimeBuffer.clear()
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

    /**
     具有特殊 id 的总阅读记录 Entity，用于总体统计
     */
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


    fun createStatsEntity(date: LocalDate) = ReadingStatisticsEntity(
        date = date,
        readingTimeCount = Count(),
        foregroundTime = 0,
        favoriteBooks = emptyList(),
        startedBooks = emptyList(),
        finishedBooks = emptyList()
    )

    suspend fun getTotalBookRecord(): BookRecordEntity {
        return bookRecordDao.getTotalRecord() ?: createTotalRecordEntity()
    }

    private suspend fun updateTotalRecord(update: ReadingStatsUpdate) {
        val totalRecord = getTotalBookRecord()
        val updatedTotal = totalRecord.copy(
            sessions = totalRecord.sessions + update.sessionDelta,
            totalTime = totalRecord.totalTime + update.secondDelta,
            lastSeen = LocalTime.now()
        )

        bookRecordDao.insertBookRecord(updatedTotal)
    }

    suspend fun updateReadingStatistics(update: ReadingStatsUpdate) {
        val today = LocalDate.now()
        val statsEntity = getReadingStatistics(today)[today] ?: createStatsEntity(today)

        val updatedStats = statsEntity.copy(
            readingTimeCount = updateCount(statsEntity.readingTimeCount, update)
        )

        val existingBookRecord = bookRecordDao.getBookRecordByIdAndDate(update.bookId, today) ?: createRecordEntity(update.bookId)

        val newBookRecord = existingBookRecord.copy(
            totalTime = existingBookRecord.totalTime + update.secondDelta,
            sessions = existingBookRecord.sessions + update.sessionDelta,
            lastSeen = update.localTime
        )

        updateTotalRecord(update)

        dateStatsMap.putWithLimit(today, updatedStats)
        dateRecordsMap.putWithLimit(today, listOf(newBookRecord))

        readingStatisticsDao.insertReadingStatistics(updatedStats)
        bookRecordDao.insertBookRecord(newBookRecord)

        bookReadTimeBuffer.clear()
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

        dateStatsMap.putWithLimit(date, updatedEntity)
        readingStatisticsDao.insertReadingStatistics(updatedEntity)
    }

    private fun updateCount(count: Count, update: ReadingStatsUpdate): Count {
        val minutesDelta = update.secondDelta / 60
        if (minutesDelta > 0) {
            val hour = update.localTime.hour
            val totalMinutes = count.getMinute(hour) + minutesDelta
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