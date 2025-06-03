package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTheme

val LocalNavController = compositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}

val LocalAppTheme = staticCompositionLocalOf<AppTheme> {
    error("No AppThemeContext provided")
}