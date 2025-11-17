package com.miraimagiclab.novelreadingapp.ui.home.explore.expanded

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.miraimagiclab.novelreadingapp.ui.book.detail.navigateToBookDetailDestination
import com.miraimagiclab.novelreadingapp.ui.dialog.navigateToAddBookToBookshelfDialog
import com.miraimagiclab.novelreadingapp.ui.home.explore.ExploreViewModel
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.isResumed
import com.miraimagiclab.novelreadingapp.utils.popBackStackIfResumed
import io.lain4504.novelreadingapp.api.ui.LocalNavController

fun NavGraphBuilder.exploreExpandDestination() {
    composable<Route.Main.Explore.Expanded> { entry ->
        val navController = LocalNavController.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val exploreViewModel = hiltViewModel<ExploreViewModel>(parentEntry)
        val exploreExpandedPageHomeViewModel = hiltViewModel<ExpandedPageViewModel>()
        var dialog : @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
        ExpandedPageScreen(
            exploreUiState = exploreViewModel.uiState,
            expandedPageUiState = exploreExpandedPageHomeViewModel.uiState,
            refresh = exploreViewModel::refresh,
            dialog = { newDialog -> dialog = newDialog },
            expandedPageDataSourceId = entry.toRoute<Route.Main.Explore.Expanded>().expandedPageDataSourceId,
            init = exploreExpandedPageHomeViewModel::init,
            loadMore = exploreExpandedPageHomeViewModel::loadMore,
            requestAddBookToBookshelf = {
                navController.navigateToAddBookToBookshelfDialog(it)
            },
            onClickBack = {
                exploreExpandedPageHomeViewModel.clear()
                navController.popBackStackIfResumed()
            },
            onClickBook = {
                navController.navigateToBookDetailDestination(it)
            }
        )
        dialog.invoke()
    }
}

fun NavController.navigateToExploreExpandDestination(expandedPageDataSourceId: String) {
    if (!this.isResumed()) return
    navigate(Route.Main.Explore.Expanded(expandedPageDataSourceId))
}