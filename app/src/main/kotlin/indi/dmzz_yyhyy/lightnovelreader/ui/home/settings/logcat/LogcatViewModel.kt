package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LoggerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogcatViewModel @Inject constructor (
    private val loggerRepository: LoggerRepository
): ViewModel() {
    private val _uiState = MutableLogcatUiState()
    val uiState: LogcatUiState = _uiState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loggerRepository.logEntriesFlow.collect { newEntries ->
                _uiState.logEntries = newEntries
            }
        }
    }

    fun startLogging() {
        loggerRepository.startLogging()
        Log.i("Logger", "----- history")
    }

    @Suppress("unused")
    fun stopLogging() = loggerRepository.stopLogging()

    fun clearLogs() = loggerRepository.refreshLogs()
    fun shareLogs() = loggerRepository.shareLogs()
}