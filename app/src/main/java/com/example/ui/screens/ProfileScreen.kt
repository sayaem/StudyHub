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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.AlertRed
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
import com.example.util.AppStrings

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsStateWithLifecycle()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showConfirmClearDialog by remember { mutableStateOf(false) }
    var showConfirmDemoDialog by remember { mutableStateOf(false) }

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
        // Candidate Avatar & Dynamic Name Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(RoyalBlueLight)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_avatar),
                        contentDescription = userName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showEditNameDialog = true }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = AppStrings.editNameTitle(lang),
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = AppStrings.candidateSubtitle(lang),
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
                        title = AppStrings.switchLanguage(lang),
                        value = if (lang == AppLanguage.BANGLA) "বাংলা" else "English",
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
                        title = AppStrings.darkModeSetting(lang),
                        value = if (isDarkTheme) AppStrings.darkModeLabel(lang) else AppStrings.lightModeLabel(lang),
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
                        title = AppStrings.notificationsSetting(lang),
                        value = if (isNotificationsEnabled) AppStrings.notifEnabled(lang) else AppStrings.notifDisabled(lang),
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { viewModel.toggleNotifications() }
                    )
                }
            }
        }

        // Settings Card 2: Analytics, About, Help, App Version
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
                        title = AppStrings.analyticsTitle(lang),
                        value = AppStrings.viewAll(lang),
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
                        title = AppStrings.sectionAbout(lang),
                        value = null,
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { showAboutDialog = true }
                    )

                    HorizontalDivider(
                        color = if (isDarkTheme) DarkBorder else SlateBorder,
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Help & Support
                    ProfileSettingRow(
                        icon = Icons.Default.HelpOutline,
                        title = AppStrings.helpAbout(lang),
                        value = null,
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { showHelpDialog = true }
                    )

                    HorizontalDivider(
                        color = if (isDarkTheme) DarkBorder else SlateBorder,
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // App Version
                    ProfileSettingRow(
                        icon = Icons.Default.Info,
                        title = AppStrings.appVersionLabel(lang),
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

        // Settings Card 3: Explicit Demo Mode & Data Management
        item {
            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderCol
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // Load Demo Data
                    ProfileSettingRow(
                        icon = Icons.Default.AutoAwesome,
                        title = AppStrings.loadDemoData(lang),
                        value = null,
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { showConfirmDemoDialog = true }
                    )

                    HorizontalDivider(
                        color = if (isDarkTheme) DarkBorder else SlateBorder,
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Clear All Data
                    ProfileSettingRow(
                        icon = Icons.Default.DeleteOutline,
                        title = AppStrings.clearData(lang),
                        value = null,
                        isDarkTheme = isDarkTheme,
                        textPrimary = AlertRed,
                        textSecondary = textSecondary,
                        onClick = { showConfirmClearDialog = true }
                    )
                }
            }
        }
    }

    if (showEditNameDialog) {
        var newName by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = AppStrings.editNameTitle(lang),
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(AppStrings.enterNamePrompt(lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_candidate_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = borderCol,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.updateUserName(newName)
                        }
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(AppStrings.saveChanges(lang), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = AppStrings.sectionAbout(lang),
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = AppStrings.helpContent(lang),
                    color = textSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(if (lang == AppLanguage.BANGLA) "বন্ধ করুন" else "Close", color = RoyalBlue)
                }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = AppStrings.helpAbout(lang),
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = AppStrings.helpContent(lang),
                    color = textSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(if (lang == AppLanguage.BANGLA) "বুঝেছি" else "Got it", color = RoyalBlue)
                }
            }
        )
    }

    if (showConfirmDemoDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDemoDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = AppStrings.loadDemoData(lang),
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = if (lang == AppLanguage.BANGLA)
                        "এটি বাংলাদেশি বিশ্ববিদ্যালয় ভর্তি পরীক্ষার নমুনা বিষয় (পদার্থ, রসায়ন, গণিত, ইংরেজি ইত্যাদি) ও ক্যালেন্ডার ডেডলাইন লোড করবে।"
                    else
                        "This loads representative university admission subjects (Physics, Chemistry, Math, English, Biology), past sessions, and calendar deadlines.",
                    color = textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadSampleDemoData()
                        showConfirmDemoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(if (lang == AppLanguage.BANGLA) "লোড করুন" else "Load Demo", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDemoDialog = false }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = AppStrings.clearData(lang),
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = if (lang == AppLanguage.BANGLA)
                        "আপনার সকল সংরক্ষিত বিষয়, অধ্যায়, স্টাডি সেশন এবং ভর্তি লক্ষ্য মুছে সম্পূর্ণ খালি ওয়ার্কস্পেস তৈরি হবে।"
                    else
                        "This will clear all subjects, chapters, study sessions, and admission targets, leaving you with a fresh empty workspace.",
                    color = textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showConfirmClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text(if (lang == AppLanguage.BANGLA) "সব মুছুন" else "Clear Everything", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }
}

@Composable
fun ProfileSettingRow(
    icon: ImageVector,
    title: String,
    value: String?,
    isDarkTheme: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
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
