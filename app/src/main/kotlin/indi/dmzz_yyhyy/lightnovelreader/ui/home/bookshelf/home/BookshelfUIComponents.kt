package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover

@Composable
fun BookCardContent(
    selected: Boolean,
    collected: Boolean,
    modifier: Modifier = Modifier,
    bookInformation: BookInformation,
    latestChapterTitle: String? = null
) {
    Row(
        modifier = modifier.height(136.dp).padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(90.dp, 136.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier.graphicsLayer(alpha = if (selected) 0.7f else 1f)
            ) {
                Cover(
                    width = 90.dp,
                    height = 136.dp,
                    url = bookInformation.coverUrl,
                    rounded = 8.dp
                )
                if (latestChapterTitle != null) { // 有可用更新 Badge
                    Box(
                        modifier = Modifier.padding(4.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
                if (collected) {
                    Box(
                        modifier = Modifier.padding(4.dp)
                            .align(Alignment.TopStart)
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            modifier = Modifier.scale(0.75f, 0.75f),
                            painter = painterResource(R.drawable.filled_bookmark_24px),
                            contentDescription = "collected_indicator",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = selected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val color = MaterialTheme.colorScheme.primary
                    Canvas(
                        modifier = Modifier.size(36.dp)
                    ) {
                        drawCircle(
                            color = color,
                            radius = 18.dp.toPx(),
                        )
                    }
                    Icon(
                        modifier = Modifier
                            .size(22.dp),
                        painter = painterResource(R.drawable.check_24px),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = null
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            val textStyle = AppTypography.titleMedium
            val textLineHeight = textStyle.lineHeight
            Text(
                modifier = Modifier.height(
                    with(LocalDensity.current) { (textLineHeight * 2.2f).toDp() }
                ).wrapContentHeight(Alignment.CenterVertically),
                text = bookInformation.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W600,
                style = textStyle,
                lineHeight = textLineHeight,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bookInformation.author,
                    maxLines = 1,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.primary,
                    style = AppTypography.titleSmall
                )
                BookStatusIcon(bookInformation)
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.book_info_update_date,
                        bookInformation.lastUpdated.year,
                        bookInformation.lastUpdated.monthValue,
                        bookInformation.lastUpdated.dayOfMonth
                    ),
                    style = AppTypography.labelMedium
                )
                Text(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    text = stringResource(
                        R.string.book_info_word_count_kilo,
                        bookInformation.wordCount / 1000
                    ),
                    style = AppTypography.labelMedium
                )
            }
            if (latestChapterTitle == null) {
                Text(
                    text = bookInformation.description.trim(),
                    maxLines = 2,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.secondary,
                    style = AppTypography.labelMedium,
                )
            } else {
                Column {
                    Text(
                        text = "已更新至: ",
                        style = AppTypography.labelMedium,
                    )
                    Text(
                        text = latestChapterTitle,
                        fontWeight = FontWeight.W500,
                        color = MaterialTheme.colorScheme.primary,
                        style = AppTypography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun BookStatusIcon(bookInformation: BookInformation) {
    Row(
        modifier = Modifier.padding(top = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (bookInformation.isComplete) {
            Icon(
                modifier = Modifier.size(16.dp).padding(top = 1.dp),
                painter = painterResource(R.drawable.done_all_24px),
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.outline
            )
        } else {
            Icon(
                modifier = Modifier.size(16.dp).padding(top = 1.dp),
                painter = painterResource(R.drawable.hourglass_top_24px),
                contentDescription = "In Progress",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

