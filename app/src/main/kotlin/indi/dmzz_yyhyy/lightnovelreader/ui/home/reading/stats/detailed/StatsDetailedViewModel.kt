package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
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
        _uiState.targetDateRange = Pair(targetDate, targetDate)
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
        val range = listOf<(LocalDate) -> Pair<LocalDate, LocalDate>>(
            { date -> Pair(date, date) },
            { date ->
                val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                Pair(startOfWeek, endOfWeek)
            },
            { date ->
                val startOfMonth = date.withDayOfMonth(1)
                val endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth())
                Pair(startOfMonth, endOfMonth)
            }
        )

        _uiState.targetDateRange = range.getOrNull(_uiState.selectedViewIndex)
            ?.invoke(selectedDate)
            ?: throw IllegalArgumentException("Invalid view index")
    }

    private suspend fun loadStatistics() {
        val end = uiState.selectedDate.plusMonths(1).withDayOfMonth(7)
        val start = uiState.selectedDate.withDayOfMonth(1).minusDays(7)

        val bookRecordsMap = statsRepository.getBookRecords(start, end)
        val entities = statsRepository.getReadingStatistics(start, end)

        val bookIds = mutableListOf<Int>()

        val allDates = generateSequence(start) { it.plusDays(1) }
            .takeWhile { it <= end }
            .toList()

        val statsMap = allDates.associateWith { date ->
            entities.values.find { it.date == date } ?: statsRepository.createStatsEntity(date)
        }.toSortedMap()

        statsMap.values.forEach { entity ->
            bookIds.addAll(entity.favoriteBooks)
            bookIds.addAll(entity.startedBooks)
            bookIds.addAll(entity.finishedBooks)
        }

        _uiState.targetDateRangeStatsMap = statsMap
        _uiState.targetDateRangeRecordsMap = allDates.associateWith { date ->
            bookRecordsMap[date] ?: emptyList()
        }.toSortedMap()

        bookRecordsMap.forEach { (_, list) ->
            list.fastForEach { entity ->
                bookIds.add(entity.bookId)
            }
        }

        bookIds.distinct().fastForEach { id ->
            viewModelScope.launch(Dispatchers.IO) {
                bookRepository.getBookInformationFlow(id).collect {
                    _uiState.bookInformationMap[id] = it
                }
            }
        }
    }

}
