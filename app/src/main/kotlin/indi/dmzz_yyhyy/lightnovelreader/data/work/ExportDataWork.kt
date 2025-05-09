package indi.dmzz_yyhyy.lightnovelreader.data.work

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.Bookshelf
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfBookMetadata
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.json.AppUserDataJsonBuilder
import indi.dmzz_yyhyy.lightnovelreader.data.json.toJsonData
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserDataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@HiltWorker
class ExportDataWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val webBookDataSource: WebBookDataSource,
    private val bookshelfRepository: BookshelfRepository,
    private val bookRepository: BookRepository,
    private val statsRepository: StatsRepository,
    private val userDataDao: UserDataDao
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
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

    private fun buildJson(
        exportBookshelf: Boolean,
        exportReadingData: Boolean,
        exportSetting: Boolean
    ): String {
        return AppUserDataJsonBuilder()
            .data {
                webDataSourceId(webBookDataSource.id)
                if (exportBookshelf) {
                    bookshelfRepository.getAllBookshelfIds()
                        .mapNotNull { (bookshelfRepository.getBookshelf(it)) }
                        .map { (it as Bookshelf).toJsonData() }
                        .forEach(::bookshelf)
                    bookshelfRepository.getAllBookshelfBooksMetadata()
                        .map(BookshelfBookMetadata::toJsonData)
                        .forEach(::bookshelfBookMetaData)
                }
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