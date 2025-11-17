package com.miraimagiclab.novelreadingapp.ui.home.reading.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.miraimagiclab.novelreadingapp.ui.book.detail.navigateToBookDetailDestination
import com.miraimagiclab.novelreadingapp.ui.book.reader.navigateToBookReaderDestination
import com.miraimagiclab.novelreadingapp.ui.downloadmanager.navigateToDownloadManager
import com.miraimagiclab.novelreadingapp.ui.home.reading.stats.navigateToReadingStatsDestination
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import io.lain4504.novelreadingapp.api.ui.LocalNavController

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.readingHomeDestination(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Reading.Home> { entry ->
        val navController = LocalNavController.current
        val context = LocalContext.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val readingViewModel = hiltViewModel<ReadingHomeViewModel>(parentEntry)
        ReadingScreen(
            controller = navController,
            selectedRoute = Route.Main.Reading,
            recentReadingBookIds = readingViewModel.recentReadingBookIds,
            recentReadingUserReadingDataMap = readingViewModel.recentReadingUserReadingDataMap,
            recentReadingBookInformationMap = readingViewModel.recentReadingBookInformationMap,
            onClickDownloadManager = navController::navigateToDownloadManager,
            onClickBook = navController::navigateToBookDetailDestination,
            onClickContinueReading = { bookId, chapterId ->
                navController.navigateToBookDetailDestination(bookId)
                navController.navigateToBookReaderDestination(bookId, chapterId, context)
            },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this,
            onClickStats = navController::navigateToReadingStatsDestination,
            loadBookInfo = readingViewModel::loadBookInfo,
            onAddBook = readingViewModel::addToReadingList,
            onRemoveBook = readingViewModel::removeFromReadingList
        )
    }
}