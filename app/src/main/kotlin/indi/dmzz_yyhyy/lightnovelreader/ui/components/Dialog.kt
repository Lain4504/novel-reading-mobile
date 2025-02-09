package indi.dmzz_yyhyy.lightnovelreader.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.update.UpdateCheckRepository.Companion.proxyUrlRegex
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.StringUserData
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.ObjectOptions

@Composable
fun BaseDialog(
    icon: Painter,
    title: String,
    description: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dismissText: String,
    confirmationText: String,
    content: @Composable ColumnScope.() -> Unit
) {
    BaseDialog(
        icon = icon,
        title = title,
        description = description,
        onDismissRequest = onDismissRequest,
    ) {
        content.invoke(this)
        Row(
            modifier = Modifier
                .padding(8.dp, 24.dp, 24.dp, 24.dp)
                .align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            TextButton(
                onClick = onConfirmation
            ) {
                Text(
                    text = confirmationText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun BaseDialog(
    icon: Painter,
    title: String,
    description: String,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card(
            modifier = Modifier
                .sizeIn(minWidth = 280.dp, maxWidth = 560.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Box(Modifier.height(24.dp))
            Icon(
                modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                painter = icon,
                tint = MaterialTheme.colorScheme.secondary,
                contentDescription = null
            )
            Box(Modifier.height(16.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.W400,
            )
            Box(Modifier.height(16.dp))
            Text(
                modifier = Modifier
                    .sizeIn(minWidth = 280.dp, maxWidth = 560.dp)
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Start,
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W400,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(Modifier.height(16.dp))
            content.invoke(this)
        }
    }
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun SliderDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onSlideChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    title: String,
    description: String
) {
    BaseDialog(
        icon = painterResource(R.drawable.filled_settings_24px),
        title = title,
        description = description,
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        dismissText = stringResource(R.string.cancel),
        confirmationText = stringResource(R.string.apply),
    ) {
        val sliderPercentage = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
        val current = LocalDensity.current
        var indicatorWidthDp by remember { mutableStateOf(0F) }

        Box(modifier = Modifier.width(350.dp))  {
            Box(
                modifier = Modifier
                    .offset(x = ((sliderPercentage * 300 + 25) - (indicatorWidthDp / 2)).dp)
                    .clip(RoundedCornerShape(64.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(12.dp)
            ) {
                Text(
                    modifier = Modifier
                        .onGloballyPositioned { layoutCoordinates ->
                            with(current) {
                                indicatorWidthDp = layoutCoordinates.size.width.toDp().value
                            }
                        }
                        .padding(horizontal = 12.dp),
                    text = value.toInt().toString(),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        }
        Slider(
            modifier = Modifier.width(300.dp).align(Alignment.CenterHorizontally),
            value = value,
            valueRange = valueRange,
            steps = steps,
            onValueChange = onSlideChange,
            onValueChangeFinished = onSliderChangeFinished,
            colors = SliderDefaults.colors(
                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        )
    }
}


interface ExportContext {
    val bookshelf: Boolean
    val readingData: Boolean
    val settings: Boolean
    val bookmark: Boolean
}

class MutableExportContext: ExportContext {
    override var bookshelf by mutableStateOf(true)
    override var readingData by mutableStateOf(true)
    override var settings by mutableStateOf(true)
    override var bookmark by mutableStateOf(true)
}

@Composable
fun ExportUserDataDialog(
    onDismissRequest: () -> Unit,
    onClickSaveAndSend: (ExportContext) -> Unit,
    onClickSaveToFile: (ExportContext) -> Unit
) {
    val mutableExportContext = remember { MutableExportContext() }
    val listItemModifier = Modifier
        .sizeIn(minWidth = 280.dp, maxWidth = 500.dp)
        .fillMaxWidth()
        .padding(horizontal = 14.dp)
    BaseDialog(
        icon = painterResource(R.drawable.output_24px),
        title = stringResource(R.string.settings_snap_data),
        description = stringResource(R.string.dialog_snap_user_data_text),
        onDismissRequest = onDismissRequest,
    ) {
        Column(Modifier.width(IntrinsicSize.Max).sizeIn(maxHeight = 350.dp)) {
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_bookshelf),
                supportingText = stringResource(R.string.dialog_snap_bookshelf_text),
                checked = mutableExportContext.bookshelf,
                onCheckedChange = { mutableExportContext.bookshelf = it }
            )
            HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_reading_data),
                supportingText = stringResource(R.string.dialog_snap_reading_data_text),
                checked = mutableExportContext.readingData,
                onCheckedChange = { mutableExportContext.readingData = it }
            )
            HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_settings),
                supportingText = stringResource(R.string.dialog_snap_settings_text),
                checked = mutableExportContext.settings,
                onCheckedChange = { mutableExportContext.settings = it }
            )
            /*HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_bookmarks),
                supportingText = stringResource(R.string.dialog_snap_bookmarks_text),
                checked = mutableExportContext.bookmark,
                onCheckedChange = { mutableExportContext.bookmark = it }
            )
            HorizontalDivider(Modifier.padding(horizontal = 14.dp))*/
        }
        Row(
            modifier = Modifier
                .padding(8.dp, 24.dp, 24.dp, 24.dp)
                .align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            TextButton(
                onClick = { onClickSaveAndSend(mutableExportContext) }
            ) {
                Text(
                    text = stringResource(R.string.export_and_share),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            TextButton(
                onClick = { onClickSaveToFile(mutableExportContext) }
            ) {
                Text(
                    text = stringResource(R.string.export_to_file),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

data class WebDataSourceItem(
    val id: Int,
    val name: String,
    val provider: String,
)

val wenku8ApiWebDataSourceItem = WebDataSourceItem(
    "wenku8".hashCode(),
    "Wenku8",
    "LightNovelReader from wenku8.net"
)

val zaiComicWebDataSourceItem = WebDataSourceItem(
    "ZaiComic".hashCode(),
    "ZaiComic",
    "LightNovelReader from zaimanhua.com"
)

@Composable
fun SourceChangeDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    webDataSourceItems: List<WebDataSourceItem>,
    selectedWebDataSourceId: Int,
    onClickItem: (Int) -> Unit
) {
    BaseDialog(
        icon = painterResource(R.drawable.public_24px),
        title = stringResource(R.string.settings_select_data_source),
        description = stringResource(R.string.dialog_select_data_source_text),
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        dismissText = stringResource(R.string.cancel),
        confirmationText = stringResource(R.string.switch_and_restart)
    ) {
        webDataSourceItems.forEachIndexed { index, webDataSourceItem ->
            RadioButtonListItem(
                modifier = Modifier
                    .sizeIn(minWidth = 280.dp, maxWidth = 500.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                title = webDataSourceItem.name,
                supportingText = stringResource(R.string.data_source_provider, webDataSourceItem.provider),
                selected = selectedWebDataSourceId == webDataSourceItem.id,
                onClick = { onClickItem(webDataSourceItem.id) }
            )
            if (index != webDataSourceItems.size - 1) {
                HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            }
        }
    }
}

@Composable
fun SettingsGitHubProxyDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (String) -> Unit,
    proxyUrlUserData: StringUserData,
) {
    val proxyUrl = proxyUrlUserData.getOrDefault("")
    var selectedOption by remember {
        mutableStateOf(
            ObjectOptions.GitHubProxyUrlOptions.optionsList.find { it.url == proxyUrl }
                ?: ObjectOptions.GitHubProxyUrlOptions.optionsList.first { it.key == "custom" }
        )
    }
    var input by remember { mutableStateOf(if (selectedOption.url == null) proxyUrl else "") }
    var isValid by remember { mutableStateOf(true) }

    AlertDialog (
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.settings_github_proxy)) },
        text = {
            Column {
                ObjectOptions.GitHubProxyUrlOptions.optionsList.forEach { option ->
                    RadioButtonListItem(
                        title = stringResource(option.name),
                        selected = selectedOption.key == option.key,
                        supportingText = stringResource(option.description),
                        onClick = { selectedOption = option },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.dialog_github_proxy_notice))
                if (selectedOption.key == "custom") {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        isError = !isValid,
                        value = input,
                        supportingText = {
                            Text(
                                stringResource(R.string.dialog_github_proxy_supporting_text),
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        onValueChange = {
                            isValid = (it.isEmpty() || proxyUrlRegex.matches(it))
                            input = it
                        },
                        label = {
                            Text(stringResource(R.string.dialog_github_proxy_custom_url))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isValid) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = stringResource(R.string.dialog_github_proxy_custom_url_hint) +
                                    "https://example.com/\n" +
                                    "https://nth.3rd.example.com/",
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }

            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (selectedOption.key) {
                        "custom" -> onConfirmation(input.ifBlank { "" })
                        "disabled" -> onConfirmation("")
                        else -> onConfirmation(selectedOption.url.toString().ifBlank { "" })
                    }
                }
            ) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SettingsAboutInfoDialog(
    onDismissRequest: () -> Unit,
) {
    AlertDialog (
        onDismissRequest = onDismissRequest,
        text = {
            Column {
                Row {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = colorResource(id = R.color.ic_launcher_background),
                        shape = CircleShape
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_foreground),
                            contentDescription = "appIcon",
                            modifier = Modifier.scale(1.4f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {

                        Text(
                            stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 18.sp
                        )
                        Text(
                            BuildConfig.APPLICATION_ID,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))


                Text(
                    text = stringResource(R.string.settings_about_oss),
                    fontSize = 14.sp,
                )
                val annotatedString = AnnotatedString.Companion.fromHtml(
                    htmlString = stringResource(
                        id = R.string.settings_about_source_code,
                        "<b><a href=\"https://github.com/dmzz-yyhyy/LightNovelReader\">GitHub</a></b>",
                        "<b><a href=\"https://github.com/dmzz-yyhyy/LightNovelReader/issues\">GitHub Issues</a></b>"
                    ),
                    linkStyles = TextLinkStyles(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        ),
                        pressedStyle = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            background = MaterialTheme.colorScheme.secondaryContainer,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                )
                Text(
                    text = annotatedString,
                    style = TextStyle(
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                val titleColor = MaterialTheme.colorScheme.onSurface
                val contentColor = MaterialTheme.colorScheme.secondary
                Column {
                    Text(
                        stringResource(R.string.dialog_about_version), color = titleColor
                    )
                    Text(
                        "${BuildConfig.VERSION_NAME} [${BuildConfig.VERSION_CODE}]", color = contentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.dialog_about_build_time), color = titleColor
                    )
                    Text(
                        stringResource(R.string.info_build_date), color = contentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(stringResource(R.string.dialog_about_build_host), color = titleColor)
                    Text(
                        stringResource(R.string.info_build_host), color = contentColor
                    )
                    Text(
                        stringResource(R.string.info_build_os), color = contentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.translators), color = titleColor
                    )
                    Text(
                        stringResource(R.string.language_translators), color = contentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
fun ExportToEpubDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog (
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.dialog_export_to_epub)) },
        text = {
            Text(stringResource(R.string.dialog_export_to_epub_text))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmation
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview
@Composable
fun ColorPickerDialogPreview() {
    ColorPickerDialog(
        onConfirmation = {},
        onDismissRequest = {},
        selectedColor = MaterialTheme.colorScheme.surfaceContainer
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (Color) -> Unit,
    selectedColor: Color
) {
    var currentColor by remember {
        mutableStateOf(selectedColor)
    }
    val colorList = listOf(
        MaterialTheme.colorScheme.surfaceContainer,
        Color(0x38FF9800),
        Color(0x38FF8080),
        Color(0x38FFCC00),
        Color(0x3834C759),
        Color(0x3832ADE6),
        Color(0x38007AFF),
        Color(0x385856D6),
        Color(0x38AF52DE)
    )
    BaseDialog (
        icon = painterResource(R.drawable.palette_24px),
        title = "调色盘",
        description = "选择一个颜色用于阅读器背景。",
        onDismissRequest = onDismissRequest,
        onConfirmation = { onConfirmation(currentColor) },
        dismissText = stringResource(R.string.cancel),
        confirmationText = stringResource(R.string.apply),
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            colorList.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null
                        ) {
                            currentColor = color
                        }
                ) {
                    val secondary = MaterialTheme.colorScheme.secondary
                    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
                    val blockIconId = painterResource(R.drawable.block_24px)
                    Canvas(
                        modifier = Modifier.size(44.dp)
                    ) {
                        if (currentColor == color)
                            drawCircle(
                                color = secondary,
                                radius = 22.dp.toPx(),
                            )

                        drawCircle(
                            color = surfaceContainer,
                            radius = 20.dp.toPx(),
                        )
                        drawCircle(
                            color = color,
                            radius = 20.dp.toPx(),
                        )
                    }
                    if (index == 0)
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            painter = blockIconId,
                            contentDescription = null
                        )
                }
            }
        }
    }
}