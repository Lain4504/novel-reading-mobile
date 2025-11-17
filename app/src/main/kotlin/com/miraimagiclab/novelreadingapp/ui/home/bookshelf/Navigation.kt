package com.miraimagiclab.novelreadingapp.ui.home.bookshelf

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.miraimagiclab.novelreadingapp.ui.home.bookshelf.edit.bookshelfEditDestination
import com.miraimagiclab.novelreadingapp.ui.home.bookshelf.home.bookshelfHomeDestination
import com.miraimagiclab.novelreadingapp.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.bookshelfNavigation(sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Main.Bookshelf>(
        startDestination = Route.Main.Bookshelf.Home
    ) {
        bookshelfHomeDestination(sharedTransitionScope)
        bookshelfEditDestination()
    }
}

@Suppress("unused")
fun NavController.navigateToBookshelfNavigation() {
    navigate(Route.Main.Bookshelf)
}