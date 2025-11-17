package com.miraimagiclab.novelreadingapp.ui.home.reading.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.miraimagiclab.novelreadingapp.data.local.room.entity.BookRecordEntity
import io.lain4504.novelreadingapp.api.book.BookInformation
import java.time.LocalDate

interface StatsOverviewUiState {
    var isLoading: Boolean
    var selected: Boolean
    var selectedDate: LocalDate
    val thresholds: Int
    val startDate: LocalDate
    var dateLevelMap: Map<LocalDate, Level>
    var totalRecordEntity: BookRecordEntity?
    var bookRecordsByDate: Map<LocalDate, List<BookRecordEntity>>
    val bookInformationMap: Map<String, BookInformation>
    val selectedDateDetails: DailyDateDetails?
}

class MutableStatisticsOverviewUiState : StatsOverviewUiState {
    override var isLoading: Boolean by mutableStateOf(true)
    override var selected: Boolean by mutableStateOf(false)
    override var selectedDate: LocalDate by mutableStateOf(LocalDate.now())
    override var thresholds: Int by mutableIntStateOf(0)
    override val startDate: LocalDate by mutableStateOf(LocalDate.now().minusMonths(6))
    override var dateLevelMap: Map<LocalDate, Level> by mutableStateOf(emptyMap())
    override var totalRecordEntity: BookRecordEntity? by mutableStateOf(null)
    override var bookRecordsByDate: Map<LocalDate, List<BookRecordEntity>> by mutableStateOf(emptyMap())
    override var bookInformationMap = mutableStateMapOf<String, BookInformation>()
    override var selectedDateDetails: DailyDateDetails? by mutableStateOf(null)
}
