package com.miraimagiclab.novelreadingapp.data.sync

import android.util.Log
import com.miraimagiclab.novelreadingapp.data.book.BookRepository
import com.miraimagiclab.novelreadingapp.data.local.LocalBookDataSource
import com.miraimagiclab.novelreadingapp.data.userinteraction.UserNovelInteractionRepository
import com.miraimagiclab.novelreadingapp.data.userinteraction.ReadingHistoryData
import io.lain4504.novelreadingapp.api.book.BookVolumes
import io.lain4504.novelreadingapp.api.book.UserReadingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingHistorySyncService @Inject constructor(
    private val userNovelInteractionRepository: UserNovelInteractionRepository,
    private val bookRepository: BookRepository,
    private val localBookDataSource: LocalBookDataSource
) {
    companion object {
        private const val TAG = "ReadingHistorySync"
        private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    }

    /**
     * Push sync: Sync local reading progress to backend
     * Called when user reads a new chapter
     */
    suspend fun pushReadingProgress(
        novelId: String,
        chapterId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Get local reading data
            val localReadingData = localBookDataSource.getUserReadingData(novelId)
            
            // Find chapter number from volumes
            val chapterNumber = findChapterNumber(novelId, chapterId)
                ?: return@runCatching Result.failure<Unit>(
                    IllegalStateException("Chapter number not found for chapter $chapterId")
                )
            
            // Sync to backend
            val result = userNovelInteractionRepository.updateReadingProgress(
                novelId = novelId,
                chapterNumber = chapterNumber
            )
            
            result.fold(
                onSuccess = {
                    Log.d(TAG, "Successfully synced reading progress for novel $novelId, chapter $chapterNumber")
                    Result.success(Unit)
                },
                onFailure = { e ->
                    Log.e(TAG, "Failed to sync reading progress", e)
                    Result.failure(e)
                }
            )
        }.getOrElse { Result.failure(it) }
    }

    /**
     * Pull sync: Get reading history from backend and merge with local data
     * Called on app start or manual sync
     */
    suspend fun pullReadingHistory(novelId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Get reading history from backend
            val backendHistory = userNovelInteractionRepository.getMyReadingHistoryByNovelId(novelId)
                .getOrNull()
            
            if (backendHistory == null) {
                Log.d(TAG, "No reading history found on backend for novel $novelId")
                return@runCatching Result.success(Unit)
            }
            
            // Get local reading data
            val localReadingData = localBookDataSource.getUserReadingData(novelId)
            
            // Merge: prefer local if newer, otherwise use backend
            val mergedData = mergeReadingData(localReadingData, backendHistory)
            
            // Update local database
            localBookDataSource.updateUserReadingData(novelId) { mergedData }
            
            Log.d(TAG, "Successfully pulled and merged reading history for novel $novelId")
            Result.success(Unit)
        }.getOrElse { 
            Log.e(TAG, "Failed to pull reading history", it)
            Result.failure(it)
        }
    }

    /**
     * Sync all reading history from backend
     * Used for full sync operation
     */
    suspend fun syncAllReadingHistory(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            // Get all local reading data
            val allLocalReadingData = localBookDataSource.getAllUserReadingData()
            var syncedCount = 0
            
            // Sync each novel's reading progress
            allLocalReadingData.forEach { localData ->
                if (localData.lastReadChapterId.isNotBlank()) {
                    val chapterNumber = findChapterNumber(localData.id, localData.lastReadChapterId)
                    if (chapterNumber != null) {
                        userNovelInteractionRepository.updateReadingProgress(
                            novelId = localData.id,
                            chapterNumber = chapterNumber
                        ).fold(
                            onSuccess = { syncedCount++ },
                            onFailure = { Log.e(TAG, "Failed to sync ${localData.id}", it) }
                        )
                    }
                }
            }
            
            Log.d(TAG, "Synced $syncedCount out of ${allLocalReadingData.size} reading histories")
            Result.success(syncedCount)
        }.getOrElse {
            Log.e(TAG, "Failed to sync all reading history", it)
            Result.failure(it)
        }
    }

    /**
     * Find chapter number from chapter ID by searching through volumes
     */
    private suspend fun findChapterNumber(novelId: String, chapterId: String): Int? {
        return try {
            // Get volumes from local data source
            val volumes = localBookDataSource.getBookVolumes(novelId)
                ?: return null
            
            // Search through all volumes to find the chapter
            var chapterNumber = 1
            for (volume in volumes.volumes) {
                for (chapter in volume.chapters) {
                    if (chapter.id == chapterId) {
                        return chapterNumber
                    }
                    chapterNumber++
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error finding chapter number", e)
            null
        }
    }

    /**
     * Merge local and backend reading data
     * Prefers local if lastReadTime is newer, otherwise uses backend
     */
    private fun mergeReadingData(
        local: UserReadingData,
        backend: ReadingHistoryData
    ): UserReadingData {
        // Parse backend lastReadAt
        val backendLastReadTime = try {
            backend.lastReadAt?.let { 
                LocalDateTime.parse(it, dateTimeFormatter)
            }
        } catch (e: Exception) {
            null
        }
        
        // Prefer local if it's newer or if backend doesn't have lastReadAt
        val useLocal = backendLastReadTime == null || 
                      local.lastReadTime.isAfter(backendLastReadTime)
        
        return if (useLocal) {
            // Keep local data, but update chapter number if backend has it
            local
        } else {
            // Use backend data, but preserve local fields that backend doesn't have
            val mutable = local.toMutable()
            mutable.lastReadChapterId = backend.currentChapterId ?: local.lastReadChapterId
            mutable.lastReadTime = backendLastReadTime ?: local.lastReadTime
            mutable
        }
    }
}

