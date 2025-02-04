import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.book.content.navigateToBookContentDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home.navigateToExplorationHomeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home.ReadingHomeScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home.ReadingHomeViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.navigateToReadingStatsDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.readingHomeDestination(navController: NavController, sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Home.Reading.Home> {
        val readingHomeViewModel = hiltViewModel<ReadingHomeViewModel>()
        ReadingHomeScreen(
            controller = navController,
            selectedRoute = Route.Home.Reading,
            uiState = readingHomeViewModel.uiState,
            update = readingHomeViewModel::update,
            onClickBook = navController::navigateToBookDetailDestination,
            onClickContinueReading = { bookId, chapterId ->
                navController.navigateToBookDetailDestination(bookId)
                navController.navigateToBookContentDestination(bookId, chapterId)
            },
            onClickJumpToExploration = navController::navigateToExplorationHomeDestination,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this,
            onClickStats = navController::navigateToReadingStatsDestination
        )
    }
}