package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.FormattingRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTextFormattingRuleDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    onDelete: () -> Unit,
) {
    val mock = FormattingRule(
        id = 1,
        name = "asdf",
        isRegex = false,
        match = "asdasdasdasdasdasdadasdf",
        replacement = "sdfsdfsdfsdfsdfsdfsdfasdasd",
        isEnabled = true
    )

    var name by remember { mutableStateOf(mock.name) }
    var match by remember { mutableStateOf(mock.match) }
    var replacement by remember { mutableStateOf(mock.replacement) }
    var isRegex by remember { mutableStateOf(mock.isRegex) }

    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "编辑规则",
                style = AppTypography.titleLarge,
                color = colorScheme.onSurface,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = match,
                    onValueChange = { match = it },
                    label = { Text("匹配内容") },
                    singleLine = false
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = replacement,
                    onValueChange = { replacement = it },
                    label = { Text("替换为") },
                    singleLine = false
                )
                FilterChip(
                    selected = isRegex,
                    onClick = { isRegex = !isRegex },
                    leadingIcon = {
                        if (isRegex)
                            Icon(
                                painter = painterResource(R.drawable.check_24px),
                                contentDescription = ""
                            )
                    },
                    label = { Text("使用正则匹配") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmation) {
                Text(text = "保存规则")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDelete) {
                    Text(text = "删除规则", color = colorScheme.error)
                }
                TextButton(onClick = onDismissRequest) {
                    Text(text = "取消")
                }
            }
        }
    )
}

