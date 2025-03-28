package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogEntry
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogLevel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogcatScreen(
    uiState: LogcatUiState,
    onClickBack: () -> Unit,
    onClickClearLogs: () -> Unit,
    onClickShareLogs: () -> Unit,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var unwrapLogsText by remember { mutableStateOf(false) }
    val onClickWarp = { unwrapLogsText = !unwrapLogsText }
    val entries = uiState.logEntries


    LaunchedEffect(entries.size) {
        if (uiState.logEntries.size > 1) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.logEntries.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日志") },
                navigationIcon = {
                    IconButton(onClickBack) {
                        Icon(
                            painterResource(id = R.drawable.arrow_back_24px),
                            contentDescription = "back"
                        )
                    }
                },
                actions = {
                    if (entries.isNotEmpty()){
                        IconButton(onClickClearLogs) {
                            Icon(
                                painterResource(id = R.drawable.delete_forever_24px),
                                contentDescription = "clear",
                            )
                        }
                        if (!unwrapLogsText)
                            IconButton(onClickWarp) {
                                Icon(
                                    painterResource(id = R.drawable.wrap_text_24px),
                                    contentDescription = "wrap"
                                )
                            }
                        else
                            IconButton(onClickWarp) {
                                Icon(
                                    painterResource(id = R.drawable.menu_open_24px),
                                    contentDescription = "unwrap",
                                )
                            }
                        IconButton(onClickShareLogs) {
                            Icon(
                                painterResource(id = R.drawable.ios_share_24px),
                                contentDescription = "share",
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (entries.isEmpty()) EmptyLogListContent() else {
                if (unwrapLogsText) LogListContent(uiState.logEntries, listState)
                else UnWrapLogListContent(uiState.logEntries, listState)
            }
        }
    }
}

@Composable
fun EmptyLogListContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(44.dp),
                painter = painterResource(R.drawable.bug_report_24px),
                tint = MaterialTheme.colorScheme.outline,
                contentDescription = "empty_list_icon"
            )
            Spacer(Modifier.height(18.dp))
            Text("空日志列表，等待更新")
            Text("")
        }
    }
}

@Composable
fun UnWrapLogListContent(
    logEntries: List<LogEntry>,
    listState: LazyListState
) {
    Box(modifier = Modifier.width(Int.MAX_VALUE.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState()),
            contentPadding = PaddingValues(2.dp)
        ) {
            logEntries.forEach {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = it.text,
                        color = colorOf(it.logLevel),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.sp,
                        lineHeight = 15.sp,
                        fontSize = 12.sp,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun colorOf(logLevel: LogLevel): Color {
    return when (logLevel.level) {
        2 -> MaterialTheme.colorScheme.error
        4 -> Color(0xFFF7B400)
        6 -> MaterialTheme.colorScheme.onSurface
        8 -> MaterialTheme.colorScheme.outline
        10 -> MaterialTheme.colorScheme.outlineVariant
        else -> MaterialTheme.colorScheme.outline
    }
}

@Composable
fun LogListContent(
    logEntries: List<LogEntry>,
    listState: LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(2.dp)
    ) {

        logEntries.forEach {
            item {
                Text(
                    text = it.text,
                    color = colorOf(it.logLevel),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.sp,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    softWrap = true
                )
            }
        }
    }
}