package com.miraimagiclab.novelreadingapp.utils

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.miraimagiclab.novelreadingapp.ui.LocalAppTheme
import com.miraimagiclab.novelreadingapp.ui.book.reader.SettingState
import com.miraimagiclab.novelreadingapp.R
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
        Log.e("FontLoad", "Failed to load custom font", e)
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
            settingState.fontFamilyUriUserData.asynchronousSet(Uri.EMPTY)
            Toast.makeText(context, "字体加载失败，已恢复为默认字体", Toast.LENGTH_SHORT).show()
        }
    }

    return fontFamily ?: MaterialTheme.typography.bodyMedium.fontFamily as FontFamily
}

@Composable
fun rememberReaderBackgroundPainter(settingState: SettingState): Painter {
    val context = LocalContext.current
    val isDark = LocalAppTheme.current.isDark
    val isCustomEmpty = (settingState.backgroundImageUri.toString().isEmpty() && settingState.backgroundDarkImageUri.toString().isEmpty())

    var loadFailed by remember { mutableStateOf(false) }


    if (isCustomEmpty) {
        return painterResource(id = R.drawable.paper)
    }

    val backgroundUri = remember(isDark) {
        if (isDark) settingState.backgroundDarkImageUri
        else settingState.backgroundImageUri
    }

    val imageRequest = remember(backgroundUri) {
        ImageRequest.Builder(context)
            .data(backgroundUri)
            .listener(onError = { _, _ -> loadFailed = true })
            .build()
    }

    val painter = rememberAsyncImagePainter(
        model = imageRequest,
        error = painterResource(id = R.drawable.paper)
    )

    if (loadFailed) {
        LaunchedEffect(backgroundUri) {
            when {
                isDark && settingState.backgroundDarkImageUri != Uri.EMPTY ->
                    settingState.backgroundDarkImageUriUserData.asynchronousSet(Uri.EMPTY)
                else ->
                    settingState.backgroundImageUriUserData.asynchronousSet(Uri.EMPTY)
            }
            Toast.makeText(context, "背景加载失败，已恢复默认", Toast.LENGTH_SHORT).show()
            loadFailed = false
        }
    }

    return painter
}

@Composable
fun readerTextColor(settingState: SettingState): Color {
    val localTheme = LocalAppTheme.current
    val isDark = localTheme.isDark
    val onSurface = localTheme.colorScheme.onSurface

    val color = remember(isDark, settingState.textColor, settingState.textDarkColor, onSurface) {
        when {
            isDark && settingState.textDarkColor.isUnspecified -> onSurface
            !isDark && settingState.textColor.isUnspecified -> onSurface
            isDark -> settingState.textDarkColor
            else -> settingState.textColor
        }
    }

    return color
}
