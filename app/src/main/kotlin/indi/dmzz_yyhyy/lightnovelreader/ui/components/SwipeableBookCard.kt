package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.BookCardContent
import indi.dmzz_yyhyy.lightnovelreader.utils.withHaptic
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCardItem(
    modifier: Modifier = Modifier,
    bookInformation: BookInformation,
    selected: Boolean = false,
    collected: Boolean = false,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    latestChapterTitle: String? = null,
    swipeToRightActions: List<SwipeAction> = listOf(),
    swipeToLeftActions: List<SwipeAction> = listOf(),
) {
    SwipeableActionsBox(
        startActions = swipeToRightActions,
        endActions = swipeToLeftActions
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = withHaptic { onLongPress() },
                )
        ) {
            BookCardContent(
                selected = selected,
                collected = collected,
                latestChapterTitle = latestChapterTitle,
                bookInformation = bookInformation
            )
        }
    }

}