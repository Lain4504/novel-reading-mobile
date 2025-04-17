package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState

@Composable
fun DisplaySettingsList(
    settingState: SettingState,
    onClickThemeSettings: () -> Unit
) {
    SettingsClickableEntry(
        iconRes = R.drawable.dark_mode_24px,
        title = stringResource(R.string.settings_dark_theme),
        description = stringResource(R.string.settings_dark_theme_desc),
        onClick = onClickThemeSettings
    )
}