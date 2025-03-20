package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class StatsOverviewViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val bookRepository: BookRepository
) : ViewModel() {
    private var _uiState = MutableStatisticsOverviewUiState()
    val uiState: StatsOverviewUiState = _uiState

    init {
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            val time = System.currentTimeMillis()
            Log.d("AppReadingStats", "Refresh started")

            _uiState.isLoading = true
            _uiState.datePair = _uiState.datePair.copy(
                first = LocalDate.now().minusMonths(6)
            )
            val start = _uiState.datePair.first
            val end = _uiState.datePair.second
            Log.d("AppReadingStats", "START generateLevelMap")
            generateLevelMap(start, end)
            Log.d("AppReadingStats", "FINISH generateLevelMap")


            val dates = mutableListOf<LocalDate>()
            var currentDate = start
            while (!currentDate.isAfter(end)) {
                dates.add(currentDate)
                currentDate = currentDate.plusDays(1)
            }

            val allBookRecords = mutableListOf<BookRecordEntity>()
            dates.forEach { date ->
                val records = statsRepository.getBookRecordsForDate(date)
                allBookRecords.addAll(records)
            }

            _uiState.bookRecordsByDate = allBookRecords.groupBy { it.date }
            println("BY DATE = ${uiState.bookRecordsByDate}")
            _uiState.bookRecordsByBookId = allBookRecords.groupBy { it.bookId }
            println("BY BID = ${uiState.bookRecordsByBookId}")
            val bookIds = _uiState.bookRecordsByBookId.keys.toSet()
            bookIds.forEach {
                viewModelScope.launch(Dispatchers.IO) {
                    bookRepository.getBookInformation(it).collect {
                        _uiState.bookInformationMap[it.id] = it
                    }
                }
            }
            println("POST fetch, map = ${_uiState.bookInformationMap}")

            _uiState.isLoading = false

            var totalStartedBooks = 0
            var totalReadInSeconds = 0
            var totalSessions = 0
            _uiState.dateStatsEntityMap.forEach { (_, readingStatistics) ->
                totalStartedBooks += readingStatistics.startedBooks.size
            }
            _uiState.bookRecordsByDate.forEach { (_, records) ->
                totalReadInSeconds += records.sumOf { it.totalSeconds }
                totalSessions += records.sumOf { it.sessions }
            }
            _uiState.totalSessions = totalSessions
            _uiState.totalStartedBooks = totalStartedBooks
            _uiState.totalReadInSeconds = totalReadInSeconds

            val elapsed = (System.currentTimeMillis() - time) / 1000.0
            Log.d("AppReadingStats", "Refresh completed in $elapsed seconds")
        }
    }

    private suspend fun generateLevelMap(
        startDate: LocalDate,
        endDate: LocalDate
    ) {

        val entities = statsRepository.getReadingEntitiesBetweenDates(startDate, endDate)
        val allDates = generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it <= endDate }
            .toList()

        val entityMap = allDates.associateWith { date ->
            (entities.find { it.date == date } ?: createDefaultEntity(date))
        }

        val bulkData = entityMap.mapValues { (_, entity) ->
            entity.readingTimeCount.getTotalMinutes()
        }

        _uiState.dateReadingTimeMap = bulkData

        val readingTimes = allDates.map { bulkData[it] ?: 0 }
        val thresholds = readingTimes.filter { it > 0 }.run {
            if (isEmpty()) listOf(0, 0, 0) else listOf(
                quickSelect(this, 0.25),
                quickSelect(this, 0.5),
                quickSelect(this, 0.75)
            )
        }
        _uiState.thresholds = thresholds[2]

        _uiState.dateStatsEntityMap = entityMap

        val levelMap = allDates.associateWith { date ->
            val readingTime = bulkData[date] ?: 0
            when {
                thresholds.all { it == 0 } -> Level.Zero
                readingTime >= thresholds[2] -> Level.Four
                readingTime >= thresholds[1] -> Level.Three
                readingTime >= thresholds[0] -> Level.Two
                readingTime > 0 -> Level.One
                else -> Level.Zero
            }
        }
        _uiState.dateLevelMap = levelMap
    }

    private fun createDefaultEntity(date: LocalDate) = ReadingStatisticsEntity(
        date = date,
        readingTimeCount = Count(),
        avgSpeed = 0,
        favoriteBooks = emptyList(),
        startedBooks = emptyList(),
        finishedBooks = emptyList()
    )

    private fun quickSelect(list: List<Int>, percentile: Double): Int {
        val targetIndex = (list.size * percentile).toInt().coerceIn(list.indices)
        val arr = list.toMutableList()

        var left = 0
        var right = arr.lastIndex

        while (left < right) {
            val pivotIndex = partition(arr, left, right)
            when {
                pivotIndex == targetIndex -> return arr[pivotIndex]
                pivotIndex < targetIndex -> left = pivotIndex + 1
                else -> right = pivotIndex - 1
            }
        }
        return arr[left]
    }

    private fun partition(arr: MutableList<Int>, left: Int, right: Int): Int {
        val pivot = arr[right]
        var i = left
        for (j in left until right) {
            if (arr[j] <= pivot) {
                Collections.swap(arr, i, j)
                i++
            }
        }
        Collections.swap(arr, i, right)
        return i
    }

    fun selectDate(date: LocalDate) {
        _uiState.selectedDate = date
        _uiState.selected = true
    }
}