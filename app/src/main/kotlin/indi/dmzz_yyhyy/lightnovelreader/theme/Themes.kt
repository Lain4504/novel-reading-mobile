package indi.dmzz_yyhyy.lightnovelreader.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import indi.dmzz_yyhyy.lightnovelreader.utils.LocaleUtil

@Composable
fun LightNovelReaderTheme(
    darkMode: String,
    isDynamicColor: Boolean = true,
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
    val colorScheme =
        if (isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            if (isDark) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        else
            if (isDark) darkColorScheme()
            else lightColorScheme()

    LaunchedEffect(context, view, isDark) {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawable(
            ColorDrawable(colorScheme.background.toArgb())
        )

        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !isDark
    }

    val (language, variant) = appLocale.split("-")
    LocaleUtil.set(context, language = language, variant = variant)

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
