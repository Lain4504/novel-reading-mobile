package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class StatsDetailedViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStatsDetailedUiState()
    val uiState: StatsDetailedUiState = _uiState

    private val targetDateArg: LocalDate = LocalDate.now()

    fun initialize(targetDate: LocalDate) {
        println("RECV init with date $targetDate")
    }

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.isLoading = true
            updateDateRange()
            loadStatistics()
            _uiState.isLoading = false
        }
    }

    fun setSelectedView(index: Int) {
        _uiState.selectedViewIndex = index
        updateDateRange()
    }

    private fun updateDateRange() {
        _uiState.targetDateRange = when (_uiState.selectedViewIndex) {
            0 -> Pair(targetDateArg, targetDateArg)
            1 -> Pair(
                targetDateArg.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                targetDateArg.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            )
            2 -> Pair(
                targetDateArg.withDayOfMonth(1),
                targetDateArg.with(TemporalAdjusters.lastDayOfMonth())
            )
            else -> throw IllegalArgumentException("Invalid view index")
        }
    }

    private suspend fun loadStatistics() {
        val (start, end) = _uiState.targetDateRange
        val entities = statsRepository.getReadingEntitiesBetweenDates(start, end)
        _uiState.dateStatsMap = entities.associateBy { it.date }
    }
}
