package indi.dmzz_yyhyy.lightnovelreader.utils

fun <T> T.debugPrint(tag: String? = null): T = this.also { println("${tag?.let { "$it: " } ?: ""}${it.toString()}") }