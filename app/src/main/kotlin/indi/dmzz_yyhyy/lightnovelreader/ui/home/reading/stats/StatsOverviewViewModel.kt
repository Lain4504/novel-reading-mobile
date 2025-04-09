package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
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
        reloadData()
    }

    private fun reloadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val time = System.currentTimeMillis()
            Log.d("AppReadingStats", "Refresh started")
            _uiState.totalRecordEntity = statsRepository.getTotalBookRecord()

            _uiState.isLoading = true
            _uiState.dateRange = _uiState.dateRange.copy(
                first = LocalDate.now().minusMonths(6)
            )
            val startDate = _uiState.dateRange.first
            val endDate = _uiState.dateRange.second
            Log.d("AppReadingStats", "START generateLevelMap")
            generateLevelMap(startDate, endDate)
            Log.d("AppReadingStats", "FINISH generateLevelMap")


            val bookRecordsMap = statsRepository.getBookRecords(startDate, endDate)

            _uiState.bookRecordsByDate = bookRecordsMap

            val allBookRecords = bookRecordsMap.values.flatten()

            val groupedByBookId = allBookRecords.groupBy { it.bookId }
            _uiState.bookRecordsByBookId = groupedByBookId

            val bookIds = groupedByBookId.keys
            println("getbookinfo list = $bookIds")
            bookIds.forEach { bookId ->
                if (bookId < 0) return@forEach // don't get total record bookId == -721
                viewModelScope.launch(Dispatchers.IO) {
                    bookRepository.getBookInformation(bookId).collect { bookInformation ->
                        _uiState.bookInformationMap[bookId] = bookInformation
                    }
                }
            }

            _uiState.isLoading = false
            val elapsed = (System.currentTimeMillis() - time) / 1000.0
            Log.d("AppReadingStats", "Refresh completed in $elapsed seconds")

        }
    }

    private suspend fun generateLevelMap(
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val dateStatsEntityMap = statsRepository.getReadingStatistics(startDate, endDate)
        val localDateList = dateStatsEntityMap.values.map { it.date }.sorted()

        val entityMap = dateStatsEntityMap.values.associateBy { it.date }

        val dateTotalTimeMap = entityMap.mapValues { (_, entity) ->
            entity.readingTimeCount.getTotalMinutes()
        }

        _uiState.dateReadingTimeMap = dateTotalTimeMap

        val readingTimes = localDateList.map { dateTotalTimeMap[it] ?: 0 }
        val thresholds = readingTimes.filter { it > 0 }.run {
            if (isEmpty()) listOf(0, 0, 0) else listOf(
                quickSelect(this, 0.25),
                quickSelect(this, 0.5),
                quickSelect(this, 0.75)
            )
        }
        _uiState.thresholds = thresholds[2]

        _uiState.dateStatsEntityMap = entityMap

        val dateLevelMap = localDateList.associateWith { date ->
            val readingTime = dateTotalTimeMap[date] ?: 0
            when {
                thresholds.all { it == 0 } -> Level.Zero
                readingTime >= thresholds[2] -> Level.Four
                readingTime >= thresholds[1] -> Level.Three
                readingTime >= thresholds[0] -> Level.Two
                readingTime > 0 -> Level.One
                else -> Level.Zero
            }
        }
        _uiState.dateLevelMap = dateLevelMap
    }


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