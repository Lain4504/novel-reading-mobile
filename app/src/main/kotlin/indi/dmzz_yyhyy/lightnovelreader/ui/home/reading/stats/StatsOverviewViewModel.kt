package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.DurationFormat
import indi.dmzz_yyhyy.lightnovelreader.utils.quickSelect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

data class DailyDateDetails(
    val formattedTotal: String,
    val timeDetails: List<Pair<String, String>>,
    val firstBook: String?,
    val firstSeenTime: String?,
    val lastBook: String?,
    val lastSeenTime: String?
)

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
            bookIds.forEach { bookId ->
                if (bookId < 0) return@forEach // don't get total record bookId == -721
                viewModelScope.launch(Dispatchers.IO) {
                    bookRepository.getBookInformation(bookId).collect { bookInformation ->
                        _uiState.bookInformationMap[bookId] = bookInformation
                    }
                }
            }

            computeDateDetails(selectedDate = _uiState.selectedDate)

            _uiState.isLoading = false
            val elapsed = (System.currentTimeMillis() - time) / 1000.0
            Log.d("AppReadingStats", "Refresh completed in $elapsed seconds")
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.selectedDate = date
        computeDateDetails(date)
    }

    @OptIn(ExperimentalTime::class)
    private fun computeDateDetails(selectedDate: LocalDate) {
        viewModelScope.launch(Dispatchers.Default) {
            val records = _uiState.bookRecordsByDate[selectedDate] ?: emptyList()
            val dateFormatter = DateTimeFormatter.ofPattern("HH:mm")

            val totalSeconds = records.sumOf { it.totalTime }
            val totalDuration = totalSeconds.toDuration(DurationUnit.SECONDS)
            val formattedTotal = DurationFormat().format(totalDuration, DurationFormat.Unit.SECOND)

            val timeDetails = records.take(4).map { record ->
                val book = _uiState.bookInformationMap[record.bookId]?.title ?: "unknown"
                val duration = record.totalTime.toDuration(DurationUnit.SECONDS)
                val formattedTime = DurationFormat(Locale.ENGLISH).format(duration)
                book to formattedTime
            }.toMutableList().apply {
                if (records.size > 4) add("...${records.size - 4} more" to "")
            }

            val firstRecord = records.minByOrNull { it.firstSeen }
            val lastRecord = records.maxByOrNull { it.lastSeen }

            _uiState.selectedDateDetails = DailyDateDetails(
                formattedTotal = formattedTotal,
                timeDetails = timeDetails,
                firstBook = firstRecord?.let { _uiState.bookInformationMap[it.bookId]?.title },
                firstSeenTime = firstRecord?.firstSeen?.format(dateFormatter),
                lastBook = lastRecord?.let { _uiState.bookInformationMap[it.bookId]?.title },
                lastSeenTime = lastRecord?.lastSeen?.format(dateFormatter)
            )
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
}