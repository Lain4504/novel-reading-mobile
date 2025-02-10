package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.data.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@HiltWorker
class CacheBookWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val localBookDataSource: LocalBookDataSource,
    private val webBookDataSource: WebBookDataSource,
    private val bookRepository: BookRepository
) : Worker(appContext, workerParams) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun doWork(): Result {
        val bookId = inputData.getInt("bookId", -1)
        if (bookId < 0) return Result.failure()
        val progressFlow = MutableSharedFlow<Int>(0)
        bookRepository.addCacheBookProgressFlow(bookId, progressFlow)
        var total = 0
        var count = 0
        webBookDataSource.getBookVolumes(bookId)?.let { bookVolumes ->
            total = bookVolumes.volumes.sumOf { it.chapters.size } + 1
            if (bookVolumes.volumes.isEmpty()) return Result.failure()
            localBookDataSource.updateBookVolumes(bookId, bookVolumes)
            bookVolumes.volumes.forEach { volume ->
                volume.chapters.map { it.id }.forEach { chapterId ->
                    localBookDataSource.updateChapterContent(
                        webBookDataSource.getChapterContent(
                            chapterId = chapterId,
                            bookId = bookId
                        ) ?: return Result.failure()
                    )
                    count ++
                    coroutineScope.launch {
                        progressFlow.emit(100*count/total)
                    }
                }
            }
        }
        webBookDataSource.getBookInformation(bookId)
            ?.let {
                if (it.isEmpty()) return Result.failure()
                localBookDataSource.updateBookInformation(it)
            }
        count ++
        coroutineScope.launch {
            progressFlow.emit(100*count/total)
        }
        coroutineScope.launch {
            progressFlow.emit(-1)
        }
        bookRepository.clearCacheBookProgressFlow(bookId)
        return Result.success()
    }

    override fun onStopped() {
        coroutineScope.cancel()
        super.onStopped()
    }
}