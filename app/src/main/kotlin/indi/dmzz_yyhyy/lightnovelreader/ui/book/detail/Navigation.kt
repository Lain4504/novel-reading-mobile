package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.content.Intent
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import androidx.work.WorkInfo
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToBookReaderDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ExportToEpubDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun NavGraphBuilder.bookDetailDestination() {
    composable<Route.Book.Detail> { entry ->
        val navController = LocalNavController.current
        val bookId = entry.toRoute<Route.Book.Detail>().bookId
        val viewModel = hiltViewModel<DetailViewModel>()
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        viewModel.navController = navController
        LaunchedEffect(bookId) {
            viewModel.init(bookId)
        }
        DetailScreen(
            uiState = viewModel.uiState,
            onClickExportToEpub = {
                navController.navigateToExportToEpubDialog(bookId, viewModel.uiState.bookInformation.title)
            },
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
                if (viewModel.uiState.userReadingData.lastReadChapterId == -1)
                    viewModel.uiState.bookVolumes.volumes.firstOrNull()?.chapters?.firstOrNull()?.id?.let {
                        navController.navigateToBookReaderDestination(bookId, it, context)
                    }
                else {
                    navController.navigateToBookReaderDestination(bookId, viewModel.uiState.userReadingData.lastReadChapterId, context)
                }
            },
            cacheBook = {
                coroutineScope.launch {
                    viewModel.cacheBook(it).collect {
                        if (it == null) {
                            Toast.makeText(context, "此书本正在缓存中", Toast.LENGTH_SHORT).show()
                            return@collect
                        }
                        when (it.state) {
                            WorkInfo.State.SUCCEEDED -> Toast.makeText(context, "缓存成功", Toast.LENGTH_SHORT).show()
                            WorkInfo.State.FAILED -> Toast.makeText(context, "缓存书本失败", Toast.LENGTH_SHORT).show()
                            WorkInfo.State.RUNNING -> Toast.makeText(context, "此书本正在缓存中", Toast.LENGTH_SHORT).show()
                            else -> {}
                        }
                    }
                }
            },
            requestAddBookToBookshelf = navController::navigateToAddBookToBookshelfDialog,
            onClickTag = viewModel::onClickTag
        )
    }
    exportToEpubDialog()
}

fun NavController.navigateToBookDetailDestination(bookId: Int) {
    navigate(Route.Book.Detail(bookId))
}

fun NavGraphBuilder.exportToEpubDialog() {
    dialog<Route.Book.ExportUserDataDialog> { entry ->
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<ExportToEpubDialogViewModel>()
        val context = LocalContext.current
        val route = entry.toRoute<Route.Book.ExportUserDataDialog>()
        val bookId = route.bookId
        val title = route.title
        val exportToEPUBLauncher = uriLauncher {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "开始导出书本 $title", Toast.LENGTH_SHORT).show()
                viewModel.exportToEpub(it, bookId, title).collect {
                    if (it != null)
                        when (it.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                Toast.makeText(context, "成功导出书本 $title", Toast.LENGTH_SHORT).show()
                            }
                            WorkInfo.State.FAILED -> {
                                Toast.makeText(context, "导出书本 $title 失败", Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                }
            }
            navController.popBackStack()
        }
        ExportToEpubDialog (
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = {
                createDataFile(title, exportToEPUBLauncher)
            }
        )
    }
}

@Suppress("DuplicatedCode")
fun createDataFile(fileName: String, launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/epub+zip"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
        putExtra(Intent.EXTRA_TITLE, fileName)
    }
    launcher.launch(Intent.createChooser(intent, "选择一位置"))
}

fun NavController.navigateToExportToEpubDialog(bookId: Int, title: String) {
    navigate(Route.Book.ExportUserDataDialog(bookId, title))
}
