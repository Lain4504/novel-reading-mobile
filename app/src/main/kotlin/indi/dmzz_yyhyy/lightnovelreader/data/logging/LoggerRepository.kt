package indi.dmzz_yyhyy.lightnovelreader.data.logging

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.buildReportHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class LoggerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDataRepository: UserDataRepository,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var logLevel: LogLevel = LogLevel.NONE

    private val _logEntries = mutableStateListOf<LogEntry>()
    val logEntries: List<LogEntry> = _logEntries
    private var loggingJob: Job? = null
    private val currentPid: Int = Process.myPid()

    init {
        startLogging()
    }

    fun startLogging() {
        if (loggingJob?.isActive == true) return
        logLevel = LogLevel.from(
            userDataRepository.stringUserData(UserDataPath.Settings.Data.LogLevel.path)
                .getOrDefault("none")
        )
        if (logLevel == LogLevel.NONE) return
        Log.i("Logger", "----- new session")

        loggingJob = coroutineScope.launch {
            Runtime.getRuntime().exec("logcat -c").waitFor()

            val process = Runtime.getRuntime().exec("logcat -v threadtime --pid=$currentPid")
            val reader = process.inputStream.bufferedReader()

            try {
                while (isActive) {
                    val line = reader.readLine() ?: break
                    if (line.isBlank()) continue

                    parseLine(line)?.let {
                        _logEntries += it
                        if (_logEntries.size > 1000) {
                            _logEntries.removeAt(0)
                        }
                    }
                }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                reader.close()
                process.destroy()
            }
        }
    }

    private fun parseLine(line: String): LogEntry? {
        val parts = line.split("\\s+".toRegex())
        if (parts.size < 5) return null

        val priority = when (parts[4].firstOrNull()?.uppercaseChar()) {
            'V' -> LogLevel.VERBOSE
            'D' -> LogLevel.DEBUG
            'I' -> LogLevel.INFO
            'W' -> LogLevel.WARNING
            'E', 'F' -> LogLevel.ERROR
            else -> return null
        }

        if (priority.level > logLevel.level) return null

        return LogEntry(text = line, logLevel = priority)
    }

    fun refreshLogs() {
        stopLogging()
        _logEntries.clear()
        startLogging()
    }

    fun shareLogs() {
        val logText = buildString {
            append(buildReportHeader())
            append("----- beginning of logs\n\n")
            append(_logEntries.joinToString("\n") { it.text })
        }

        val file = File(context.cacheDir, "lnr_logs_${System.currentTimeMillis()}.txt").apply {
            writeText(logText)
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setType("*/*")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

    }

    fun stopLogging() {
        loggingJob?.cancel()
        loggingJob = null
    }
}

