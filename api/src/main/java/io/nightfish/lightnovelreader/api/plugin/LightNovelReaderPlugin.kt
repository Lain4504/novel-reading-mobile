package io.nightfish.lightnovelreader.api.plugin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

interface LightNovelReaderPlugin {
    fun onLoad() { }
    fun onUnload() { }
//    fun PluginPageContentRegistry.registerPluginPage(pluginId: String) {
//        register(pluginId) {
//        }
//    }
    @Composable
    fun PageContent(paddingValues: PaddingValues) {
    }
}