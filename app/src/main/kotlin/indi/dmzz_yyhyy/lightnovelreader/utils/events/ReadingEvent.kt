package indi.dmzz_yyhyy.lightnovelreader.utils.events

import indi.dmzz_yyhyy.lightnovelreader.data.statistics.ReadingStatsUpdate
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

sealed class ReadingEvent {
    data class SessionStart(
        val bookId: Int, val currentTime: LocalTime = LocalTime.now()
    ): ReadingEvent()
    data class BookStarted(val bookId: Int) : ReadingEvent()
    data class BookFinished(val bookId: Int) : ReadingEvent()
    data class BookFavorite(val bookId: Int) : ReadingEvent()
    data class ReadingSpeed(val bookId: Int, val speed: Int): ReadingEvent()
}

@Singleton
class ReadingEventHandler @Inject constructor(
    private val statsRepository: StatsRepository
) {
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _events = MutableSharedFlow<ReadingEvent>()
    private val events = _events.asSharedFlow()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            events.collect { event ->
                when (event) {
                    is ReadingEvent.SessionStart -> handleSessionStart(event)
                    is ReadingEvent.BookStarted -> handleBookStarted(event)
                    is ReadingEvent.BookFinished -> handleBookFinished(event)
                    is ReadingEvent.ReadingSpeed -> handleReadingSpeed(event)
                    is ReadingEvent.BookFavorite -> handleBookFavorite(event)
                }
            }
        }
    }

    suspend fun sendEvent(event: ReadingEvent) {
        _events.emit(event)
    }

    private suspend fun handleSessionStart(event: ReadingEvent.SessionStart) {
        if (event.bookId < 1) return
        event.bookId.let { bookId ->
            statsRepository.updateReadingStatistics(
                ReadingStatsUpdate(
                    bookId = bookId,
                    sessionDelta = 1,
                    localTime = event.currentTime,
                )
            )
        }
    }

    private fun handleBookStarted(event: ReadingEvent.BookStarted) {
        coroutineScope.launch {
            println("EVENT START book ${event.bookId}")
            statsRepository.updateBookStatus(
                bookId = event.bookId,
                isFirstReading = true
            )
        }
    }

    private fun handleBookFinished(event: ReadingEvent.BookFinished) {
        coroutineScope.launch {
            statsRepository.updateBookStatus(
                bookId = event.bookId,
                isFinishedReading = true
            )
        }
    }

    private fun handleBookFavorite(event: ReadingEvent.BookFavorite) {
        println("EVENT FAV book ${event.bookId}")
        coroutineScope.launch {
            statsRepository.updateBookStatus(
                bookId = event.bookId,
                isFavorite = true
            )
        }
    }

    private fun handleReadingSpeed(event: ReadingEvent.ReadingSpeed) {
        coroutineScope.launch {
            statsRepository.updateReadingStatistics(
                ReadingStatsUpdate(
                    bookId = event.bookId,
                    currentSpeed = event.speed
                )
            )
        }
    }
}