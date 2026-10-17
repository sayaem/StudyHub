package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkMidnightSurface
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextWhite
import com.example.ui.theme.MintGreen
import com.example.ui.theme.NavyText
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateMuted
import com.example.util.AppLanguage
import com.example.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun RevisionTrackerScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val revisionTab by viewModel.revisionTab.collectAsStateWithLifecycle()
    val allItems by viewModel.revisionItems.collectAsStateWithLifecycle()

    var showAddRevisionDialog by remember { mutableStateOf(false) }

    val filteredItems = allItems.filter {
        if (revisionTab == "PENDING") !it.isCompleted else it.isCompleted
    }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    val now = System.currentTimeMillis()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("revision_tracker_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar with Back Arrow + "Revision Tracker"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = AppStrings.revisionTitle(lang),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = textPrimary
                )
            }
        }

        // Info Banner: Spaced Revision Intervals explanation
        item {
            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (isDarkTheme) Color(0xFF1E293B) else RoyalBlueLight.copy(alpha = 0.5f),
                borderColor = RoyalBlue.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(RoyalBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "স্পেসড রিভিশন পদ্ধতি (১-৩-৭-১৪ দিন)" else "Spaced Revision Intervals (1-3-7-14 Days)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = if (lang == AppLanguage.BANGLA)
                                "অধ্যায় শেষ করার পর যথাসময়ে রিভিশন সম্পন্ন করলে দীর্ঘমেয়াদী স্মৃতিতে স্থায়ী হয়।"
                            else
                                "Timely spaced reviews dramatically increase memory retention for admission tests.",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }
            }
        }

        // Tabs: Pending vs Completed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SyllabusTopPill(
                    label = AppStrings.tabPending(lang),
                    isSelected = revisionTab == "PENDING",
                    isDarkTheme = isDarkTheme,
                    onClick = { viewModel.setRevisionTab("PENDING") }
                )

                SyllabusTopPill(
                    label = AppStrings.tabCompletedRevs(lang),
                    isSelected = revisionTab == "COMPLETED",
                    isDarkTheme = isDarkTheme,
                    onClick = { viewModel.setRevisionTab("COMPLETED") }
                )
            }
        }

        if (filteredItems.isEmpty()) {
            item {
                StudyHubCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = cardBg,
                    borderColor = borderCol
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = AppStrings.emptyRevisionTitle(lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppStrings.emptyRevisionDesc(lang),
                            fontSize = 12.sp,
                            color = textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Revision Items List
            items(filteredItems, key = { it.id }) { item ->
                val icon = when {
                    item.subjectName.contains("Physics", ignoreCase = true) -> Icons.Default.Science
                    item.subjectName.contains("Chemistry", ignoreCase = true) -> Icons.Default.Science
                    item.subjectName.contains("Math", ignoreCase = true) -> Icons.Default.Calculate
                    else -> Icons.Default.Book
                }

                val iconBg = if (item.completedCount > 0) {
                    if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) else RoyalBlueLight
                } else {
                    if (isDarkTheme) Color(0xFF1E2C46) else Color(0xFFF1F5F9)
                }

                val iconTint = if (item.completedCount > 0) RoyalBlue else SlateMuted

                val isOverdue = !item.isCompleted && item.nextRevisionDate < (now - 3600000L)
                val isDueToday = !item.isCompleted && !isOverdue && item.nextRevisionDate <= (now + 86400000L)

                val statusBadgeColor = when {
                    item.isCompleted -> MintGreen
                    isOverdue -> AlertRed
                    isDueToday -> Color(0xFFF59E0B)
                    else -> RoyalBlue
                }

                val dueText = if (item.isCompleted) {
                    if (lang == AppLanguage.BANGLA) "রিভিশন সম্পূর্ণ" else "Fully Mastered"
                } else if (isOverdue) {
                    if (lang == AppLanguage.BANGLA) "রিভিশন বাকি (দেরি)" else "Overdue"
                } else {
                    val daysUntil = TimeUnit.MILLISECONDS.toDays(item.nextRevisionDate - now).toInt()
                    if (daysUntil <= 0) {
                        if (lang == AppLanguage.BANGLA) "আজকের রিভিশন" else "Due Today"
                    } else {
                        if (lang == AppLanguage.BANGLA) "${AppStrings.toBengaliDigits(daysUntil.toString())} দিন পর" else "Due in ${daysUntil}d"
                    }
                }

                StudyHubCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = cardBg,
                    borderColor = if (isOverdue) AlertRed.copy(alpha = 0.4f) else borderCol
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(iconBg, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.subjectName,
                                            fontSize = 11.sp,
                                            color = textSecondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = statusBadgeColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = dueText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusBadgeColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "${AppStrings.formatNumber(item.completedCount, lang)} / ${AppStrings.formatNumber(item.totalCount, lang)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSecondary
                            )
                        }

                        if (!item.isCompleted) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Postpone 1 day button
                                TextButton(
                                    onClick = { viewModel.postponeRevision(item.id, 1) },
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = textSecondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "+১ দিন পেছান" else "Postpone 1d",
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Mark step revised button
                                Button(
                                    onClick = { viewModel.markRevisionStepCompleted(item.id, item.completedCount, item.totalCount) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "রিভাইজড" else "Mark Revised",
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { viewModel.deleteRevision(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = AppStrings.deleteAction(lang),
                                        tint = textSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Button: "+ Add Revision"
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { showAddRevisionDialog = true }
                    .testTag("add_revision_button"),
                color = if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) else RoyalBlueLight,
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.addRevisionTopic(lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                }
            }
        }
    }

    if (showAddRevisionDialog) {
        var topicTitle by remember { mutableStateOf("") }
        var subjectName by remember { mutableStateOf("") }
        var totalTarget by remember { mutableIntStateOf(4) }

        AlertDialog(
            onDismissRequest = { showAddRevisionDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = AppStrings.addRevisionTopic(lang),
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = topicTitle,
                        onValueChange = { topicTitle = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "টপিকের নাম (যেমনঃ তাপগতিবিদ্যা)" else "Topic Title (e.g. Thermodynamics)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = borderCol,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "বিষয়ের নাম (যেমনঃ পদার্থবিজ্ঞান)" else "Subject Name (e.g. Physics)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = borderCol,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                    OutlinedTextField(
                        value = totalTarget.toString(),
                        onValueChange = { totalTarget = it.toIntOrNull() ?: 4 },
                        label = { Text(if (lang == AppLanguage.BANGLA) "লক্ষ্যমাত্রা রিভিশন সংখ্যা" else "Target Revisions (e.g. 4)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = borderCol,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topicTitle.isNotBlank()) {
                            viewModel.addRevisionItem(topicTitle, subjectName, totalTarget)
                            showAddRevisionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(AppStrings.addRevisionTopic(lang), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRevisionDialog = false }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }
}
