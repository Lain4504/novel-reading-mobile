package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.LightNovelReaderNavHost

@Composable
fun LightNovelReaderApp() {
    val navController = rememberNavController()
    /*
    val context = LocalContext.current
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        viewModel.autoCheckUpdate()
    }
    LaunchedEffect(viewModel.updateDialogUiState.toast) {
        if (viewModel.updateDialogUiState.toast.isBlank()) return@LaunchedEffect
        Toast.makeText(context, viewModel.updateDialogUiState.toast, Toast.LENGTH_SHORT).show()
        viewModel.clearToast()
    }
    if (viewModel.updateDialogUiState.visible) {
        val releaseNotes = viewModel.updateDialogUiState.release.releaseNotes ?: ""
        val downloadUrl = viewModel.updateDialogUiState.release.downloadUrl ?: ""
        val version = viewModel.updateDialogUiState.release.version ?: -1
        val versionName = viewModel.updateDialogUiState.release.versionName ?: ""
        val checksum = viewModel.updateDialogUiState.release.checksum ?: ""
        val downloadSize = viewModel.updateDialogUiState.release.downloadSize?.toLong() ?: -1
        UpdatesAvailableDialog(
            onDismissRequest = viewModel::onDismissUpdateRequest,
            onConfirmation = { viewModel.downloadUpdate(
                url = downloadUrl,
                version = versionName,
                checksum = checksum,
                context = context
            ) },
            newVersionCode = version,
            newVersionName = versionName,
            contentMarkdown = releaseNotes,
            downloadSize = downloadSize.toDouble(),
            downloadUrl = downloadUrl
        )
    }
    if (viewModel.addToBookshelfDialogUiState.visible)
        AddBookToBookshelfDialog(
            onDismissRequest = viewModel::onDismissAddToBookshelfRequest,
            onConfirmation = viewModel::processAddToBookshelfRequest,
            onSelectBookshelf = viewModel::onSelectBookshelf,
            onDeselectBookshelf = viewModel::onDeselectBookshelf,
            allBookshelf = viewModel.addToBookshelfDialogUiState.allBookShelf,
            selectedBookshelfIds = viewModel.addToBookshelfDialogUiState.selectedBookshelfIds
        )
    */
    LightNovelReaderNavHost(navController)
}
