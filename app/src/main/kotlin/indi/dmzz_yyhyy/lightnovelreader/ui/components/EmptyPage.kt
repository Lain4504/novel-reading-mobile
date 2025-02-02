package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyPage(
    modifier: Modifier = Modifier,
    icon: Painter,
    titleId: Int,
    descriptionId: Int,
    button: @Composable () -> Unit = {},
) {
    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(76.dp),
                painter = icon,
                tint = MaterialTheme.colorScheme.secondary,
                contentDescription = null
            )
            Spacer(Modifier.height(42.dp))
            Text(
                text = stringResource(titleId),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(descriptionId),
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.secondary,
            )
            if (button != {}) { Spacer(Modifier.height(35.dp)) }
            button.invoke()
        }
    }
}