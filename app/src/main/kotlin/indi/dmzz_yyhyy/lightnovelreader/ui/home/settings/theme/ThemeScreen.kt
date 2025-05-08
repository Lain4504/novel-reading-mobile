package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.theme.LocalIsDarkTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.book.content.selectDataFile
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncherWithFlag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ThemeScreen(
    themeSettingState: SettingState,
    onClickBack: () -> Unit,
    onClickChangeTextColor: () -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(onClickBack)
        },
    ) {
        LazyColumn (
            modifier = Modifier.padding(it)
        ) {
            item {
                DarkModeSettings(themeSettingState)
            }
            item {
                ThemeSettingsList(themeSettingState)
            }
            item {
                ReaderThemeSettingsList(themeSettingState, onClickChangeTextColor)
            }
        }
    }
}

@Composable
fun DarkModeSettings(
    settingState: SettingState
) {
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        text = "深色模式",
        fontSize = 15.sp,
        fontWeight = FontWeight.W600
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LightThemeSettingsItem(settingState = settingState)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    modifier = Modifier.size(32.dp),
                    selected = settingState.darkModeKey == "Disabled",
                    onClick = { settingState.darkModeKeyUserData.asynchronousSet("Disabled") }
                )
                Text(stringResource(R.string.key_dark_mode_disabled), fontSize = 15.sp)
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DarkThemeSettingsItem(settingState = settingState)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    modifier = Modifier.size(32.dp),
                    selected = settingState.darkModeKey == "Enabled",
                    onClick = { settingState.darkModeKeyUserData.asynchronousSet("Enabled") }
                )
                Text(stringResource(R.string.key_dark_mode_enabled), fontSize = 15.sp)
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .height(170.dp)
                    .width(110.dp)
            ) {
                val shapeTop = GenericShape { size: Size, _ ->
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height / 2)
                    lineTo(0f, size.height / 2)
                    close()
                }

                val shapeBottom = GenericShape { size: Size, _ ->
                    moveTo(0f, size.height / 2)
                    lineTo(size.width, size.height / 2)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }

                val modifierTop = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        clip = true
                        shape = shapeTop
                    }

                val modifierBottom = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        clip = true
                        shape = shapeBottom
                    }

                LightThemeSettingsItem(modifier = modifierTop, settingState = settingState)
                DarkThemeSettingsItem(modifier = modifierBottom, settingState = settingState)
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    modifier = Modifier.size(32.dp),
                    selected = settingState.darkModeKey == "FollowSystem",
                    onClick = { settingState.darkModeKeyUserData.asynchronousSet("FollowSystem") }
                )
                Text(stringResource(R.string.key_dark_mode_follow_system), fontSize = 15.sp)
            }
        }
    }

}

@Composable
fun ThemeSettingsList(
    settingState: SettingState,
) {
    Spacer(Modifier.height(14.dp))
    SettingsSwitchEntry(
        modifier = Modifier.background(colorScheme.background),
        iconRes = R.drawable.format_color_fill_24px,
        title = stringResource(R.string.settings_dynamic_colors),
        description = stringResource(R.string.settings_dynamic_colors_desc),
        checked = settingState.dynamicColorsKey,
        booleanUserData = settingState.dynamicColorsKeyUserData,
        disabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    )
    if (!settingState.dynamicColorsKey) {
        SettingsMenuEntry(
            modifier = Modifier.background(colorScheme.background),
            iconRes = R.drawable.light_mode_24px,
            title = "浅色主题",
            description = if (LocalIsDarkTheme.current) "切换至浅色模式以预览主题" else "指定应用在浅色模式下的主题",
            options = MenuOptions.LightThemeNameOptions,
            selectedOptionKey = settingState.lightThemeName,
            onOptionChange = settingState.lightThemeNameUserData::asynchronousSet
        )
        SettingsMenuEntry(
            modifier = Modifier.background(colorScheme.background),
            iconRes = R.drawable.dark_mode_24px,
            title = "深色主题",
            description = if (LocalIsDarkTheme.current) "指定应用在深色模式下的主题" else "切换至深色模式以预览主题",
            options = MenuOptions.DarkThemeNameOptions,
            selectedOptionKey = settingState.darkThemeName,
            onOptionChange = settingState.darkThemeNameUserData::asynchronousSet
        )
    }
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        text = "字形",
        fontSize = 15.sp,
        fontWeight = FontWeight.W600
    )
    SettingsMenuEntry(
        modifier = Modifier.background(colorScheme.background),
        iconRes = R.drawable.translate_24px,
        title = stringResource(R.string.settings_characters_variant),
        description = stringResource(R.string.settings_characters_variant_desc),
        options = MenuOptions.AppLocaleOptions,
        selectedOptionKey = settingState.appLocaleKey,
        onOptionChange = settingState.appLocaleKeyUserData::asynchronousSet
    )
    key(settingState.appLocaleKey) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors().copy(
                    containerColor = colorScheme.surfaceContainer
                )
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    text = "预览文本\n熊浅骨新全微苗省澳火刃忍家辨边\n勇哥恐径尖底立牙丰北直耳亦食周",
                    fontWeight = FontWeight.W500
                )
            }
        }
    }
}

@Composable
fun ReaderThemeSettingsList(
    settingState: SettingState,
    onClickChangeTextColor: () -> Unit
) {
    val context = LocalContext.current
    val customBgIsEmpty = (settingState.backgroundImageUri.toString().isEmpty() && settingState.backgroundDarkImageUri.toString().isEmpty())

    Spacer(Modifier.height(12.dp))
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        text = "纸张",
        fontSize = 15.sp,
        fontWeight = FontWeight.W600
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            BackgroundImageSettingDefaultItem()
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier.size(32.dp),
                    selected = !settingState.enableBackgroundImage,
                    onClick = { settingState.enableBackgroundImageUserData.asynchronousSet(false) }
                )
                Text("无背景", fontSize = 15.sp)
            }
        }
        Column {
            BasePageItem(Modifier
                .width(110.dp)
                .height(170.dp)) {

                Box (
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.size(102.dp, 162.dp),
                        painter = rememberAsyncImagePainter(R.drawable.paper),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    Text("Aa 文字", color = colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                }

            }
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier.size(32.dp),
                    selected = (customBgIsEmpty && settingState.enableBackgroundImage),
                    onClick = {
                        settingState.enableBackgroundImageUserData.asynchronousSet(true)
                        settingState.backgroundImageUriUserData.asynchronousSet(Uri.EMPTY)
                        settingState.backgroundDarkImageUriUserData.asynchronousSet(Uri.EMPTY)
                    }
                )
                Text("内置", fontSize = 15.sp)
            }
        }
        Column {
            Box(
                modifier = Modifier
                    .height(170.dp)
                    .width(110.dp)
            ) {
                val shapeTop = GenericShape { size: Size, _ ->
                    val tiltOffset = size.height * 0.05f
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height / 2 - tiltOffset)
                    lineTo(0f, size.height / 2 + tiltOffset)
                    close()
                }

                val shapeBottom = GenericShape { size: Size, _ ->
                    val tiltOffset = size.height * 0.05f
                    moveTo(0f, size.height / 2 + tiltOffset)
                    lineTo(size.width, size.height / 2 - tiltOffset)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }

                val modifierTop = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        clip = true
                        shape = shapeTop
                    }

                val modifierBottom = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        clip = true
                        shape = shapeBottom
                    }

                val (launcher, setIsDarkFlag) = uriLauncherWithFlag { uri, isDark ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val time = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(
                            Date()
                        )
                        val fileName = if (isDark) "readerBackgroundImage_dark_$time" else "readerBackgroundImage_light_$time"
                        val image = context.filesDir.resolve(fileName).apply {
                            if (exists()) delete()
                            createNewFile()
                        }


                        try {
                            context.contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
                                FileInputStream(parcelFileDescriptor.fileDescriptor).use { fileInputStream ->
                                    fileInputStream.readBytes()
                                }.let(image::writeBytes)
                            }
                        } catch (e: Exception) {
                            Log.e("ReaderBackground", "failed to load chosen file (${if (isDark) "dark" else "light"})")
                            e.printStackTrace()
                        }

                        if (isDark) {
                            settingState.backgroundDarkImageUriUserData.set(image.toUri())
                        } else {
                            settingState.backgroundImageUriUserData.set(image.toUri())
                        }
                    }
                }


                LightBackgroundSettingsItem(modifierTop, settingState = settingState) {
                    setIsDarkFlag(false)
                    selectDataFile(launcher, "image/*")
                }

                DarkBackgroundSettingsItem(modifierBottom, settingState = settingState) {
                    setIsDarkFlag(true)
                    selectDataFile(launcher, "image/*")
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier.size(32.dp),
                    selected = !customBgIsEmpty && settingState.enableBackgroundImage,
                    onClick = {
                        settingState.enableBackgroundImageUserData.asynchronousSet(true)
                        if (customBgIsEmpty) Toast.makeText(context, "选择一个自定义图片以启用", Toast.LENGTH_SHORT).show()
                    }
                )
                Text("自定义", fontSize = 15.sp)
            }
        }
    }
    if (settingState.enableBackgroundImage) {
        SettingsMenuEntry(
            modifier = Modifier.background(colorScheme.background),
            title = "背景显示模式",
            iconRes = R.drawable.insert_page_break_24px,
            description = "指定自定义背景图片的显示模式",
            options = MenuOptions.ReaderBgImageDisplayModeOptions,
            selectedOptionKey = settingState.backgroundImageDisplayMode,
            stringUserData = settingState.backgroundImageDisplayModeUserData
        )
    }
    Spacer(Modifier.height(8.dp))
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        text = "文本",
        fontSize = 15.sp,
        fontWeight = FontWeight.W600
    )
    val onSecondaryContainer = colorScheme.onSecondaryContainer
    val background = colorScheme.background
    SettingsClickableEntry (
        modifier = Modifier.background(colorScheme.background),
        iconRes = R.drawable.palette_24px,
        title = "文本颜色",
        description = "自定义阅读器文本颜色",
        onClick = onClickChangeTextColor,
        trailingContent = {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.size(44.dp)
            ) {
                drawCircle(
                    color = onSecondaryContainer,
                    radius = 20.dp.toPx(),
                )
                drawCircle(
                    color = background,
                    radius = 17.5.dp.toPx(),
                )
                drawCircle(
                    color = if (settingState.textColor.isUnspecified) background else settingState.textColor,
                    radius = 17.5.dp.toPx(),
                )
            }
        }
    )
    BasePageItem(Modifier
        .fillMaxWidth()
        .height(260.dp)
        .padding(horizontal = 16.dp)
    ) {
        Box (
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val isDark = LocalIsDarkTheme.current

            if (settingState.enableBackgroundImage)
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter =
                        if (settingState.backgroundImageUri.toString().isEmpty()) painterResource(id = R.drawable.paper)
                        else
                            if (isDark)
                                rememberAsyncImagePainter(settingState.backgroundDarkImageUri)
                            else
                                rememberAsyncImagePainter(settingState.backgroundImageUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            Text(
                modifier = Modifier.padding(horizontal = 18.dp),
                text = stringResource(R.string.settings_about_oss),
                fontWeight = FontWeight.W500,
                textAlign = TextAlign.Center,
                color = settingState.textColor
            )
        }
    }
}

@Composable
private fun LightThemeSettingsItem(
    modifier: Modifier = Modifier, settingState: SettingState
) {
    MaterialTheme (
        if (settingState.dynamicColorsKey && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        dynamicLightColorScheme(context = LocalContext.current) else lightColorScheme()
    ) {
        DarkModeSettingItem(modifier)
    }
}

@Composable
private fun DarkThemeSettingsItem(
    modifier: Modifier = Modifier, settingState: SettingState
) {
    MaterialTheme (
        if (settingState.dynamicColorsKey && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            dynamicDarkColorScheme(context = LocalContext.current) else darkColorScheme()
    ) {
        DarkModeSettingItem(modifier)
    }
}

@Composable
private fun LightBackgroundSettingsItem(
    modifier: Modifier = Modifier,
    settingState: SettingState,
    onClickSelectImage: () -> Unit
) {
    MaterialTheme (
        if (settingState.dynamicColorsKey && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            dynamicLightColorScheme(context = LocalContext.current) else lightColorScheme()
    ) {
        BackgroundImageSettingItem(modifier, uri = settingState.backgroundImageUri, onClickSelectImage = { onClickSelectImage() })
    }
}

@Composable
private fun DarkBackgroundSettingsItem(
    modifier: Modifier = Modifier,
    settingState: SettingState,
    onClickSelectImage: () -> Unit
) {
    MaterialTheme (
        if (settingState.dynamicColorsKey && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            dynamicDarkColorScheme(context = LocalContext.current) else darkColorScheme()
    ) {
        BackgroundImageSettingItem(modifier, uri = settingState.backgroundDarkImageUri, onClickSelectImage = { onClickSelectImage() })
    }
}

@Composable
fun BackgroundImageSettingItem(
    modifier: Modifier,
    uri: Uri,
    onClickSelectImage: () -> Unit?
) {
    BasePageItem(modifier
        .width(110.dp)
        .height(170.dp)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box (
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .fillMaxSize()
            ) {
                Image(
                    modifier = Modifier.size(102.dp, 162.dp),
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,

                )
            }
            Text("Aa 文字", color = colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton({ onClickSelectImage() }) {
                        Icon(
                            painter = painterResource(R.drawable.library_add_24px),
                            tint = colorScheme.onSurface,
                            contentDescription = "cancel"
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton({ onClickSelectImage() }) {
                        Icon(
                            painter = painterResource(R.drawable.library_add_24px),
                            tint = colorScheme.onSurface,
                            contentDescription = "cancel"
                        )
                    }
                }
            }
        }

    }
    return
}

@Composable
private fun BackgroundImageSettingDefaultItem() {
    BasePageItem(Modifier
        .width(110.dp)
        .height(170.dp)) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .width(65.dp)
                        .height(8.dp)
                        .background(
                            color = colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                )
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                color = colorScheme.secondaryContainer,
                                shape = CircleShape
                            )
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .fillMaxWidth()
                        .height(55.dp)
                        .background(
                            color = colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .width(65.dp)
                        .height(8.dp)
                        .background(
                            color = colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                )
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                color = colorScheme.secondaryContainer,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun BasePageItem(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = colorScheme.background, // F1XME: incorrect colorScheme
                        shape = RoundedCornerShape(9.dp)
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                content()
            }
        }
    }
}

@Composable
private fun DarkModeSettingItem(
    modifier: Modifier
) {
    BasePageItem(
        modifier = modifier.width(110.dp)
            .height(170.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row {
                Box(
                    modifier = Modifier
                        .height(46.dp)
                        .width(36.dp)
                        .background(
                            color = colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier.width(56.dp)
                            .height(8.dp)
                            .background(
                                color = colorScheme.primaryContainer,
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(8.dp)
                            .background(
                                color = colorScheme.secondaryContainer,
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(8.dp)
                            .background(
                                color = colorScheme.secondaryContainer,
                                shape = CircleShape
                            )
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(8.dp)
                        .background(
                            color = colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            color = colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(8.dp)
                        .background(
                            color = colorScheme.inversePrimary,
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(8.dp)
                        .background(
                            color = colorScheme.tertiaryContainer,
                            shape = CircleShape
                        )
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 5.dp, vertical = 8.dp)
                .height(height = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(18.dp)
                    .background(
                        color = colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.settings_theme))
            }
        },
        navigationIcon = {
            IconButton(onClickBack) {
                Icon(
                    painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
    )
}