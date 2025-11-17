package com.miraimagiclab.novelreadingapp.data.work

import android.content.Context
import android.os.Looper
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.miraimagiclab.novelreadingapp.data.download.DownloadProgressRepository
import com.miraimagiclab.novelreadingapp.data.download.DownloadType
import com.miraimagiclab.novelreadingapp.data.download.MutableDownloadItem
import com.miraimagiclab.novelreadingapp.data.local.LocalBookDataSource
import com.miraimagiclab.novelreadingapp.data.web.WebBookDataSourceProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CacheBookWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val localBookDataSource: LocalBookDataSource,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val downloadProgressRepository: DownloadProgressRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val bookId = inputData.getString("bookId") ?: return Result.failure()
        if (bookId.isBlank()) return Result.failure()
        val downloadItem = MutableDownloadItem(DownloadType.CACHE, bookId)
        downloadProgressRepository.addExportItem(downloadItem)
        var count = 0
        val bookVolumes = webBookDataSourceProvider.value.getBookVolumes(bookId)
        val total = bookVolumes.volumes.sumOf { it.chapters.size } + 1
        if (bookVolumes.volumes.isEmpty()) return Result.failure()
        localBookDataSource.updateBookVolumes(bookId, bookVolumes)
        bookVolumes.volumes.forEach { volume ->
            volume.chapters.map { it.id }.forEach { chapterId ->
                val chapter = webBookDataSourceProvider.value.getChapterContent(
                    chapterId = chapterId,
                    bookId = bookId
                )
                if (chapter.isEmpty()) return Result.failure().also {
                    Looper.prepare()
                    Toast.makeText(applicationContext, "缓存失败，请检查网络环境", Toast.LENGTH_SHORT).show()
                    Looper.loop()
                }
                localBookDataSource.updateChapterContent(chapter)
                count ++
                downloadItem.progress = count.toFloat() / total
            }
        }
        webBookDataSourceProvider.value.getBookInformation(bookId)
            .let {
                if (it.isEmpty()) return Result.failure()
                localBookDataSource.updateBookInformation(it)
            }
        downloadItem.progress = 1f
        return Result.success()
    }
}