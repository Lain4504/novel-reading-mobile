package com.miraimagiclab.novelreadingapp.ui.book

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.miraimagiclab.novelreadingapp.ui.book.detail.bookDetailDestination
import com.miraimagiclab.novelreadingapp.ui.book.reader.bookReaderDestination
import com.miraimagiclab.novelreadingapp.ui.navigation.Route

fun NavGraphBuilder.bookNavigation() {
    navigation<Route.Book>(
        startDestination = Route.Book.Detail(""),
    ) {
        bookDetailDestination()
        bookReaderDestination()
    }
}