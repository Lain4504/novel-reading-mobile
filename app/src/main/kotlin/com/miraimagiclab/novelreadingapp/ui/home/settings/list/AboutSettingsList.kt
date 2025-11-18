package com.miraimagiclab.novelreadingapp.ui.home.settings.list

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.miraimagiclab.novelreadingapp.ui.components.SettingsAboutInfoDialog
import com.miraimagiclab.novelreadingapp.BuildConfig
import com.miraimagiclab.novelreadingapp.R
import io.lain4504.novelreadingapp.api.ui.components.SettingsClickableEntry

@Composable
fun AboutSettingsList() {
    val appInfo: String = buildString {
        append(BuildConfig.VERSION_NAME)
    }
    var showAppInfoDialog by remember { mutableStateOf(false) }

    if (showAppInfoDialog) {
        SettingsAboutInfoDialog(onDismissRequest = { showAppInfoDialog = false })
    }

    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.info_24px),
        title = stringResource(R.string.app_name),
        description = appInfo,
        onClick = { showAppInfoDialog = true },
        option = stringResource(R.string.item_view_details)
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.group_24px),
        title = stringResource(R.string.settings_discord_server),
        description = stringResource(R.string.settings_discord_server_desc),
        openUrl = "https://discord.com/invite/cxrtYXdK8q"
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.archive_24px),
        title = stringResource(R.string.settings_facebook_page),
        description = stringResource(R.string.settings_facebook_page_desc),
        openUrl = "https://www.facebook.com/profile.php?id=61578944706240"
    )
}