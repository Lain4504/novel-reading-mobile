package io.nightfish.lightnovelreader.api.plugin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

interface LightNovelReaderPlugin {
    fun onLoad() { }
    fun onUnload() { }
    @Composable
    fun PageContent(paddingValues: PaddingValues) {
    }
}