package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorationScreen(
    modifier: Modifier = Modifier,
    refresh: () -> Unit,
    uiState: ExplorationUiState,
    content: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    val scope = rememberCoroutineScope()
    val rememberPullToRefreshState = rememberPullToRefreshState()
    AnimatedVisibility(
        modifier = modifier,
        visible = uiState.isOffLine,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = uiState.isRefreshing,
            state = rememberPullToRefreshState,
            onRefresh = {
                refresh.invoke()
                scope.launch {
                    rememberPullToRefreshState.animateToHidden()
                }
            }
        ) {
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                item {
                    EmptyPage(
                        icon = painterResource(R.drawable.link_off_24px),
                        titleId = R.string.offline,
                        descriptionId = R.string.offline_desc
                    )
                }
            }
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = !uiState.isOffLine,
        enter = fadeIn(),
        exit = fadeOut(),
        content = content
    )
}