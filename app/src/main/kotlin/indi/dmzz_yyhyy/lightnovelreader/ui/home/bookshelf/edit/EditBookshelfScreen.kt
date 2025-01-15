package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.Bookshelf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookshelfScreen(
    title: String,
    bookshelfId: Int,
    topBar: (@Composable () -> Unit) -> Unit,
    dialog: (@Composable () -> Unit) -> Unit,
    bookshelf: Bookshelf,
    inti: (Int) -> Unit,
    onClickBack: () -> Unit,
    onClickSave: () -> Unit,
    onClickDelete: () -> Unit,
    onNameChange: (String) -> Unit,
    onAutoCacheChange: (Boolean) -> Unit,
    onSystemUpdateReminderChange: (Boolean) -> Unit,
) {
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var dialogVisible by remember { mutableStateOf(false) }
    topBar {
        TopBar(
            title = title,
            scrollBehavior = pinnedScrollBehavior,
            onClickBack = onClickBack,
            onClickSave = onClickSave
        )
    }
    dialog {
        if (dialogVisible)
            DeleteBookshelfDialog(
                onDismissRequest = { dialogVisible = false },
                onConfirmation = {
                    dialogVisible = false
                    onClickDelete.invoke()
                }
            )
    }
    LaunchedEffect(bookshelfId) {
        inti(bookshelfId)
    }
    Column {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            value = bookshelf.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.name)) },
            placeholder = { Text(stringResource(R.string.bookshelf_name_placeholder)) },
            supportingText = { Text(stringResource(R.string.bookshelf_name_placeholder)) },
            maxLines = 1,
            interactionSource = interactionSource,
            trailingIcon = {
                IconButton(onClick = { onNameChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.cancel_24px),
                        contentDescription = "cancel",
                        tint =
                        if (isFocused) OutlinedTextFieldDefaults.colors().focusedTrailingIconColor
                        else OutlinedTextFieldDefaults.colors().unfocusedTrailingIconColor
                    )
                }
            }
        )
        Text(
            modifier = Modifier.padding(16.dp, 10.dp),
            text = stringResource(R.string.bookshelf_settings),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.W700,
            fontSize = 17.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SwitchSettingItem(
            iconRes = R.drawable.cloud_download_24px,
            title = stringResource(R.string.settings_auto_cache),
            description = stringResource(R.string.settings_auto_cache_desc),
            value = bookshelf.autoCache,
            onValueChange = onAutoCacheChange
        )
        SwitchSettingItem(
            iconRes = R.drawable.outline_schedule_24px,
            title = stringResource(R.string.settings_book_update_reminder),
            description = stringResource(R.string.settings_book_update_reminder_desc),
            value = bookshelf.systemUpdateReminder,
            onValueChange = onSystemUpdateReminderChange
        )
        if (bookshelfId >= 0)
            ListItem(
                modifier = Modifier.clickable {
                    dialogVisible = true
                },
                leadingContent = {
                    Icon(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        painter = painterResource(R.drawable.delete_forever_24px),
                        contentDescription = "Localized description",
                    )
                },
                headlineContent = { Text(
                        text = stringResource(R.string.settings_delete_bookshelf),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_delete_bookshelf_desc),
                        fontSize = 14.sp,
                        lineHeight = 15.sp
                    ) },
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit,
    onClickSave: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(onClickBack) {
                Icon(painterResource(
                    id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
        actions = {
            IconButton(onClickSave) {
                Icon(
                    painter = painterResource(R.drawable.save_24px),
                    contentDescription = "save"
                )
            }
        },
        windowInsets =
        WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun SwitchSettingItem(
    iconRes: Int,
    title: String,
    description: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable {onValueChange(!value)},
        headlineContent = { Text(text = title, fontSize = 16.sp, modifier = Modifier.padding(bottom = 2.dp)) },
        supportingContent = { Text(text = description, fontSize = 14.sp, lineHeight = 15.sp) },
        trailingContent = {
            Switch(
                checked = value,
                onCheckedChange = onValueChange
            )
        },
        leadingContent = {
            Icon(
                modifier = Modifier.padding(horizontal = 10.dp),
                painter = painterResource(iconRes),
                contentDescription = "Localized description",
            )
        },
    )
}

@Composable
private fun DeleteBookshelfDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit) {
    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.dialog_delete_bookshelf),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.W400
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_delete_bookshelf_text),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.W400
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirmation
            ) {
                Text(
                    text = stringResource(android.R.string.ok),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}