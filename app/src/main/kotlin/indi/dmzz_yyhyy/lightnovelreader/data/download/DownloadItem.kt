package indi.dmzz_yyhyy.lightnovelreader.data.download

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime

interface DownloadItem {
    val type: DownloadType
    val bookId: Int
    val startTime: LocalDateTime
    val progress: Float
}

@Stable
class MutableDownloadItem(
    override val type: DownloadType,
    override val bookId: Int,
    override val startTime: LocalDateTime = LocalDateTime.now()
): DownloadItem {
    override var progress by mutableFloatStateOf(0f)

    override fun equals(other: Any?): Boolean {
        return if (other is DownloadItem) other.type == this.type && other.bookId == this.bookId else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + bookId
        result = 31 * result + startTime.hashCode()
        return result
    }

    override fun toString(): String {
        return "MutableDownloadItem{type=$type, bookId=$bookId, startTimeMillis=$startTime, progress=$progress}"
    }
}