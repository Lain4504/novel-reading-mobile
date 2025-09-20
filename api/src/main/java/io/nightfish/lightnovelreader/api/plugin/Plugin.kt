package io.nightfish.lightnovelreader.api.plugin

annotation class Plugin(
    val name: String,
    val version: Int,
    val versionName: String,
    val author: String,
    val description: String,
    val updateUrl: String
)
