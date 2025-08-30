package io.nightfish.defaultdatasource.zaicomic.json

import com.google.gson.annotations.SerializedName

data class UpdatePageItem(
    @SerializedName("comic_id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("cover")
    val cover: String,
)