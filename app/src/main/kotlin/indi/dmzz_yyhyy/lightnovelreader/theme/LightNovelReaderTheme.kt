package indi.dmzz_yyhyy.lightnovelreader.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.utils.LocaleUtil

data class AppTheme(
    val isDark: Boolean,
    val colorScheme: ColorScheme
)

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
    val appTheme = remember(isDark, colorScheme) {
        AppTheme(
            isDark = isDark,
            colorScheme = colorScheme,
        )
    }

    LaunchedEffect(colorScheme, isDark) {
        val window = (view.context as Activity).window
        val backgroundColor = colorScheme.background.toArgb()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.setBackgroundDrawable(backgroundColor.toDrawable())
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
        } else {
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                view.setPadding(
                    0, 0, 0, 0
                )
                insets
            }
        }

        WindowInsetsControllerCompat(window, view).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }

    val (language, variant) = appLocale.split("-")
    LocaleUtil.set(language = language, variant = variant)

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
