package com.miraimagiclab.novelreadingapp.ui.home.settings.list

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.miraimagiclab.novelreadingapp.ui.components.SettingsMenuEntry
import com.miraimagiclab.novelreadingapp.ui.home.settings.SettingState
import com.miraimagiclab.novelreadingapp.ui.home.settings.data.MenuOptions
import com.miraimagiclab.novelreadingapp.R
import io.lain4504.novelreadingapp.api.ui.components.SettingsClickableEntry
import io.lain4504.novelreadingapp.api.ui.components.SettingsSwitchEntry

@Composable
fun UpdatesSettingsList(
    updatePhase: String,
    settingState: SettingState,
    checkUpdate: () -> Unit,
) {
    SettingsSwitchEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.cloud_download_24px),
        title = stringResource(R.string.settings_auto_check_updates),
        description = stringResource(R.string.settings_auto_check_updates_desc),
        checked = settingState.checkUpdate,
        booleanUserData = settingState.checkUpdateUserData
    )
    SettingsMenuEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.alt_route_24px),
        title = stringResource(R.string.settings_update_channel),
        description = stringResource(R.string.settings_update_channel_desc),
        options = MenuOptions.UpdatePlatformOptions.getOptionWithValue(settingState.distributionPlatformKey).value,
        selectedOptionKey = settingState.updateChannelKey,
        onOptionChange = settingState.updateChannelKeyUserData::asynchronousSet
    )
    SettingsMenuEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.outline_explore_24px),
        title = stringResource(R.string.settings_distribution_platform),
        options = MenuOptions.UpdatePlatformOptions,
        selectedOptionKey = settingState.distributionPlatformKey,
        onOptionChange = { option ->
            settingState.distributionPlatformKeyUserData.asynchronousSet(option)
            if (MenuOptions.UpdatePlatformOptions.optionList.firstOrNull { settingState.updateChannelKey == it.key } == null)
                settingState.updateChannelKeyUserData.asynchronousSet(MenuOptions.UpdateChannelOptions.Development)
        }
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.deployed_code_update_24px),
        title = stringResource(R.string.settings_get_updates),
        description = stringResource(R.string.settings_get_updates_desc),
        option = updatePhase,
        onClick = checkUpdate
    )
}