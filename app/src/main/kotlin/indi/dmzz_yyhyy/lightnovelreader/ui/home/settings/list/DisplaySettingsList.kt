package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    SettingsClickableEntry(
        iconRes = R.drawable.format_paint_24px,
        title = stringResource(R.string.settings_theme),
        description = stringResource(R.string.settings_theme_desc),
        onClick = onClickThemeSettings
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        SettingsClickableEntry(
            iconRes = R.drawable.language_24px,
            title = stringResource(R.string.settings_app_language),
            description = stringResource(R.string.settings_app_language_desc),
            option = stringResource(R.string.language),
            onClick = {
                val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                intent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(intent)
            }
        )
    }
    SettingsMenuEntry(
        iconRes = R.drawable.translate_24px,
        title = stringResource(R.string.settings_characters_variant),
        description = stringResource(R.string.settings_characters_variant_desc),
        options = MenuOptions.AppLocaleOptions,
        selectedOptionKey = settingState.appLocaleKey,
        onOptionChange = settingState.appLocaleKeyUserData::asynchronousSet
    )
}