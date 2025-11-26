package com.miraimagiclab.novelreadingapp.ui.home.settings

import android.net.Uri
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.miraimagiclab.novelreadingapp.ui.SharedContentKey
import com.miraimagiclab.novelreadingapp.ui.auth.UserViewModel
import com.miraimagiclab.novelreadingapp.ui.auth.navigateToLogin
import com.miraimagiclab.novelreadingapp.ui.home.HomeNavigateBar
import com.miraimagiclab.novelreadingapp.ui.home.settings.list.AboutSettingsList
import com.miraimagiclab.novelreadingapp.ui.home.settings.list.ReadingSettingsList
import com.miraimagiclab.novelreadingapp.ui.home.settings.list.UpdatesSettingsList
import com.miraimagiclab.novelreadingapp.utils.LocalSnackbarHost
import com.miraimagiclab.novelreadingapp.R
import io.lain4504.novelreadingapp.api.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SettingsScreen(
    controller: NavController,
    selectedRoute: Any,
    settingState: SettingState,
    onClickThemeSettings: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
) {
    val userViewModel: UserViewModel = hiltViewModel()
    val userUiState by userViewModel.uiState.collectAsState()
    
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    with(sharedTransitionScope) {
        Scaffold(
            topBar = { TopBar(pinnedScrollBehavior) },
            bottomBar = {
                HomeNavigateBar(
                    Modifier.sharedElement(
                        sharedTransitionScope.rememberSharedContentState(SharedContentKey.HomeNavigateBar),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    selectedRoute,
                    controller
                )
            },
            snackbarHost = {
                SnackbarHost(LocalSnackbarHost.current)
            }
        ) {
            Column(
                Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState())
                    .nestedScroll(pinnedScrollBehavior.nestedScrollConnection)
            ) {
                // User Profile Section - shown at the top
                UserProfileSection(
                    user = userUiState.user,
                    isLoading = userUiState.isLoading,
                    onLoginClick = {
                        controller.navigateToLogin()
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                SettingsCategory(
                    title = stringResource(R.string.app_updates)
                ) {
                    UpdatesSettingsList(
                        settingState = settingState,
                    )
                }
                SettingsCategory(
                    title = stringResource(R.string.reading_settings),
                ) {
                    ReadingSettingsList(
                        onClickTheme = onClickThemeSettings
                    )
                }


                SettingsCategory(
                    title = stringResource(R.string.about_settings),
                ) {
                    AboutSettingsList(
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_settings),
                style = AppTypography.titleTopBar,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            Box(Modifier.size(48.dp)) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(id = R.drawable.outline_settings_24px),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun SettingsCategory(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    title?.let {
        Text(
            modifier = Modifier.padding(horizontal = 24.dp)
                .padding(vertical = 12.dp),
            text = it,
            color = colorScheme.onSurfaceVariant,
            style = AppTypography.titleSmall,
            fontWeight = FontWeight.W600
        )
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        content()
    }
}
