package com.miraimagiclab.novelreadingapp.data.content.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.miraimagiclab.novelreadingapp.ui.book.reader.navigateToImageViewerDialog
import com.miraimagiclab.novelreadingapp.ui.components.ZoomableImage
import io.lain4504.novelreadingapp.api.content.component.AbstractContentComponent
import io.lain4504.novelreadingapp.api.content.component.ImageComponentData
import io.lain4504.novelreadingapp.api.ui.LocalNavController
import io.lain4504.novelreadingapp.api.web.WebBookDataSourceManagerApi

class ImageComponent(
    data: ImageComponentData,
    private val webBookDataSourceManagerApi: WebBookDataSourceManagerApi
): AbstractContentComponent<ImageComponentData>(data) {
    override val id = ImageComponentData.ID

    @Composable
    override fun Content(modifier: Modifier) {
        val imageHeader = remember(webBookDataSourceManagerApi.getWebDataSource()) { webBookDataSourceManagerApi.getWebDataSource().imageHeader }
        val navController = LocalNavController.current
        ZoomableImage(
            imageUri = data.uri,
            modifier = modifier.fillMaxSize(),
            onZoomEnd = {
                println("ciallo~")
                navController.navigateToImageViewerDialog(data.uri)
            },
            header = imageHeader
        )
    }
}