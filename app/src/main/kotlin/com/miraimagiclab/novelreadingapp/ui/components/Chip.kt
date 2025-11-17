package com.miraimagiclab.novelreadingapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.miraimagiclab.novelreadingapp.R

@Composable
fun SwitchChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = {
            AnimatedContent(targetState = selected) { isSelected ->
                if (isSelected) {
                    Icon(
                        painter = painterResource(R.drawable.check_24px),
                        contentDescription = null
                    )
                }
            }
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}