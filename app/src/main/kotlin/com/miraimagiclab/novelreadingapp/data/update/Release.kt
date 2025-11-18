package com.miraimagiclab.novelreadingapp.data.update

data class Release(
    val version: Int,
    val versionName: String,
    val releaseNotes: String? = null,
    val storeUrl: String
)