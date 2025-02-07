package indi.dmzz_yyhyy.lightnovelreader.ui.book

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.ui.book.content.bookContentDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.bookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.expandEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandExit
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopExit

fun NavGraphBuilder.bookNavigation(navController: NavController) {
    navigation<Route.Book>(
        startDestination = Route.Book.Detail(1),
        enterTransition = { expandEnter() },
        exitTransition = { expandExit() },
        popEnterTransition = { expandPopEnter() },
        popExitTransition = { expandPopExit() }
    ) {
        bookDetailDestination(navController)
        bookContentDestination(navController)
    }
}