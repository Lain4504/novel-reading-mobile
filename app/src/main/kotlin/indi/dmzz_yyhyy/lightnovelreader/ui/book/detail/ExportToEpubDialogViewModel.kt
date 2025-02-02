package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.work.ExportBookToEPUBWork
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ExportToEpubDialogViewModel @Inject constructor(
    private val workManager: WorkManager,
) : ViewModel() {
    fun exportToEpub(uri: Uri, bookId: Int, title: String): Flow<WorkInfo?> {
        val workRequest = OneTimeWorkRequestBuilder<ExportBookToEPUBWork>()
            .setInputData(
                workDataOf(
                    "bookId" to bookId,
                    "uri" to uri.toString(),
                    "title" to title
                )
            )
            .build()
        workManager.enqueueUniqueWork(
            bookId.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workManager.getWorkInfoByIdFlow(workRequest.id)
    }
}