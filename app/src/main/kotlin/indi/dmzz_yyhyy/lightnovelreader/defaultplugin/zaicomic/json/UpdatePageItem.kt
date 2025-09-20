package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.json

import com.google.gson.annotations.SerializedName

data class UpdatePageItem(
    @SerializedName("comic_id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("cover")
    val cover: String,
)