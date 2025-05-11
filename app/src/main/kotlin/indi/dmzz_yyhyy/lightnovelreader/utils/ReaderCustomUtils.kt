package indi.dmzz_yyhyy.lightnovelreader.utils

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException

fun loadReaderFontFamilySafe(uri: Uri): FontFamily? {
    return try {
        if (uri == Uri.EMPTY) return null
        val fontFile = File(uri.path ?: return null)
        if (!fontFile.exists()) throw FileNotFoundException()
        FontFamily(Font(fontFile))
    } catch (e: Exception) {
        Log.e("FontLoad", "字体加载失败，使用默认字体", e)
        null
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun rememberReaderFontFamily(settingState: SettingState): FontFamily {
    val coroutineScope = rememberCoroutineScope()
    val uri = settingState.fontFamilyUri
    val fontFamily = remember(uri) {
        loadReaderFontFamilySafe(uri)
    }

    if (fontFamily == null && uri != Uri.EMPTY) {
        val context = LocalContext.current
        coroutineScope.launch(Dispatchers.IO) {
            settingState.fontFamilyUriUserData.set(Uri.EMPTY)
        }
        LaunchedEffect(uri) {
            Toast.makeText(context, "字体加载失败，已恢复为默认字体", Toast.LENGTH_SHORT).show()
        }
    }

    return fontFamily ?: MaterialTheme.typography.bodyMedium.fontFamily as FontFamily
}

