package com.miraimagiclab.novelreadingapp.data.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.miraimagiclab.novelreadingapp.data.web.WebBookDataSourceProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.miraimagiclab.novelreadingapp.R
import io.lain4504.novelreadingapp.api.book.BookInformation
import kotlinx.coroutines.delay

@HiltWorker
class CheckUpdateWork @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val webBookDataSourceProvider: WebBookDataSourceProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Bookshelf update checking removed - now handled via UserNovelInteraction follow notifications
        // This work can be removed or repurposed for other update checks
        Log.d("CheckUpdateWork", "Update check skipped - use UserNovelInteraction follow notifications instead")
        return Result.success()
    }

    @Suppress("UNUSED")
    private fun sendNotification(bookInformation: BookInformation) {
        // Stub - notification functionality removed
        // Use UserNovelInteraction follow notifications instead
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Thông báo cập nhật"
            val descriptionText = "Nhắc khi tiểu thuyết có chương mới"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("BookUpdate", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
