package indi.dmzz_yyhyy.lightnovelreader.ui.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.BookCardContent
import indi.dmzz_yyhyy.lightnovelreader.utils.withHaptic
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

val addToBookshelf = SwipeAction(
    icon = {
        Icon(
            painter = painterResource(R.drawable.bookmark_add_24px),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            contentDescription = null
        )
    },
    background = Color(0xff2ECC71),
    isUndo = true,
    onSwipe = { },
)

val removeFromBookshelf = SwipeAction(
    icon = {
        Icon(
            painter = painterResource(R.drawable.delete_forever_24px),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            contentDescription = null
        )
    },
    background = Color(0xffE74C3C),
    isUndo = true,
    onSwipe = { },
)

val pin = SwipeAction(
    icon = {
        Icon(
            painter = painterResource(R.drawable.keep_24px),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            contentDescription = null
        )
    },
    background = Color(0xff007AFF),
    isUndo = true,
    onSwipe = { },
)

val expand = SwipeAction(
    icon = {
        Icon(
            painter = painterResource(R.drawable.expand_circle_down_24px),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            contentDescription = null
        )
    },
    background = Color(0xffF1C40F),
    isUndo = true,
    onSwipe = { },
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCardItem(
    bookInformation: BookInformation,
    selected: Boolean = false,
    collected: Boolean = false,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    latestChapterTitle: String? = null,
    swipeToRightActions: List<SwipeAction> = listOf(),
    swipeToLeftActions: List<SwipeAction> = listOf(),
){

    SwipeableActionsBox(
        startActions = swipeToRightActions,
        endActions = swipeToLeftActions
    ) {
        Box(
            modifier = Modifier
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