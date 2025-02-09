package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageDownloader
import io.nightfish.potatoepub.builder.EpubBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.time.LocalDateTime

@HiltWorker
class ExportBookToEPUBWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val webBookDataSource: WebBookDataSource
) : Worker(appContext, workerParams) {

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var notification: Notification? = null
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "BookEpubExport",
                "EPUB 导出进度",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateProgressNotification(bookId: Int, progress: Int) {
        notification = NotificationCompat.Builder(applicationContext, "BookEpubExport")
            .setContentTitle("导出『${inputData.getString("title")}』")
            .setContentText("已处理 ${progress}%")
            .setSmallIcon(R.drawable.file_export_24px)
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        notificationManager.notify(bookId, notification)
    }

    private fun showProgressNotification(bookId: Int) {
        notification = NotificationCompat.Builder(applicationContext, "BookEpubExport")
            .setContentTitle("导出『${inputData.getString("title")}』")
            .setContentText("准备中...")
            .setSmallIcon(R.drawable.file_export_24px)
            .setProgress(100, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        notificationManager.notify(bookId, notification)
    }

    private fun updateFailureNotification(bookId: Int) {
        notification = NotificationCompat.Builder(applicationContext, "BookEpubExport")
            .setContentTitle("导出『${inputData.getString("title")}』")
            .setContentText("导出失败")
            .setSmallIcon(R.drawable.file_export_24px)
            .setProgress(0, 0, false)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(bookId, notification)
    }

    private fun updateCompletionNotification(bookId: Int) {
        notification = NotificationCompat.Builder(applicationContext, "BookEpubExport")
            .setContentTitle("导出『${inputData.getString("title")}』")
            .setContentText("已成功完成")
            .setSmallIcon(R.drawable.file_export_24px)
            .setProgress(0, 0, false)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(bookId, notification)
    }

    override fun doWork(): Result {
        createNotificationChannel()
        val bookId = inputData.getInt("bookId", -1)
        showProgressNotification(bookId)
        val fileUri = inputData.getString("uri")?.let(Uri::parse) ?: return Result.failure()
        val tempDir = applicationContext.cacheDir.resolve("epub").resolve(bookId.toString())
        val cover = tempDir.resolve("cover.jpg")
        if (bookId < 0) {
            updateFailureNotification(bookId)
            return Result.failure()
        }

        val tasks = mutableListOf<ImageDownloader.Task>()
        val epub = EpubBuilder().apply {
            val bookInformation = webBookDataSource.getBookInformation(bookId) ?: return Result.failure().also {
                updateFailureNotification(bookId)
            }
            val bookVolumes = webBookDataSource.getBookVolumes(bookId) ?: return Result.failure().also {
                updateFailureNotification(bookId)
            }
            val bookContentMap = mutableMapOf<Int, ChapterContent>()

            updateProgressNotification(bookId, 0)
            val volumesCount = bookVolumes.volumes.size
            var currentVolumeIndex1 = 0

            bookVolumes.volumes.forEach { volume ->
                currentVolumeIndex1++
                val progressForVolume = (20 * currentVolumeIndex1) / volumesCount
                updateProgressNotification(bookId, progressForVolume)
                volume.chapters.forEach {
                    bookContentMap[it.id] = webBookDataSource.getChapterContent(it.id, bookId) ?: return Result.failure().also {
                        updateFailureNotification(bookId, )
                    }
                }
            }

            title = bookInformation.title
            modifier = LocalDateTime.now()
            creator = bookInformation.author
            description = bookInformation.description
            publisher = bookInformation.publishingHouse
            updateProgressNotification(bookId, 20)


            var currentVolumeIndex = 0
            updateProgressNotification(bookId, 20)

            bookVolumes.volumes.forEach { volume ->
                chapter {
                    title(volume.volumeTitle)
                    volume.chapters.forEach {
                        chapter {
                            title(it.title)
                            content {
                                bookContentMap[it.id]!!.content.split("[image]").filter { it.isNotEmpty() }.forEach { singleText ->
                                    if (singleText.startsWith("http://") || singleText.startsWith("https://")) {
                                        val image = tempDir.resolve(singleText.hashCode().toString() + ".jpg")
                                        tasks.add(ImageDownloader.Task(image, singleText))
                                        image(image)
                                    } else {
                                        singleText.split("\n").forEach {
                                            text(it)
                                            br()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                currentVolumeIndex++
                val progressForVolume = (30 * currentVolumeIndex) / volumesCount
                updateProgressNotification(bookId, 20 + progressForVolume)
            }
            tasks.add(ImageDownloader.Task(cover, bookInformation.coverUrl))
            cover(cover)
        }

        val imageDownloader = ImageDownloader(
            tasks = tasks,
            coroutineScope = coroutineScope,
            onProgress = { current, total ->
                val progress = (50 + current.toFloat() / total * 40).toInt()
                updateProgressNotification(bookId, progress)
            },
            onFinished = {
                coroutineScope.launch {
                    updateProgressNotification(bookId, 90)

                    val file = tempDir.resolve("epub")
                    epub.build().save(file)

                    updateProgressNotification(bookId, 95)
                    applicationContext.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                        FileInputStream(file).use { inputStream ->
                            val buffer = ByteArray(1024 * 1024) // = 1MB
                            var bytesRead: Int
                            var totalBytes = 0L
                            val fileSize = file.length()

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytes += bytesRead
                                if (fileSize > 0) {
                                    val writeProgress = 95 + (totalBytes.toFloat() / fileSize * 5).toInt()
                                    updateProgressNotification(bookId, writeProgress)
                                }
                            }
                        }
                    }
                    tempDir.delete()
                }
            }
        )
        while (!imageDownloader.isDone) {
            Thread.sleep(500)
        }
        updateCompletionNotification(bookId)
        return Result.success()
    }


    override fun onStopped() {
        super.onStopped()
        coroutineScope.cancel()
    }
}
