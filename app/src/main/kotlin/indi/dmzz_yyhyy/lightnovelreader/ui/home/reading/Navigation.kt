package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.book.content.navigateToBookContentDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home.navigateToExplorationHomeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.homeReadingDestination(navController: NavController, sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Home.Reading> {
        val parentEntry = remember(it) { navController.getBackStackEntry(Route.Home) }
        val readingViewModel = hiltViewModel<ReadingViewModel>(parentEntry)
        ReadingScreen(
            controller = navController,
            selectedRoute = Route.Home.Reading,
            uiState = readingViewModel.uiState,
            onClickBook = navController::navigateToBookDetailDestination,
            onClickContinueReading = { bookId, chapterId ->
                navController.navigateToBookDetailDestination(bookId)
                navController.navigateToBookContentDestination(bookId, chapterId)
            },
            onClickJumpToExploration = navController::navigateToExplorationHomeDestination,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this
        )
    }
}

@Suppress("unused")
fun NavController.navigateToHomeReadingDestination() {
    navigate(Route.Home.Reading)
}