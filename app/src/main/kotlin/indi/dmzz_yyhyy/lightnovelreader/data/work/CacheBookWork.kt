package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.content.Context
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadProgressRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadType
import indi.dmzz_yyhyy.lightnovelreader.data.download.MutableDownloadItem
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

@HiltWorker
class CacheBookWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val localBookDataSource: LocalBookDataSource,
    private val webBookDataSource: WebBookDataSource,
    private val downloadProgressRepository: DownloadProgressRepository
) : Worker(appContext, workerParams) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun doWork(): Result {
        val bookId = inputData.getInt("bookId", -1)
        if (bookId < 0) return Result.failure()
        val downloadItem = MutableDownloadItem(DownloadType.CACHE, bookId)
        downloadProgressRepository.addExportItem(downloadItem)
        var total: Int
        var count = 0
        webBookDataSource.getBookVolumes(bookId)?.let { bookVolumes ->
            total = bookVolumes.volumes.sumOf { it.chapters.size } + 1
            if (bookVolumes.volumes.isEmpty()) return Result.failure()
            localBookDataSource.updateBookVolumes(bookId, bookVolumes)
            bookVolumes.volumes.forEach { volume ->
                volume.chapters.map { it.id }.forEach { chapterId ->
                    val chapter = webBookDataSource.getChapterContent(
                        chapterId = chapterId,
                        bookId = bookId
                    ) ?: return Result.failure()
                    if (chapter.isEmpty()) return Result.failure().also { Toast.makeText(applicationContext, "缓存失败，请检查网络环境", Toast.LENGTH_SHORT).show() }
                    localBookDataSource.updateChapterContent(chapter)
                    count ++
                    downloadItem.progress = count.toFloat()/total
                }
            }
        }
        webBookDataSource.getBookInformation(bookId)
            ?.let {
                if (it.isEmpty()) return Result.failure()
                localBookDataSource.updateBookInformation(it)
            }
        count ++
        downloadItem.progress = 1f
        return Result.success()
    }

    override fun onStopped() {
        coroutineScope.cancel()
        super.onStopped()
    }
}