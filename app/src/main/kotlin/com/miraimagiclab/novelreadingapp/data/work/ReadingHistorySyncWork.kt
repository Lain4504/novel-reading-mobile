package com.miraimagiclab.novelreadingapp.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.miraimagiclab.novelreadingapp.data.sync.ReadingHistorySyncService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReadingHistorySyncWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val readingHistorySyncService: ReadingHistorySyncService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val result = readingHistorySyncService.syncAllReadingHistory()
            result.fold(
                onSuccess = { syncedCount ->
                    android.util.Log.d("ReadingHistorySyncWork", "Synced $syncedCount reading histories")
                    Result.success()
                },
                onFailure = { e ->
                    android.util.Log.e("ReadingHistorySyncWork", "Failed to sync reading history", e)
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("ReadingHistorySyncWork", "Error in sync work", e)
            Result.retry()
        }
    }
}

