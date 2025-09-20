package indi.dmzz_yyhyy.lightnovelreader.data.plugin

data class PluginInfo(
    val isUpdatable: Boolean,
    val id: String,
    val name: String,
    val version: Int,
    val versionName: String,
    val author: String,
    val description: String,
    val updateUrl: String?
)