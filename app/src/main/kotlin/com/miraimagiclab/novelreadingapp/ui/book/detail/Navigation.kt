package com.miraimagiclab.novelreadingapp.ui.book.detail

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.work.WorkInfo
import com.miraimagiclab.novelreadingapp.ui.book.reader.navigateToBookReaderDestination
import com.miraimagiclab.novelreadingapp.ui.book.reader.navigateToImageViewerDialog
import com.miraimagiclab.novelreadingapp.ui.dialog.navigateToMarkAllChaptersAsReadDialog
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.LocalSnackbarHost
import com.miraimagiclab.novelreadingapp.utils.isResumed
import com.miraimagiclab.novelreadingapp.utils.popBackStackIfResumed
import com.miraimagiclab.novelreadingapp.utils.showSnackbar
import com.miraimagiclab.novelreadingapp.R
import io.lain4504.novelreadingapp.api.ui.LocalNavController
import kotlinx.coroutines.launch

fun NavGraphBuilder.bookDetailDestination() {
    composable<Route.Book.Detail> { entry ->
        val navController = LocalNavController.current
        val bookId = entry.toRoute<Route.Book.Detail>().bookId
        val viewModel = hiltViewModel<DetailViewModel>(entry)
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        viewModel.navController = navController
        val snackbarHostState = LocalSnackbarHost.current

        LaunchedEffect(bookId) {
            viewModel.init(bookId)
        }
        DetailScreen(
            uiState = viewModel.uiState,
            onClickBackButton = navController::popBackStackIfResumed,
            onClickChapter = {
                navController.navigateToBookReaderDestination(bookId, it, context)
            },
            onClickReadFromStart = {
                viewModel.uiState.bookVolumes.volumes.firstOrNull()?.chapters?.firstOrNull()?.id?.let {
                    navController.navigateToBookReaderDestination(bookId, it, context)
                }
            },
            onClickContinueReading = {
                if (viewModel.uiState.userReadingData.lastReadChapterId.isBlank())
                    viewModel.uiState.bookVolumes.volumes.firstOrNull()?.chapters?.firstOrNull()?.id?.let {
                        navController.navigateToBookReaderDestination(bookId, it, context)
                    }
                else {
                    navController.navigateToBookReaderDestination(bookId, viewModel.uiState.userReadingData.lastReadChapterId, context)
                }
            },
            cacheBook = { bookId ->
                coroutineScope.launch {
                    viewModel.cacheBook(bookId).collect {
                        if (it == null) {
                            showSnackbar(
                                coroutineScope = coroutineScope,
                                hostState = snackbarHostState,
                                message = context.getString(
                                    R.string.cache_book_started,
                                    viewModel.uiState.bookInformation.title
                                )
                            ) { }
                            return@collect
                        }
                        when (it.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                showSnackbar(
                                    coroutineScope = coroutineScope,
                                    hostState = snackbarHostState,
                                    message = context.getString(R.string.cache_book_finished)
                                ) { }
                            }
                            WorkInfo.State.FAILED -> {
                                showSnackbar(
                                    coroutineScope = coroutineScope,
                                    hostState = snackbarHostState,
                                    message = context.getString(R.string.cache_book_error)
                                ) { }
                            }
                            WorkInfo.State.RUNNING -> {
                                showSnackbar(
                                    coroutineScope = coroutineScope,
                                    hostState = snackbarHostState,
                                    message = context.getString(R.string.cache_book_running)
                                ) { }
                            }
                            else -> {}
                        }
                    }
                }
            },
            onFollowNovel = viewModel::followNovel,
            onUnfollowNovel = viewModel::unfollowNovel,
            onClickTag = viewModel::onClickTag,
            onClickCover = navController::navigateToImageViewerDialog,
            onClickMarkAllRead = { navController.navigateToMarkAllChaptersAsReadDialog(viewModel.uiState.bookInformation.id) }
        )
    }
}

fun NavController.navigateToBookDetailDestination(bookId: String) {
    if (!this.isResumed()) return
    navigate(Route.Book.Detail(bookId))
}

