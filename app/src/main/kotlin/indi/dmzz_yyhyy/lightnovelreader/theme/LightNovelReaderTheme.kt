package indi.dmzz_yyhyy.lightnovelreader.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import indi.dmzz_yyhyy.lightnovelreader.utils.LocaleUtil

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun LightNovelReaderTheme(
    darkMode: String,
    isDynamicColor: Boolean = true,
    lightThemeName: String,
    darkThemeName: String,
    appLocale: String,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = when (darkMode) {
        "Disabled" -> false
        "Enabled" -> true
        "FollowSystem" -> isSystemInDarkTheme()
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    else {
        if (isDark)
            when (darkThemeName) {
                "dark_obsidian" -> DarkObsidianColorScheme
                "dark_default"  -> DefaultDarkColorScheme
                else            -> DefaultDarkColorScheme
            }
        else
            when (lightThemeName) {
                "light_default" -> DefaultLightColorScheme
                else            -> DefaultLightColorScheme
            }
    }

    LaunchedEffect(context, view, isDark) {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawable(colorScheme.background.toArgb().toDrawable())

        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !isDark
    }

    val (language, variant) = appLocale.split("-")
    LocaleUtil.set(context, language = language, variant = variant)

    CompositionLocalProvider(LocalIsDarkTheme provides isDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
