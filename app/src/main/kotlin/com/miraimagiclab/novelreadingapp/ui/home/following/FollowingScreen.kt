package com.miraimagiclab.novelreadingapp.ui.home.following

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.miraimagiclab.novelreadingapp.ui.SharedContentKey
import com.miraimagiclab.novelreadingapp.ui.components.AnimatedText
import com.miraimagiclab.novelreadingapp.ui.components.BookCardItem
import com.miraimagiclab.novelreadingapp.ui.components.EmptyPage
import com.miraimagiclab.novelreadingapp.ui.home.HomeNavigateBar
import com.miraimagiclab.novelreadingapp.utils.LocalSnackbarHost
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow
import com.miraimagiclab.novelreadingapp.R
import io.lain4504.novelreadingapp.api.book.BookInformation
import io.lain4504.novelreadingapp.api.ui.theme.AppTypography
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun FollowingScreen(
    controller: NavController,
    selectedRoute: Any,
    init: () -> Unit,
    onClickBook: (String) -> Unit,
    uiState: FollowingUiState,
    getBookInfoFlow: (String) -> kotlinx.coroutines.flow.StateFlow<BookInformation>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val shimmerInstance = rememberShimmer(ShimmerBounds.Custom)

    LaunchedEffect(Unit) {
        init()
    }

    with(sharedTransitionScope) {
        Scaffold(
            topBar = {
                MediumTopAppBar(
                    title = {
                        AnimatedText(
                            text = stringResource(R.string.following),
                            style = AppTypography.titleTopBar
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                HomeNavigateBar(
                    modifier = Modifier.sharedElement(
                        sharedTransitionScope.rememberSharedContentState(SharedContentKey.HomeNavigateBar),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    selectedRoute = selectedRoute,
                    controller = controller
                )
            },
            snackbarHost = {
                SnackbarHost(LocalSnackbarHost.current)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                var showEmptyPage by remember { mutableStateOf(false) }

                LaunchedEffect(uiState.followedNovels) {
                    if (uiState.followedNovels.isEmpty() && !uiState.isLoading) {
                        delay(140)
                        showEmptyPage = true
                    } else {
                        showEmptyPage = false
                    }
                }

                AnimatedVisibility(
                    visible = showEmptyPage,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    EmptyPage(
                        icon = painterResource(R.drawable.bookmarks_90px),
                        title = stringResource(R.string.nothing_here),
                        description = if (uiState.error != null) {
                            uiState.error!!
                        } else {
                            stringResource(R.string.nothing_here_desc_bookshelf)
                        }
                    )
                }

                if (uiState.followedNovels.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .onGloballyPositioned { layoutCoordinates ->
                                val position = layoutCoordinates.unclippedBoundsInWindow()
                                shimmerInstance.updateBounds(position)
                            },
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.followedNovels,
                            key = { it.id }
                        ) { bookInfo ->
                            val infoFlow = remember(bookInfo.id) { getBookInfoFlow(bookInfo.id) }
                            val info by infoFlow.collectAsStateWithLifecycle()

                            BookCardItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                bookInformation = info,
                                selected = false,
                                collected = false,
                                onClick = { onClickBook(bookInfo.id) },
                                onLongPress = { },
                                shimmer = shimmerInstance
                            )
                        }

                        // Pagination controls
                        if (uiState.totalPages > 1) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Trang ${uiState.currentPage + 1} / ${uiState.totalPages}",
                                        style = AppTypography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

