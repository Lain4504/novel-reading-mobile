package com.miraimagiclab.novelreadingapp.ui.home.settings

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.miraimagiclab.novelreadingapp.data.bookshelf.BookshelfRepository
import com.miraimagiclab.novelreadingapp.data.local.LocalBookDataSource
import com.miraimagiclab.novelreadingapp.data.statistics.StatsRepository
import com.miraimagiclab.novelreadingapp.data.userdata.UserDataRepository
import com.miraimagiclab.novelreadingapp.data.web.WebBookDataSourceManager
import com.miraimagiclab.novelreadingapp.data.web.WebBookDataSourceProvider
import com.miraimagiclab.novelreadingapp.data.work.ExportDataWork
import com.miraimagiclab.novelreadingapp.data.work.ImportDataWork
import com.miraimagiclab.novelreadingapp.ui.components.ExportContext
import com.miraimagiclab.novelreadingapp.ui.components.MutableExportContext
import dagger.hilt.android.lifecycle.HiltViewModel
import io.lain4504.novelreadingapp.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltViewModel
class SourceChangeDialogViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val workManager: WorkManager,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val localBookDataSource: LocalBookDataSource,
    private val bookshelfRepository: BookshelfRepository,
    private val statsRepository: StatsRepository,
    webBookDataSourceManager: WebBookDataSourceManager
) : ViewModel() {

    val webBookDataSourceId = webBookDataSourceProvider.value.id

    val webDataSourceItems = webBookDataSourceManager.webDataSourceItems

    private fun exportToFile(uri: Uri, exportContext: ExportContext): OneTimeWorkRequest {
        val workRequest = OneTimeWorkRequestBuilder<ExportDataWork>()
            .setInputData(
                workDataOf(
                    "uri" to uri.toString(),
                    "exportBookshelf" to exportContext.bookshelf,
                    "exportReadingData" to exportContext.readingData,
                    "exportSetting" to exportContext.settings,
                    "exportBookmark" to exportContext.bookmark,
                )
            )
            .build()
        workManager.enqueueUniqueWork(
            uri.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workRequest
    }

    private fun importFromFile(uri: Uri): OneTimeWorkRequest {
        val workRequest = OneTimeWorkRequestBuilder<ImportDataWork>()
            .setInputData(
                workDataOf(
                    "uri" to uri.toString(),
                    "ignoreDataIdCheck" to true
                )
            )
            .build()
        workManager.enqueueUniqueWork(
            uri.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workRequest
    }

    fun changeWebSource(webDataSourceId: Int, fileDir: File) {
        if (webDataSourceId == webBookDataSourceId) return

        CoroutineScope(Dispatchers.IO).launch {
            val oldUri = File(fileDir, "${webBookDataSourceProvider.value.id}.data.lnr").toUri()
            val exportRequest = exportToFile(oldUri, MutableExportContext().apply { settings = false })

            try {
                workManager.getWorkInfoByIdFlow(exportRequest.id).collect { workInfo ->
                    when (workInfo?.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            performDataClearAndImport(webDataSourceId, fileDir, oldUri)
                            cancel()
                        }
                        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                            Log.e("SourceChange", "Export failed, aborting")
                            cancel()
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("SourceChange", "Export failed", e)
            }
        }
    }

    private suspend fun performDataClearAndImport(
        newSourceId: Int,
        fileDir: File,
        fallbackUri: Uri
    ) {
        val oldSourceId = webBookDataSourceId
        try {
            localBookDataSource.clear()
            bookshelfRepository.clear()
            userDataRepository.remove(UserDataPath.ReadingBooks.path)
            userDataRepository.remove(UserDataPath.Search.History.path)
            userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).set(newSourceId)
            statsRepository.clear()

            val newFile = File(fileDir, "$newSourceId.data.lnr")
            if (!newFile.exists()) {
                restartApp()
                return
            }

            val importRequest = importFromFile(newFile.toUri())
            workManager.getWorkInfoByIdFlow(importRequest.id).collect { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        Log.i("SourceChange", "All operations completed, restarting")
                        restartApp()
                    }
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                        Log.e("SourceChange", "Import failed. Attempting rollback")
                        restoreFallbackData(fallbackUri, oldSourceId)
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            Log.e("SourceChange", "Error during import", e)
            restoreFallbackData(fallbackUri, oldSourceId)
        }
    }

    private suspend fun restoreFallbackData(uri: Uri, fallbackSourceId: Int) {
        try {
            userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).set(fallbackSourceId)
            val restoreRequest = importFromFile(uri)
            workManager.getWorkInfoByIdFlow(restoreRequest.id).collect { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    Log.i("SourceChange", "Rollback succeeded, restarting")
                    restartApp()
                } else if (workInfo?.state == WorkInfo.State.FAILED) {
                    Log.e("SourceChange", "Rollback failed.")
                }
            }
        } catch (e: Exception) {
            Log.e("SourceChange", "Rollback import failed with exception", e)
        }
    }

    private fun restartApp() {
        exitProcess(0)
    }
}
