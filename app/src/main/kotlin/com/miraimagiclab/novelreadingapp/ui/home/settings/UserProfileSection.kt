package com.miraimagiclab.novelreadingapp.ui.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.miraimagiclab.novelreadingapp.data.auth.UserInfo
import io.lain4504.novelreadingapp.api.ui.theme.AppTypography

@Composable
fun UserProfileSection(
    user: UserInfo?,
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant
        )
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colorScheme.primary
                )
            }
        } else if (user != null) {
            // User is logged in - show profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.avatarUrl != null && user.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = "User avatar",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback: Show initial letter
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .background(colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user.displayName ?: user.username).take(1).uppercase(),
                                style = AppTypography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // User info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = user.displayName ?: user.username,
                        style = AppTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "@${user.username}",
                        style = AppTypography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // User is not logged in - show login button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Đăng nhập để đồng bộ dữ liệu",
                    style = AppTypography.bodyLarge,
                    color = colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Đăng nhập")
                }
            }
        }
    }
}

