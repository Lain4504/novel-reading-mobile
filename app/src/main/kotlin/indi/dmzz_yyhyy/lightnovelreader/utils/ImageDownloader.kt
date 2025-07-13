package indi.dmzz_yyhyy.lightnovelreader.utils

import android.util.Log
import androidx.work.ListenableWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class ImageDownloader(
    private val tasks: List<Task>,
    val onProgress: (Int, Int) -> Unit,
) {
    var count = 0
        private set

    data class Task(val file: File, val url: String)

    suspend fun run(): ListenableWorker.Result = withContext(Dispatchers.IO) {
        Log.i("ImageDownloader", "total tasks: ${tasks.size}")
        tasks.forEach {task ->
            val image = getImageFromNetByUrl(task.url) ?: return@withContext ListenableWorker.Result.failure()
            writeImageToDisk(image, task.file)
            count++
            onProgress(count, tasks.size)
            Log.i("ImageDownloader", "tasks: ${count}/${tasks.size}")
        }
        return@withContext ListenableWorker.Result.success()
    }

    private fun writeImageToDisk(data: ByteArray, file: File) {
        try {
            val fileParent = file.parentFile
            if (fileParent != null) {
                if (!fileParent.exists()) {
                    fileParent.mkdirs()
                    file.createNewFile()
                }
            }
            val fops = FileOutputStream(file)
            fops.write(data)
            fops.flush()
            fops.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun getImageFromNetByUrl(strUrl: String): ByteArray? {
        try {
            val url = URL(strUrl)
            val conn = withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5 * 1000
            val inStream = conn.inputStream
            val btData = readInputStream(inStream)
            return btData
        } catch (e: Exception) {
            e.printStackTrace()
            println(strUrl)
        }
        return null
    }

    private fun readInputStream(inStream: InputStream): ByteArray {
        val outStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int
        while ((inStream.read(buffer).also { len = it }) != -1) {
            outStream.write(buffer, 0, len)
        }
        inStream.close()
        return outStream.toByteArray()
    }
}
