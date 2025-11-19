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
import com.miraimagiclab.novelreadingapp.data.bookshelf.BookshelfRepository
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
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val bookshelfRepository: BookshelfRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val reminderBookMap = mutableMapOf<String, BookInformation>()
        bookshelfRepository.getAllBookshelfBooksMetadata().forEach { bookshelfBookMetadata ->
            delay(3000)
            if (bookshelfBookMetadata.bookShelfIds.all {
                bookshelfRepository.getBookshelf(it)?.systemUpdateReminder != true
            }) return@forEach
            Log.d("CheckUpdateWork", "Updating book id=${bookshelfBookMetadata.id}")
            val bookInformation = webBookDataSourceProvider.value.getBookInformation(bookshelfBookMetadata.id)
            val webBookLastUpdate = bookInformation.lastUpdated
            if (webBookLastUpdate.isAfter(bookshelfBookMetadata.lastUpdate)) {
                bookshelfBookMetadata.bookShelfIds.forEach {
                    bookshelfRepository.addUpdatedBooksIntoBookShelf(it, bookshelfBookMetadata.id)
                    val bookshelf = bookshelfRepository.getBookshelf(it)
                    if (bookshelf != null && bookshelf.systemUpdateReminder)
                        reminderBookMap[bookshelfBookMetadata.id] = bookInformation
                }
                bookshelfRepository.updateBookshelfBookMetadataLastUpdateTime(bookInformation.id, webBookLastUpdate)
            }
        }
        reminderBookMap.values.forEach {
            with(NotificationManagerCompat.from(appContext)) {
                if (ActivityCompat.checkSelfPermission(
                        appContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@forEach
                }
                createNotificationChannel()
                notify(
                    it.id.hashCode(),
                    NotificationCompat.Builder(appContext, "BookUpdate")
                        .setSmallIcon(R.drawable.icon_foreground)
                        .setContentTitle(appContext.getString(R.string.app_name))
                        .setContentText("Tiểu thuyết bạn theo dõi ${it.title} vừa cập nhật")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()
                )
            }
        }
        return Result.success()
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
