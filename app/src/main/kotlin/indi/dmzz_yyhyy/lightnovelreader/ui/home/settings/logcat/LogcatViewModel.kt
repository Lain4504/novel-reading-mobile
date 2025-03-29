package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LoggerRepository
import javax.inject.Inject

@HiltViewModel
class LogcatViewModel @Inject constructor (
    private val loggerRepository: LoggerRepository
): ViewModel() {
    val logEntries = loggerRepository.logEntries

    fun startLogging() {
        loggerRepository.startLogging()
        Log.i("Logger", "----- history")
    }

    @Suppress("unused")
    fun stopLogging() = loggerRepository.stopLogging()

    fun clearLogs() = loggerRepository.refreshLogs()
    fun shareLogs() = loggerRepository.shareLogs()
}