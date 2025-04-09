package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class StatsDetailedViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStatsDetailedUiState()
    val uiState: StatsDetailedUiState = _uiState

    fun initialize(targetDate: LocalDate) {
        println("RECV init with date $targetDate")
        _uiState.targetDateRange = Pair(uiState.selectedDate, uiState.selectedDate)
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.isLoading = true
            loadStatistics()
            _uiState.isLoading = false
        }
    }

    fun setSelectedView(index: Int) {
        _uiState.selectedViewIndex = index
        updateDateRange()
    }

    private fun updateDateRange() {
        val selectedDate = uiState.selectedDate
        val today = LocalDate.now()
        val range = listOf<(LocalDate) -> Pair<LocalDate, LocalDate>>(
            { date -> Pair(date, date) },
            { date ->
                val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val validEndOfWeek = if (endOfWeek.isBefore(today)) endOfWeek else today
                Pair(startOfWeek, validEndOfWeek)
            },
            { date ->
                val startOfMonth = date.withDayOfMonth(1)
                val endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth())
                val validEndOfMonth = if (endOfMonth.isBefore(today)) endOfMonth else today
                Pair(startOfMonth, validEndOfMonth)
            }
        )

        _uiState.targetDateRange = range.getOrNull(_uiState.selectedViewIndex)
            ?.invoke(selectedDate)
            ?: throw IllegalArgumentException("Invalid view index")
    }

    private suspend fun loadStatistics() {
        val end = uiState.selectedDate
        val start = end.minusMonths(1)
        val bookRecordsMap = statsRepository.getBookRecords(start, end)
        val entities = statsRepository.getReadingStatistics(start, end)
        val bookIds = mutableSetOf<Int>()

        val allDates = generateSequence(start) { it.plusDays(1) }
            .takeWhile { it <= end }
            .toList()

        _uiState.targetDateRangeStatsMap = allDates.associateWith { date ->
            entities.values.find { it.date == date } ?: createDefaultEntity(date)
        }
        _uiState.targetDateRangeRecordsMap = bookRecordsMap

        bookRecordsMap.forEach { list ->
            list.value.forEach { entity ->
                bookIds.add(entity.bookId)
            }
        }
        println("bookIds is $bookIds")

        bookIds.forEach { id ->
            viewModelScope.launch(Dispatchers.IO) {
                bookRepository.getBookInformation(id).collect {
                    _uiState.bookInformationMap[id] = it
                }
            }
        }
    }

    private fun createDefaultEntity(date: LocalDate) = ReadingStatisticsEntity(
        date = date,
        readingTimeCount = Count(),
        avgSpeed = 0,
        favoriteBooks = emptyList(),
        startedBooks = emptyList(),
        finishedBooks = emptyList()
    )
}
