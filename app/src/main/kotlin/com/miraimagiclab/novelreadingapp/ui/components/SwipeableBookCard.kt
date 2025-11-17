package com.miraimagiclab.novelreadingapp.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.miraimagiclab.novelreadingapp.ui.home.bookshelf.home.BookCardContent
import com.miraimagiclab.novelreadingapp.ui.home.bookshelf.home.BookCardContentSkeleton
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer
import io.lain4504.novelreadingapp.api.book.BookInformation
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
    shimmer: Shimmer? = null,
    swipeToRightActions: List<SwipeAction> = listOf(),
    swipeToLeftActions: List<SwipeAction> = listOf(),
) {
    val isEmpty = bookInformation.isEmpty()

    SwipeableActionsBox(
        startActions = swipeToRightActions,
        endActions = swipeToLeftActions
    ) {
        Crossfade(
            targetState = isEmpty,
            label = "BookCardCrossfade"
        ) { empty ->
            if (empty) {
                BookCardContentSkeleton(
                    modifier = if (shimmer != null) modifier.shimmer(shimmer)
                    else modifier
                )
            } else {
                BookCardContent(
                    modifier = modifier,
                    selected = selected,
                    collected = collected,
                    latestChapterTitle = latestChapterTitle,
                    bookInformation = bookInformation,
                    onClick = onClick,
                    onLongPress = onLongPress
                )
            }
        }
    }

}