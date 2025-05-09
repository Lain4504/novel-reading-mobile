package indi.dmzz_yyhyy.lightnovelreader.data.book

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime

interface UserReadingData {
    val id: Int
    val lastReadTime: LocalDateTime
    val totalReadTime: Int
    val readingProgress: Float
    val lastReadChapterId: Int
    val lastReadChapterTitle: String
    val lastReadChapterProgress: Float
    val readCompletedChapterIds: List<Int>

    companion object {
        fun empty(): UserReadingData = MutableUserReadingData(
            -1,
            LocalDateTime.MIN,
            -1,
            0.0f,
            -1,
            "",
            0.0f,
            readCompletedChapterIds = emptyList()
        )
    }

    fun toMutable(): MutableUserReadingData = MutableUserReadingData(id, lastReadTime, totalReadTime, readingProgress, lastReadChapterId, lastReadChapterTitle, lastReadChapterProgress, readCompletedChapterIds)
}

class MutableUserReadingData(
    id: Int,
    lastReadTime: LocalDateTime,
    totalReadTime: Int,
    readingProgress: Float,
    lastReadChapterId: Int,
    lastReadChapterTitle: String,
    lastReadChapterProgress: Float,
    readCompletedChapterIds: List<Int>
): UserReadingData {
    override var id by mutableIntStateOf(id)
    override var lastReadTime by mutableStateOf(lastReadTime)
    override var totalReadTime by mutableIntStateOf(totalReadTime)
    override var readingProgress by mutableFloatStateOf(readingProgress)
    override var lastReadChapterId by mutableIntStateOf(lastReadChapterId)
    override var lastReadChapterTitle by mutableStateOf(lastReadChapterTitle)
    override var lastReadChapterProgress by mutableFloatStateOf(lastReadChapterProgress)
    override var readCompletedChapterIds = mutableStateListOf<Int>().apply { addAll(readCompletedChapterIds) }
    
    companion object {
        fun empty(): MutableUserReadingData = MutableUserReadingData(
            -1,
            LocalDateTime.MIN,
            -1,
            0.0f,
            -1,
            "",
            0.0f,
            readCompletedChapterIds = emptyList()
        )
    }
    
    fun update(userReadingData: UserReadingData) {
        this.id = userReadingData.id
        this.lastReadTime = userReadingData.lastReadTime
        this.totalReadTime = userReadingData.totalReadTime
        this.readingProgress = userReadingData.readingProgress
        this.lastReadChapterId = userReadingData.lastReadChapterId
        this.lastReadChapterTitle = userReadingData.lastReadChapterTitle
        this.lastReadChapterProgress = userReadingData.lastReadChapterProgress
        this.readCompletedChapterIds.clear()
        this.readCompletedChapterIds.addAll(userReadingData.readCompletedChapterIds)
    }
}
