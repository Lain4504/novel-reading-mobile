package indi.dmzz_yyhyy.lightnovelreader.ui.book

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.ui.book.content.bookContentDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.bookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

fun NavGraphBuilder.bookNavigation(navController: NavController) {
    navigation<Route.Book>(
        startDestination = Route.Book.Detail(1)
    ) {
        bookDetailDestination(navController)
        bookContentDestination(navController)
    }
}