package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import android.util.Log
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.DurationFormat
import indi.dmzz_yyhyy.lightnovelreader.utils.quickSelect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class DailyDateDetails(
    val formattedTotalTime: String,
    val timeDetails: List<Pair<BookInformation, Int>?>,
    val firstBook: BookInformation?,
    val firstSeenTime: String?,
    val lastBook: BookInformation?,
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
            _uiState.isLoading = true

            val time = System.currentTimeMillis()
            Log.d("AppReadingStats", "Refresh started")
            _uiState.selectedDate = LocalDate.now()
            _uiState.totalRecordEntity = statsRepository.getTotalBookRecord()

            val startDate = _uiState.startDate
            val endDate = LocalDate.now()
            generateLevelMap(startDate, endDate)

            val bookRecordsMap = statsRepository.getBookRecords(startDate, endDate)
            _uiState.bookRecordsByDate = bookRecordsMap
            println("POST getBookRecordMap, length ${bookRecordsMap.size}")
            val allBookIds = bookRecordsMap.flatMap { it.value.map { record -> record.bookId } }

            allBookIds.fastForEach { id ->
                viewModelScope.launch(Dispatchers.IO) {
                    bookRepository.getBookInformation(id).collect {
                        _uiState.bookInformationMap[it.id] = it
                    }
                }
            }
            selectDate(_uiState.selectedDate)

            _uiState.isLoading = false
            val elapsed = (System.currentTimeMillis() - time) / 1000.0
            Log.d("AppReadingStats", "Refresh completed in $elapsed seconds")
        }
    }

    fun selectDate(date: LocalDate) {
        println("SELECTED $date")
        _uiState.selectedDate = date
        getDateDetails(date)
        println("POST selectDate")
    }

    private fun getDateDetails(selectedDate: LocalDate) {
        val records = _uiState.bookRecordsByDate[selectedDate] ?: emptyList()
        val dateFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val totalDuration = records.sumOf { it.totalTime }.toDuration(DurationUnit.SECONDS)
        val formattedTotalTime = DurationFormat().format(totalDuration, DurationFormat.Unit.SECOND)
        val timeDetails = records.map { record ->
            val book = _uiState.bookInformationMap[record.bookId] ?: BookInformation.empty()
            val time = record.totalTime
            book to time
        }.toMutableList()
        val firstRecord = records.minByOrNull { it.firstSeen }
        val lastRecord = records.maxByOrNull { it.lastSeen }
        _uiState.selectedDateDetails = DailyDateDetails(
            formattedTotalTime = formattedTotalTime,
            timeDetails = timeDetails,
            firstBook = firstRecord?.let { _uiState.bookInformationMap[it.bookId] },
            firstSeenTime = firstRecord?.firstSeen?.format(dateFormatter),
            lastBook = lastRecord?.let { _uiState.bookInformationMap[it.bookId] },
            lastSeenTime = lastRecord?.lastSeen?.format(dateFormatter)
        )
        println("OK COMP ${_uiState.selectedDateDetails}")
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

        val readingTimes = localDateList.map { dateTotalTimeMap[it] ?: 0 }
        val thresholds = readingTimes.filter { it > 0 }.run {
            if (isEmpty()) listOf(0, 0, 0) else listOf(
                quickSelect(this, 0.25),
                quickSelect(this, 0.5),
                quickSelect(this, 0.75)
            )
        }
        _uiState.thresholds = thresholds[2]

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