package io.nightfish.lightnovelreader.api.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography
import io.nightfish.lightnovelreader.api.userdata.BooleanUserData

@Composable
fun SettingsSwitchEntry(
    modifier: Modifier = Modifier,
    iconRes: Int = -1,
    title: String,
    description: String,
    checked: Boolean,
    booleanUserData: BooleanUserData,
    disabled: Boolean = false
) {
    SettingsSwitchEntry(
        modifier = modifier,
        iconRes = iconRes,
        title = title,
        description = description,
        checked = checked,
        onCheckedChange = booleanUserData::asynchronousSet,
        disabled = disabled
    )
}

@Composable
fun SettingsSwitchEntry(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    disabled: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .wrapContentHeight()
            .then(if (!disabled) modifier.clickable { onCheckedChange(!checked) } else modifier)
            .padding(start = 18.dp, end = 14.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (iconRes > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(iconRes),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Icon"
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = AppTypography.labelLarge
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.secondary,
                style = AppTypography.labelMedium
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Switch(
                checked = checked,
                enabled = !disabled,
                onCheckedChange = if (disabled) null else onCheckedChange
            )
        }
    }
}

@Composable
fun SettingsClickableEntry(
    modifier: Modifier = Modifier,
    iconRes: Int = -1,
    title: String,
    description: String,
    openUrl: String
) {
    val context = LocalContext.current
    SettingsClickableEntry(
        modifier = modifier,
        iconRes = iconRes,
        title = title,
        description = description,
        onClick = {
            openUrl.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent, null)
            }
        }
    )
}

@Composable
fun SettingsClickableEntry(
    modifier: Modifier = Modifier,
    iconRes: Int = -1,
    title: String,
    option: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .wrapContentHeight()
            .then(modifier)
            .clickable { onClick.invoke() }
            .padding(start = 18.dp, end = 14.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (iconRes > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(iconRes),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Icon"
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = AppTypography.labelLarge
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.secondary,
                style = AppTypography.labelMedium
            )
            option?.let {
                AnimatedTextLine(
                    text = it,
                    style = AppTypography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailingContent?.let {
            Box(
                modifier = Modifier.wrapContentWidth(Alignment.End)
            ) {
                Box(Modifier.width(52.dp)) {
                    it.invoke()
                }
            }
        }
    }
}