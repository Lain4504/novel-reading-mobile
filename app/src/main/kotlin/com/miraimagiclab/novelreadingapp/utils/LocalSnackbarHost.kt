package com.miraimagiclab.novelreadingapp.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

val LocalSnackbarHost = compositionLocalOf { SnackbarHostState() }

private var snackbarJob: Job? = null

fun showSnackbar(
    coroutineScope: CoroutineScope,
    hostState: SnackbarHostState,
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short,
    result: (SnackbarResult) -> Unit
) {
    snackbarJob?.cancel()
    snackbarJob = coroutineScope.launch(Dispatchers.Main) {
        val res = hostState.showSnackbar(
            message = message,
            duration = duration,
            actionLabel = actionLabel,
            withDismissAction = withDismissAction
        )
        result(res)
    }
}
