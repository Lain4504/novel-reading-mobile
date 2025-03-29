package indi.dmzz_yyhyy.lightnovelreader.utils

import android.util.Log

fun <T> T.debugPrint(tag: String? = null): T = this.also { value -> Log.d(tag ?: "DebugPrint", "${tag?.let { "$it: " } ?: ""}${value.toString()}") }