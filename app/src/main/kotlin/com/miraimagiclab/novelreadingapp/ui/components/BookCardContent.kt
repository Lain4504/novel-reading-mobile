package com.miraimagiclab.novelreadingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.miraimagiclab.novelreadingapp.R
import com.miraimagiclab.novelreadingapp.ui.components.Cover
import io.lain4504.novelreadingapp.api.book.BookInformation
import io.lain4504.novelreadingapp.api.ui.theme.AppTypography

@Composable
fun BookCardContent(
    modifier: Modifier = Modifier,
    bookInformation: BookInformation,
    selected: Boolean = false,
    collected: Boolean = false,
    latestChapterTitle: String? = null,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Row(
        modifier = modifier
            .height(144.dp)
            .background(
                if (selected) colorScheme.primaryContainer
                else colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Cover(
            height = 136.dp,
            width = 94.dp,
            uri = bookInformation.coverUri,
            rounded = 8.dp
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            val textStyle = AppTypography.labelLarge
            val textLineHeight = textStyle.lineHeight
            Text(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 3.dp),
                text = bookInformation.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W600,
                style = AppTypography.labelLarge,
                lineHeight = textLineHeight,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bookInformation.author,
                    maxLines = 1,
                    style = AppTypography.bodyMedium,
                    fontWeight = FontWeight.W600,
                    color = colorScheme.primary
                )
            }
            Text(
                text = bookInformation.description.trim(),
                maxLines = 2,
                fontWeight = FontWeight.Normal,
                overflow = TextOverflow.Ellipsis,
                style = AppTypography.bodyMedium,
                color = colorScheme.secondary,
            )
            if (latestChapterTitle != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(14.dp),
                        painter = painterResource(id = R.drawable.text_snippet_24px),
                        contentDescription = null,
                        tint = colorScheme.secondary
                    )
                    Text(
                        text = latestChapterTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = AppTypography.bodySmall,
                        color = colorScheme.secondary,
                    )
                }
            }
        }
    }
}

@Composable
fun BookCardContentSkeleton(
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val skeletonColor = colorScheme.surfaceContainerHigh
    val skeletonRoundedCorner = RoundedCornerShape(4.dp)
    Row(
        modifier = modifier
            .height(144.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(94.dp, 142.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(skeletonColor)
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 3.dp)
                    .height(40.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.43f)
                    .height(20.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)
            )
        }
    }
}

