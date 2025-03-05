package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import java.time.LocalDate

interface StatsOverviewUiState {
    var isLoading: Boolean
    var selected: Boolean
    var selectedDate: LocalDate
    var dateLevelMap: Map<LocalDate, Level>
    var dateReadingTimeMap: Map<LocalDate, Int>
    var dateStatsEntityMap: Map<LocalDate, ReadingStatisticsEntity>
    var totalStartedBooks: Int
    var totalReadInSeconds: Int
    var totalSessions: Int
    var bookRecordsByBookId: Map<Int, List<BookRecordEntity>>
    var bookRecordsByDate: Map<LocalDate, List<BookRecordEntity>>
}

class MutableStatisticsOverviewUiState : StatsOverviewUiState {
    override var isLoading: Boolean by mutableStateOf(false)
    override var selected: Boolean by mutableStateOf(false)
    override var selectedDate: LocalDate by mutableStateOf(LocalDate.now())
    override var dateLevelMap: Map<LocalDate, Level> by mutableStateOf(emptyMap())
    override var dateReadingTimeMap: Map<LocalDate, Int> by mutableStateOf(emptyMap())
    override var dateStatsEntityMap: Map<LocalDate, ReadingStatisticsEntity> by mutableStateOf(emptyMap())
    override var totalStartedBooks: Int by mutableIntStateOf(0)
    override var totalReadInSeconds: Int by mutableIntStateOf(0)
    override var totalSessions: Int by mutableIntStateOf(0)
    override var bookRecordsByBookId: Map<Int, List<BookRecordEntity>> by mutableStateOf(emptyMap())
    override var bookRecordsByDate: Map<LocalDate, List<BookRecordEntity>> by mutableStateOf(emptyMap())
}
