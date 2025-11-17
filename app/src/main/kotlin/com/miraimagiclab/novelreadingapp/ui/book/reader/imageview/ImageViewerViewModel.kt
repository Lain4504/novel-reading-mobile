package com.miraimagiclab.novelreadingapp.ui.book.reader.imageview

import androidx.lifecycle.ViewModel
import com.miraimagiclab.novelreadingapp.data.web.WebBookDataSourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageViewerViewModel @Inject constructor(
    webBookDataSourceProvider: WebBookDataSourceProvider
): ViewModel() {
    val imageHeader = webBookDataSourceProvider.value.imageHeader
}