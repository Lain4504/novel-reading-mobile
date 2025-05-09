package indi.dmzz_yyhyy.lightnovelreader.utils

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import java.io.File
import java.io.FileNotFoundException
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember

fun loadReaderFontFamilySafe(uri: Uri): FontFamily? {
    return try {
        val fontFile = File(uri.path ?: return null)
        if (!fontFile.exists()) throw FileNotFoundException()
        FontFamily(Font(fontFile))
    } catch (e: Exception) {
        Log.e("FontLoad", "字体加载失败，使用默认字体", e)
        null
    }
}

@Composable
fun rememberReaderFontFamily(settingState: SettingState): FontFamily {
    val uri = settingState.fontFamilyUri
    println("CALL rememberFontFamily. uri = $uri")
    val fontFamily = remember(uri) {
        loadReaderFontFamilySafe(uri)
    }

    if (fontFamily == null && uri != Uri.EMPTY) {
        val context = LocalContext.current
        LaunchedEffect(uri) {
            settingState.fontFamilyUriUserData.set(Uri.EMPTY)
            Toast.makeText(context, "字体加载失败，已恢复为默认字体", Toast.LENGTH_SHORT).show()
        }
    }

    return fontFamily ?: MaterialTheme.typography.bodyMedium.fontFamily as FontFamily
}

