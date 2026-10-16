package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkMidnightSurface
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextWhite
import com.example.ui.theme.NavyText
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateMuted
import com.example.util.AppLanguage

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Candidate Avatar & Name Header matching Screen 9
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                // Profile Avatar Photo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(RoyalBlueLight)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_avatar),
                        contentDescription = "Arafat Rahman",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Arafat Rahman",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Always learning",
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }
        }

        // Settings Card 1: Language, Theme, Notifications
        item {
            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderCol
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // Language
                    ProfileSettingRow(
                        icon = Icons.Default.Language,
                        title = "Language",
                        value = if (lang == AppLanguage.BANGLA) "বাংলা / English" else "English / বাংলা",
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { viewModel.toggleLanguage() }
                    )

                    HorizontalDivider(
                        color = if (isDarkTheme) DarkBorder else SlateBorder,
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Theme
                    ProfileSettingRow(
                        icon = if (isDarkTheme) Icons.Default.Brightness4 else Icons.Default.WbSunny,
                        title = "Theme",
                        value = if (isDarkTheme) "Dark" else "Light",
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { viewModel.toggleDarkTheme() }
                    )

                    HorizontalDivider(
                        color = if (isDarkTheme) DarkBorder else SlateBorder,
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Notifications
                    ProfileSettingRow(
                        icon = Icons.Default.NotificationsNone,
                        title = "Notifications",
                        value = "On",
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { /* Toggle notifications */ }
                    )
                }
            }
        }

        // Settings Card 2: About, Help, App Version
        item {
            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderCol
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // View Progress Analytics
                    ProfileSettingRow(
                        icon = Icons.Default.Info,
                        title = "Progress Analytics",
                        value = "View",
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { onNavigate(ScreenDestination.PROGRESS_ANALYTICS) }
                    )

                    HorizontalDivider(
                        color = if (isDarkTheme) DarkBorder else SlateBorder,
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // About StudyHub
                    ProfileSettingRow(
                        icon = Icons.Default.Info,
                        title = "About StudyHub",
                        value = null,
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { /* About dialog */ }
                    )

                    HorizontalDivider(
                        color = if (isDarkTheme) DarkBorder else SlateBorder,
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Help & Support
                    ProfileSettingRow(
                        icon = Icons.Default.HelpOutline,
                        title = "Help & Support",
                        value = null,
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { /* Help */ }
                    )

                    HorizontalDivider(
                        color = if (isDarkTheme) DarkBorder else SlateBorder,
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // App Version
                    ProfileSettingRow(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        value = "1.0.0",
                        showChevron = false,
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSettingRow(
    icon: ImageVector,
    title: String,
    value: String?,
    showChevron: Boolean = true,
    isDarkTheme: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) else RoyalBlueLight,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RoyalBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = textSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
