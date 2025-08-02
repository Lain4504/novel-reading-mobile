package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormattingRule
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography

@Composable
fun EditTextFormattingRuleDialog(
    rule: FormattingRule,
    matchTextFieldValue: TextFieldValue,
    onNameChange: (String) -> Unit,
    onMatchChange: (TextFieldValue) -> Unit,
    onReplacementChange: (String) -> Unit,
    onIsRegexChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    onDelete: () -> Unit,
) {
    LaunchedEffect(rule.isRegex) {
        onMatchChange.invoke(matchTextFieldValue)
    }
    val isWrong = remember(rule.match, rule.isRegex) {
        if (!rule.isRegex) return@remember false
        var v = true
        try {
            Regex(rule.match)
            v = false
        } catch (_: Exception) { }
        return@remember v
    }
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
                    value = rule.name,
                    onValueChange = onNameChange,
                    label = { Text("名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = matchTextFieldValue,
                    onValueChange = onMatchChange,
                    label = { Text("匹配内容") },
                    singleLine = false,
                    isError = isWrong,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = rule.replacement,
                    onValueChange = onReplacementChange,
                    label = { Text("替换为") },
                    singleLine = false
                )
                FilterChip(
                    selected = rule.isRegex,
                    onClick = { onIsRegexChange(!rule.isRegex) },
                    leadingIcon = {
                        if (rule.isRegex)
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
            TextButton(onClick = onConfirmation, enabled = !isWrong) {
                Text(text = "保存规则")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rule.id != -1)
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

