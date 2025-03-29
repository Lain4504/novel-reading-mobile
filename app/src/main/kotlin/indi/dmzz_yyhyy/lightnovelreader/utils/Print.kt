package indi.dmzz_yyhyy.lightnovelreader.utils

import android.util.Log

fun <T> T.debugPrint(tag: String? = null): T = this.also { Log.d( tag ?: "DebugPrint", "${tag?.let { "$it: " } ?: ""}${it.toString()}")}