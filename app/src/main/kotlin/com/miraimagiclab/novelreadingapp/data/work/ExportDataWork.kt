package com.miraimagiclab.novelreadingapp.data.work

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.miraimagiclab.novelreadingapp.data.book.BookRepository
import com.miraimagiclab.novelreadingapp.data.json.AppUserDataJsonBuilder
import com.miraimagiclab.novelreadingapp.data.json.toJsonData
import com.miraimagiclab.novelreadingapp.data.local.room.dao.UserDataDao
import com.miraimagiclab.novelreadingapp.data.local.room.entity.UserDataEntity
import com.miraimagiclab.novelreadingapp.data.statistics.StatsRepository
import com.miraimagiclab.novelreadingapp.data.web.WebBookDataSourceProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.lain4504.novelreadingapp.api.book.UserReadingData
import io.lain4504.novelreadingapp.api.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@HiltWorker
class ExportDataWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val bookRepository: BookRepository,
    private val statsRepository: StatsRepository,
    private val userDataDao: UserDataDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val fileUri = inputData.getString("uri")?.let(Uri::parse) ?: return Result.failure()
        val exportBookshelf = inputData.getBoolean("exportBookshelf", false)
        val exportReadingData = inputData.getBoolean("exportReadingData",false)
        val exportSetting = inputData.getBoolean("exportSetting", false)

        return try {
            val json = buildJson(exportBookshelf, exportReadingData, exportSetting)
            createFile(fileUri, json)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun buildJson(
        exportBookshelf: Boolean,
        exportReadingData: Boolean,
        exportSetting: Boolean
    ): String = withContext(Dispatchers.IO) {
        return@withContext AppUserDataJsonBuilder()
            .data {
                webDataSourceId(webBookDataSourceProvider.value.id)
                // Bookshelf export removed - use UserNovelInteraction follow status instead
                if (exportReadingData) {
                    bookRepository.getAllUserReadingData()
                        .map(UserReadingData::toJsonData)
                        .forEach(::bookUserData)
                    userDataDao.getEntity(UserDataPath.ReadingBooks.path)
                        ?.toJsonData()
                        ?.let(::userData)
                    statsRepository.getAllReadingStats()
                        .forEach(::dailyReadingData)
                }
                if (exportSetting) {
                    userDataDao.getGroupValues(UserDataPath.Reader.path)
                        .map(UserDataEntity::toJsonData)
                        .forEach(::userData)
                    userDataDao.getGroupValues(UserDataPath.Settings.App.path)
                        .map(UserDataEntity::toJsonData)
                        .forEach(::userData)
                    userDataDao.getGroupValues(UserDataPath.Settings.Display.path)
                        .map(UserDataEntity::toJsonData)
                        .forEach(::userData)
                }
            }
            .build()
            .toJson()
    }

    private fun createFile(fileUri: Uri, json: String): Result {
        return try {
            File(applicationContext.filesDir, "data").apply { if (!exists()) mkdir() }
            if (fileUri.scheme.equals("file")) {
                val file = fileUri.toFile()
                if (file.exists()) file.delete()
                file.createNewFile()
            }
            applicationContext.contentResolver.openFileDescriptor(fileUri, "w")?.use { descriptor ->
                ZipOutputStream(FileOutputStream(descriptor.fileDescriptor)).use {
                    it.putNextEntry(ZipEntry("data.json"))
                    it.write(json.toByteArray())
                    it.closeEntry()
                }
            }
            Result.success()
        } catch (e: IOException) {
            e.printStackTrace()
            Result.failure()
        }
    }
}