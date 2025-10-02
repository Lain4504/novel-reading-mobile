package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry
import io.nightfish.lightnovelreader.api.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState

@Composable
fun ReadingSettingsList(
    settingState: SettingState,
    onClickTheme: () -> Unit,
    onClickTextFormatting: () -> Unit
) {
    SettingsClickableEntry(
        iconRes = R.drawable.format_paint_24px,
        title = stringResource(R.string.settings_theme),
        description = stringResource(R.string.settings_theme_desc),
        onClick = onClickTheme
    )
    SettingsClickableEntry(
        iconRes = R.drawable.find_replace_24px,
        title = stringResource(R.string.settings_text_formatting),
        description = stringResource(R.string.settings_text_formatting_desc),
        onClick = onClickTextFormatting
    )
    SettingsSwitchEntry(
        iconRes = R.drawable.translate_24px,
        title = stringResource(R.string.settings_trad_conversion),
        description = stringResource(R.string.settings_trad_conversion_desc),
        checked = settingState.enableSimplifiedTraditionalTransform,
        booleanUserData = settingState.enableSimplifiedTraditionalTransformUserData,
    )
}