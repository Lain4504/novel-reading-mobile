package com.miraimagiclab.novelreadingapp.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.miraimagiclab.novelreadingapp.ui.auth.authNavigation
import com.miraimagiclab.novelreadingapp.ui.book.bookNavigation
import com.miraimagiclab.novelreadingapp.ui.dialog.addBookToBookshelfDialog
import com.miraimagiclab.novelreadingapp.ui.dialog.markAllChaptersAsReadDialog
import com.miraimagiclab.novelreadingapp.ui.dialog.updatesAvailableDialog
import com.miraimagiclab.novelreadingapp.ui.downloadmanager.downloadManager
import com.miraimagiclab.novelreadingapp.ui.home.homeNavigation
import com.miraimagiclab.novelreadingapp.utils.LocalSnackbarHost
import com.miraimagiclab.novelreadingapp.utils.expandEnter
import com.miraimagiclab.novelreadingapp.utils.expandExit
import com.miraimagiclab.novelreadingapp.utils.expandPopEnter
import com.miraimagiclab.novelreadingapp.utils.expandPopExit
import io.lain4504.novelreadingapp.api.ui.LocalNavController

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LightNovelReaderNavHost(
    navController: NavHostController,
    onBuildNavHost: NavGraphBuilder.() -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalSnackbarHost provides snackbarHostState
    ) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = Route.Main,
                enterTransition = { expandEnter() },
                exitTransition = { expandExit() },
                popEnterTransition = { expandPopEnter() },
                popExitTransition = { expandPopExit() }
            ) {
                homeNavigation(this@SharedTransitionLayout)
                bookNavigation()
                authNavigation()
                updatesAvailableDialog()
                addBookToBookshelfDialog()
                downloadManager()
                markAllChaptersAsReadDialog()
                onBuildNavHost.invoke(this)
            }
        }
    }
}