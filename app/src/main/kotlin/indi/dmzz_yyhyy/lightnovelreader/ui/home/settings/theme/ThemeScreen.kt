package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.content.selectDataFile
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream

@Composable
fun ThemeScreen(
    themeSettingState: ThemeSettingState,
    onClickBack: () -> Unit
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
                ReaderThemeSettingsList(themeSettingState)
            }
        }
    }
}

@Composable
fun DarkModeSettings(
    settingState: ThemeSettingState
) {
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        text = "深色主题",
        fontSize = 16.sp,
        fontWeight = FontWeight.W600
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(16.dp)
    ) {
        Column {
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
        Column {
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
        Column {
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

                LightThemeSettingsItem(modifierTop, settingState = settingState)
                DarkThemeSettingsItem(modifierBottom, settingState = settingState)
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
    settingState: ThemeSettingState
) {
    Spacer(Modifier.height(14.dp))
    SettingsSwitchEntry(
        modifier = Modifier.background(colorScheme.surface),
        iconRes = R.drawable.format_color_fill_24px,
        title = stringResource(R.string.settings_dynamic_colors),
        description = stringResource(R.string.settings_dynamic_colors_desc),
        checked = settingState.dynamicColorsKey,
        booleanUserData = settingState.dynamicColorsKeyUserData,
        disabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    )
    SettingsMenuEntry(
        modifier = Modifier.background(colorScheme.surface),
        iconRes = R.drawable.translate_24px,
        title = stringResource(R.string.settings_characters_variant),
        description = stringResource(R.string.settings_characters_variant_desc),
        options = MenuOptions.AppLocaleOptions,
        selectedOptionKey = settingState.appLocaleKey,
        onOptionChange = settingState.appLocaleKeyUserData::asynchronousSet
    )
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        text = "字形预览",
        fontSize = 16.sp,
        fontWeight = FontWeight.W600
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
                    modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                    text = "熊浅骨新全微苗省澳火刃忍家辨边\n勇哥恐径尖底立牙丰北直耳亦食周",
                    fontWeight = FontWeight.W500
                )
            }
        }
    }
}

@Composable
fun ReaderThemeSettingsList(
    settingState: ThemeSettingState
) {
    Spacer(Modifier.height(8.dp))
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        text = "阅读器设置",
        fontSize = 16.sp,
        fontWeight = FontWeight.W600
    )
    SettingsSwitchEntry(
        modifier = Modifier.background(colorScheme.surface),
        iconRes = R.drawable.imagesearch_roller_24px,
        title = "背景图片",
        description = "自定义阅读器背景图片",
        checked = settingState.enableBackgroundImage,
        booleanUserData = settingState.enableBackgroundImageUserData
    )
    if (settingState.enableBackgroundImage) {
            val content = LocalContext.current
            val launcher = uriLauncher {
                CoroutineScope(Dispatchers.IO).launch {
                    val image = content.filesDir.resolve("readerBackgroundImage")
                        .also {
                            if (it.exists()) {
                                it.delete()
                                it.createNewFile()
                            }
                            else it.createNewFile()
                        }
                    try {
                        content.contentResolver.openFileDescriptor(it, "r")?.use { parcelFileDescriptor ->
                            FileInputStream(parcelFileDescriptor.fileDescriptor).use { fileInputStream ->
                                fileInputStream.readBytes()
                            }.let(image::writeBytes)
                        }
                    } catch (e: Exception) {
                        Log.e("ReaderBackground", "failed to load chosen file")
                        e.printStackTrace()
                    }
                    settingState.backgroundImageUriUserData.set(image.toUri())
                }
            }
            SettingsMenuEntry(
                modifier = Modifier.background(colorScheme.surface),
                iconRes = R.drawable.drive_file_move_24px,
                title = "选择图片",
                description = "使用应用内置的图片背景或自定义图片文件",
                options = MenuOptions.SelectImage,
                selectedOptionKey = if (settingState.backgroundImageUri.toString().isEmpty()) MenuOptions.SelectImage.Default else MenuOptions.SelectImage.Customize,
                onOptionChange = {
                    when (it) {
                        MenuOptions.SelectImage.Default -> settingState.backgroundImageUriUserData.asynchronousSet(
                            Uri.EMPTY)
                        MenuOptions.SelectImage.Customize -> selectDataFile(launcher, "image/*")
                    }
                }
            )
            SettingsMenuEntry(
                modifier = Modifier.background(colorScheme.surface),
                title = "背景显示模式",
                iconRes = R.drawable.insert_page_break_24px,
                description = "指定自定义背景图片的显示模式",
                options = MenuOptions.ReaderBgImageDisplayModeOptions,
                selectedOptionKey = settingState.backgroundImageDisplayMode,
                stringUserData = settingState.backgroundImageDisplayModeUserData
            )
    }
}

@Composable
fun LightThemeSettingsItem(
    modifier: Modifier = Modifier, settingState: ThemeSettingState
) {
    MaterialTheme (
        if (settingState.dynamicColorsKey && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        dynamicLightColorScheme(context = LocalContext.current) else lightColorScheme()
    ) {
        ThemeSettingItem(modifier)
    }
}

@Composable
fun DarkThemeSettingsItem(
    modifier: Modifier = Modifier, settingState: ThemeSettingState
) {
    MaterialTheme (
        if (settingState.dynamicColorsKey && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            dynamicDarkColorScheme(context = LocalContext.current) else darkColorScheme()
    ) {
        ThemeSettingItem(modifier)
    }
}

@Composable
fun ThemeSettingItem(
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(170.dp)
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
                            color = colorScheme.background,
                            shape = RoundedCornerShape(9.dp)
                        ),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
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
                                    modifier = Modifier
                                        .width(56.dp)
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
                Text("主题与纸张")
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