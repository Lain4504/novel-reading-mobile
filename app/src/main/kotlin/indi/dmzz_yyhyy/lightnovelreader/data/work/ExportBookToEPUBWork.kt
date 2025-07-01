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
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadProgressRepository
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadType
import indi.dmzz_yyhyy.lightnovelreader.data.download.MutableDownloadItem
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
    private val webBookDataSource: WebBookDataSource,
    private val downloadProgressRepository: DownloadProgressRepository
) : Worker(appContext, workerParams) {

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var notification: Notification? = null
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "BookEpubExport",
                "EPUB 导出进度",
                NotificationManager.IMPORTANCE_HIGH
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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setProgress(0, 0, false)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(bookId, notification)
    }

    private fun updateCompletionNotification(bookId: Int) {
        notification = NotificationCompat.Builder(applicationContext, "BookEpubExport")
            .setContentTitle("导出『${inputData.getString("title")}』")
            .setContentText("已成功完成")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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
        val downloadItem = MutableDownloadItem(DownloadType.EPUB_EXPORT, bookId)
        downloadProgressRepository.addExportItem(downloadItem)
        if (bookId < 0) {
            downloadItem.progress = -1f
            updateFailureNotification(bookId)
            return Result.failure()
        }
        val tasks = mutableListOf<ImageDownloader.Task>()
        val epub = EpubBuilder().apply {
            val bookInformation = webBookDataSource.getBookInformation(bookId)
            if (bookInformation.isEmpty()) {
                downloadItem.progress = -1f
                updateFailureNotification(bookId)
                return Result.failure()
            }
            val bookVolumes = webBookDataSource.getBookVolumes(bookId)
            if (bookVolumes.isEmpty()) {
                downloadItem.progress = -1f
                updateFailureNotification(bookId)
                return Result.failure()
            }
            val bookContentMap = mutableMapOf<Int, ChapterContent>()
            updateProgressNotification(bookId, 0)
            downloadItem.progress = 0f
            val volumesCount = bookVolumes.volumes.size
            var currentVolumeIndex1 = 0

            bookVolumes.volumes.forEach { volume ->
                currentVolumeIndex1++
                val progressForVolume = (20 * currentVolumeIndex1) / volumesCount
                updateProgressNotification(bookId, progressForVolume)
                downloadItem.progress = progressForVolume / 100f
                volume.chapters.forEach {
                    val chapterContent = webBookDataSource.getChapterContent(it.id, bookId)
                    if (chapterContent.isEmpty()) {
                        downloadItem.progress = -1f
                        updateFailureNotification(bookId)
                        return Result.failure()
                    }
                    bookContentMap[it.id] = webBookDataSource.getChapterContent(it.id, bookId)
                }
            }

            title = bookInformation.title
            modifier = LocalDateTime.now()
            creator = bookInformation.author
            description = bookInformation.description
            publisher = bookInformation.publishingHouse
            updateProgressNotification(bookId, 20)
            downloadItem.progress = 0.2f


            var currentVolumeIndex = 0
            updateProgressNotification(bookId, 20)
            downloadItem.progress = 0.2f

            bookVolumes.volumes.forEach { volume ->
                chapter {
                    title(volume.volumeTitle)
                    volume.chapters.forEach {
                        chapter {
                            title(it.title)
                            content {
                                bookContentMap[it.id]!!.content.replace("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "").split("[image]").filter { it.isNotEmpty() }.forEach { singleText ->
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
                downloadItem.progress = (20f + progressForVolume) / 100f
            }
            tasks.add(ImageDownloader.Task(cover, bookInformation.coverUrl))
            cover(cover)
        }

        var result = Result.success()

        val imageDownloader = ImageDownloader(
            tasks = tasks,
            coroutineScope = coroutineScope,
            onProgress = { current, total ->
                val progress = (50 + current.toFloat() / total * 40).toInt()
                updateProgressNotification(bookId, progress)
                downloadItem.progress = progress / 100f
            },
            onFinished = {
                coroutineScope.launch {
                    updateProgressNotification(bookId, 90)
                    downloadItem.progress = 90f
                    downloadItem.progress = 0.90f

                    val file = tempDir.resolve("epub")
                    try {
                        epub.build().save(file)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        updateFailureNotification(bookId)
                        result = Result.failure()
                        downloadItem.progress = -1f
                        return@launch
                    }
                    updateProgressNotification(bookId, 95)
                    downloadItem.progress = 0.95f
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
                                    downloadItem.progress = writeProgress / 100f
                                }
                            }
                        }
                    }
                    tempDir.delete()
                    updateCompletionNotification(bookId)
                }
            }
        )
        while (!imageDownloader.isDone) {
            Thread.sleep(500)
        }
        return result.also {
            if (it == Result.success())
                updateCompletionNotification(bookId)
        }
    }


    override fun onStopped() {
        super.onStopped()
        coroutineScope.cancel()
    }
}
