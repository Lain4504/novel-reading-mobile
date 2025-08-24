package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.bookNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.addBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.markAllChaptersAsReadDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.updatesAvailableDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.downloadmanager.downloadManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.homeNavigation
import indi.dmzz_yyhyy.lightnovelreader.utils.expandEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandExit
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopExit

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LightNovelReaderNavHost(
    navController: NavHostController
) {
    CompositionLocalProvider(LocalNavController provides navController) {
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
                updatesAvailableDialog()
                addBookToBookshelfDialog()
                downloadManager()
                markAllChaptersAsReadDialog()
            }
        }
    }
}