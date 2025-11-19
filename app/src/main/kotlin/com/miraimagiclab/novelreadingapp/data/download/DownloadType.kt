package com.miraimagiclab.novelreadingapp.data.download

import androidx.annotation.DrawableRes
import com.miraimagiclab.novelreadingapp.R

enum class DownloadType(
    @field:DrawableRes val icon: Int,
    val typeName: String
) {
    CACHE(R.drawable.downloading_24px, "Bộ nhớ đệm")
}