package com.miraimagiclab.novelreadingapp.ui.home.settings.list

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.miraimagiclab.novelreadingapp.R
import io.lain4504.novelreadingapp.api.ui.components.SettingsClickableEntry

@Composable
fun ReadingSettingsList(
    onClickTheme: () -> Unit
) {
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.format_paint_24px),
        title = stringResource(R.string.settings_theme),
        description = stringResource(R.string.settings_theme_desc),
        onClick = onClickTheme
    )
}