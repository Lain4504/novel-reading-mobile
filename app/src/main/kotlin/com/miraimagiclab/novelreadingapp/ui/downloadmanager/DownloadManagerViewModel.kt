package com.miraimagiclab.novelreadingapp.ui.downloadmanager

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraimagiclab.novelreadingapp.data.book.BookRepository
import com.miraimagiclab.novelreadingapp.data.download.DownloadItem
import com.miraimagiclab.novelreadingapp.data.download.DownloadProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.lain4504.novelreadingapp.api.book.BookInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadManagerViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val downloadProgressRepository: DownloadProgressRepository
) : ViewModel() {
    val downloadItemIdList = downloadProgressRepository.downloadItemIdList
    private val _bookInformationMap = mutableStateMapOf<String, BookInformation>()
    val bookInformationMap: Map<String, BookInformation> = _bookInformationMap

    init {
        viewModelScope.launch(Dispatchers.IO) {
            downloadProgressRepository.downloadItemIdListFlow.collect { downloadItems ->
                downloadItems.forEach { downloadItem ->
                    if (_bookInformationMap.containsKey(downloadItem.bookId))
                         return@forEach
                    viewModelScope.launch(Dispatchers.IO) {
                        bookRepository.getBookInformationFlow(downloadItem.bookId, viewModelScope).collect {
                            _bookInformationMap[it.id] = it
                        }
                    }
                 }
            }
        }
    }

    fun onClickCancel(item: DownloadItem) = downloadProgressRepository.removeExportItem(item)
    fun onClickClearCompleted() = downloadProgressRepository.clearCompleted()
}