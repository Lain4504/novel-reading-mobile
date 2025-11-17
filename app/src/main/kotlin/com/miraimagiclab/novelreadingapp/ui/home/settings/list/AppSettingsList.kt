package com.miraimagiclab.novelreadingapp.ui.home.settings.list

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.miraimagiclab.novelreadingapp.ui.components.SettingsMenuEntry
import com.miraimagiclab.novelreadingapp.ui.home.settings.SettingState
import com.miraimagiclab.novelreadingapp.ui.home.settings.data.MenuOptions
import com.miraimagiclab.novelreadingapp.utils.LocalSnackbarHost
import com.miraimagiclab.novelreadingapp.utils.showSnackbar
import com.miraimagiclab.novelreadingapp.R
import io.lain4504.novelreadingapp.api.ui.components.SettingsClickableEntry

@Composable
fun AppSettingsList(
    settingState: SettingState,
    onClickLogcat: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHost.current

    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.bug_report_24px),
        title = stringResource(R.string.settings_app_logs),
        description = stringResource(R.string.settings_app_logs_desc),
        onClick = onClickLogcat
    )
    SettingsMenuEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.bug_report_24px),
        title = stringResource(R.string.settings_app_log_level),
        description = stringResource(R.string.settings_app_log_level_desc),
        options = MenuOptions.LogLevelOptions,
        selectedOptionKey = settingState.logLevelKey,
        onOptionChange = { option ->
            settingState.logLevelKeyUserData.asynchronousSet(option)
            showSnackbar(
                coroutineScope = coroutineScope,
                hostState = snackbarHostState,
                message = context.getString(R.string.restart_to_apply_changes)
            ) { }
        }
    )
}