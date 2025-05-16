package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions

@Composable
fun DisplaySettingsList(
    settingState: SettingState,
    onClickThemeSettings: () -> Unit
) {
    SettingsClickableEntry(
        iconRes = R.drawable.format_paint_24px,
        title = stringResource(R.string.settings_theme),
        description = stringResource(R.string.settings_theme_desc),
        onClick = onClickThemeSettings
    )
    SettingsMenuEntry(
        iconRes = R.drawable.translate_24px,
        title = stringResource(R.string.settings_characters_variant),
        description = stringResource(R.string.settings_characters_variant_desc),
        options = MenuOptions.AppLocaleOptions,
        selectedOptionKey = settingState.appLocaleKey,
        onOptionChange = settingState.appLocaleKeyUserData::asynchronousSet
    )
}