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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.ChapterEntity
import com.example.data.model.SubjectEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AddChapterBottomSheet
import com.example.ui.components.ChapterStatusChip
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedLight
import com.example.ui.theme.AmberWarning
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

@Composable
fun SubjectDetailsScreen(
    viewModel: MainViewModel,
    subjectId: Long,
    subjects: List<SubjectEntity>,
    chapters: List<ChapterEntity>,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subject = subjects.find { it.id == subjectId } ?: subjects.firstOrNull()
    val subjectChapters = chapters.filter { it.subjectId == (subject?.id ?: 0L) }
    val completedCount = subjectChapters.count { it.status == "COMPLETED" }
    val totalCount = subjectChapters.size
    val percentage = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat() * 100).toInt() else 0

    var activeTab by remember { mutableStateOf("CHAPTERS") }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var chapterToDelete by remember { mutableStateOf<ChapterEntity?>(null) }
    var chapterToEdit by remember { mutableStateOf<ChapterEntity?>(null) }
    var showEditSubjectDialog by remember { mutableStateOf(false) }
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    val subjectColor = try {
        Color(android.graphics.Color.parseColor(subject?.colorHex ?: "#2563EB"))
    } catch (e: Exception) {
        RoyalBlue
    }

    val icon = when {
        subject?.nameEn?.contains("Physics", ignoreCase = true) == true -> Icons.Default.Science
        subject?.nameEn?.contains("Chemistry", ignoreCase = true) == true -> Icons.Default.Science
        subject?.nameEn?.contains("Math", ignoreCase = true) == true -> Icons.Default.Calculate
        subject?.nameEn?.contains("ICT", ignoreCase = true) == true || subject?.nameEn?.contains("Computer", ignoreCase = true) == true -> Icons.Default.Computer
        subject?.nameEn?.contains("Biology", ignoreCase = true) == true -> Icons.Default.Eco
        else -> Icons.Default.MenuBook
    }

    val displayName = if (lang == AppLanguage.BANGLA && subject?.nameBn?.isNotBlank() == true)
        subject.nameBn else (subject?.nameEn ?: "")

    Box(modifier = modifier.fillMaxSize().testTag("subject_details_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Bar with Back Arrow, Title, Edit & Delete Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = textPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (subject != null) {
                            IconButton(
                                onClick = { showEditSubjectDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = AppStrings.editAction(lang),
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { showDeleteSubjectDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = AppStrings.deleteAction(lang),
                                    tint = AlertRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Subject Hero Card
            item {
                StudyHubCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = cardBg,
                    borderColor = borderCol
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            subjectColor.copy(alpha = 0.15f),
                                            RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = subjectColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        ),
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = AppStrings.chaptersCount(completedCount, totalCount, lang),
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }

                            Text(
                                text = "${AppStrings.formatNumber(percentage, lang)}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (percentage == 100) MintGreen else textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val barProgress = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = { barProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = subjectColor,
                            trackColor = if (isDarkTheme) Color(0xFF1E2C46) else Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            // Tabs: Chapters / Overview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabChapters = AppStrings.chaptersTab(lang)
                    val tabOverview = AppStrings.overviewTab(lang)

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeTab = "CHAPTERS" },
                        color = if (activeTab == "CHAPTERS") RoyalBlue else (if (isDarkTheme) DarkMidnightSurface else Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (activeTab == "CHAPTERS") RoyalBlue else borderCol
                        )
                    ) {
                        Text(
                            text = tabChapters,
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = if (activeTab == "CHAPTERS") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeTab == "CHAPTERS") Color.White else textSecondary
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeTab = "OVERVIEW" },
                        color = if (activeTab == "OVERVIEW") RoyalBlue else (if (isDarkTheme) DarkMidnightSurface else Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (activeTab == "OVERVIEW") RoyalBlue else borderCol
                        )
                    ) {
                        Text(
                            text = tabOverview,
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = if (activeTab == "OVERVIEW") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeTab == "OVERVIEW") Color.White else textSecondary
                        )
                    }
                }
            }

            if (activeTab == "CHAPTERS") {
                if (subjectChapters.isEmpty()) {
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
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(RoyalBlueLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = AppStrings.emptyChaptersTitle(lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppStrings.emptyChaptersDesc(lang),
                                    fontSize = 12.sp,
                                    color = textSecondary,
                                    lineHeight = 16.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(subjectChapters, key = { _, c -> c.id }) { index, chapter ->
                        val chapTitle = if (lang == AppLanguage.BANGLA && chapter.titleBn.isNotBlank())
                            chapter.titleBn else chapter.titleEn

                        StudyHubCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = cardBg,
                            borderColor = borderCol
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
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
                                                .size(28.dp)
                                                .background(
                                                    if (chapter.status == "COMPLETED") MintGreen.copy(alpha = 0.15f)
                                                    else (if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (chapter.status == "COMPLETED") {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MintGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = AppStrings.formatNumber(index + 1, lang),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textSecondary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = chapTitle,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = textPrimary
                                            )
                                            if (chapter.estimatedMinutes > 0) {
                                                val h = chapter.estimatedMinutes / 60
                                                val m = chapter.estimatedMinutes % 60
                                                val durText = if (h > 0) "${AppStrings.formatNumber(h, lang)}h ${AppStrings.formatNumber(m, lang)}m"
                                                    else "${AppStrings.formatNumber(m, lang)}m"
                                                Text(
                                                    text = durText,
                                                    fontSize = 11.sp,
                                                    color = textSecondary
                                                )
                                            }
                                        }
                                    }

                                    // Status cycle Chip
                                    val nextStatus = when (chapter.status) {
                                        "NOT_STARTED" -> "IN_PROGRESS"
                                        "IN_PROGRESS" -> "COMPLETED"
                                        else -> "NOT_STARTED"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.updateChapterStatus(chapter.id, nextStatus)
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        ChapterStatusChip(
                                            status = chapter.status,
                                            lang = lang
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Bottom action row: Revision Flag & Edit / Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.toggleChapterRevision(chapter.id, !chapter.isRevisionNeeded)
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (chapter.isRevisionNeeded) Icons.Default.Star else Icons.Outlined.StarBorder,
                                            contentDescription = null,
                                            tint = if (chapter.isRevisionNeeded) AmberWarning else textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (chapter.isRevisionNeeded) AppStrings.needsRevision(lang) else AppStrings.markRevised(lang),
                                            fontSize = 11.sp,
                                            color = if (chapter.isRevisionNeeded) AmberWarning else textSecondary,
                                            fontWeight = if (chapter.isRevisionNeeded) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { chapterToEdit = chapter },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = AppStrings.editAction(lang),
                                                tint = textSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { chapterToDelete = chapter },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = AppStrings.deleteAction(lang),
                                                tint = AlertRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Overview Tab
                item {
                    val inProgressCount = subjectChapters.count { it.status == "IN_PROGRESS" }
                    val notStartedCount = subjectChapters.count { it.status == "NOT_STARTED" }
                    val revisionCount = subjectChapters.count { it.isRevisionNeeded }
                    val totalMinutes = subjectChapters.sumOf { it.estimatedMinutes }
                    val totalHours = totalMinutes / 60

                    StudyHubCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = cardBg,
                        borderColor = borderCol
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "পরিসংখ্যান ও সারসংক্ষেপ" else "Subject Statistics & Breakdown",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )

                            OverviewRow(
                                label = if (lang == AppLanguage.BANGLA) "মোট অধ্যায়" else "Total Chapters",
                                value = AppStrings.formatNumber(totalCount, lang),
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            OverviewRow(
                                label = if (lang == AppLanguage.BANGLA) "সম্পন্ন হয়েছে" else "Completed Chapters",
                                value = AppStrings.formatNumber(completedCount, lang),
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            OverviewRow(
                                label = if (lang == AppLanguage.BANGLA) "চলমান" else "In Progress",
                                value = AppStrings.formatNumber(inProgressCount, lang),
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            OverviewRow(
                                label = if (lang == AppLanguage.BANGLA) "শুরু হয়নি" else "Not Started",
                                value = AppStrings.formatNumber(notStartedCount, lang),
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            OverviewRow(
                                label = if (lang == AppLanguage.BANGLA) "রিভিশন প্রয়োজন" else "Flagged for Revision",
                                value = AppStrings.formatNumber(revisionCount, lang),
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            OverviewRow(
                                label = if (lang == AppLanguage.BANGLA) "আনুমানিক অধ্যয়ন সময়" else "Total Estimated Study",
                                value = "${AppStrings.formatNumber(totalHours, lang)}h",
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button on Bottom Right
        FloatingActionButton(
            onClick = { showAddChapterDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 88.dp, end = 20.dp)
                .testTag("fab_add_chapter"),
            containerColor = RoyalBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = AppStrings.addChapter(lang))
        }
    }

    if (showAddChapterDialog && subject != null) {
        AddChapterBottomSheet(
            subjects = listOf(subject),
            preselectedSubjectId = subject.id,
            isDarkTheme = isDarkTheme,
            lang = lang,
            onDismiss = { showAddChapterDialog = false },
            onConfirm = { subId, titleEn, titleBn, priority, mins ->
                viewModel.addChapter(subId, titleEn, titleBn, priority, mins)
                showAddChapterDialog = false
            }
        )
    }

    // Edit Chapter Dialog
    if (chapterToEdit != null) {
        val editing = chapterToEdit!!
        var editTitleEn by remember { mutableStateOf(editing.titleEn) }
        var editTitleBn by remember { mutableStateOf(editing.titleBn) }
        var editMins by remember { mutableIntStateOf(editing.estimatedMinutes) }

        AlertDialog(
            onDismissRequest = { chapterToEdit = null },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = AppStrings.editChapterTitle(lang),
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitleEn,
                        onValueChange = { editTitleEn = it },
                        label = { Text(AppStrings.chapterTitleLabel(lang)) },
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
                        value = editTitleBn,
                        onValueChange = { editTitleBn = it },
                        label = { Text(AppStrings.chapterTitleBnLabel(lang)) },
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
                        val ch = chapterToEdit
                        if (ch != null && (editTitleEn.isNotBlank() || editTitleBn.isNotBlank())) {
                            val en = editTitleEn.ifBlank { editTitleBn }
                            val bn = editTitleBn.ifBlank { editTitleEn }
                            viewModel.updateChapter(
                                ch.copy(
                                    titleEn = en,
                                    titleBn = bn,
                                    estimatedMinutes = editMins
                                )
                            )
                        }
                        chapterToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(AppStrings.saveChanges(lang), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { chapterToEdit = null }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }

    // Edit Subject Dialog
    if (showEditSubjectDialog && subject != null) {
        var editSubNameEn by remember { mutableStateOf(subject.nameEn) }
        var editSubNameBn by remember { mutableStateOf(subject.nameBn) }

        AlertDialog(
            onDismissRequest = { showEditSubjectDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "বিষয় সম্পাদনা" else "Edit Subject",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editSubNameEn,
                        onValueChange = { editSubNameEn = it },
                        label = { Text(AppStrings.subjectNameLabel(lang)) },
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
                        value = editSubNameBn,
                        onValueChange = { editSubNameBn = it },
                        label = { Text(AppStrings.subjectNameBnLabel(lang)) },
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
                        if (editSubNameEn.isNotBlank() || editSubNameBn.isNotBlank()) {
                            val en = editSubNameEn.ifBlank { editSubNameBn }
                            val bn = editSubNameBn.ifBlank { editSubNameEn }
                            viewModel.updateSubject(subject.copy(nameEn = en, nameBn = bn))
                        }
                        showEditSubjectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(AppStrings.saveChanges(lang), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditSubjectDialog = false }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }

    // Delete Chapter Dialog
    if (chapterToDelete != null) {
        val ch = chapterToDelete!!
        val chTitle = if (lang == AppLanguage.BANGLA && ch.titleBn.isNotBlank()) ch.titleBn else ch.titleEn

        AlertDialog(
            onDismissRequest = { chapterToDelete = null },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "অধ্যায় মুছবেন?" else "Delete Chapter?",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = AppStrings.deleteChapterPrompt(chTitle, lang),
                    color = textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteChapter(ch.id)
                    chapterToDelete = null
                }) {
                    Text(AppStrings.deleteAction(lang), color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { chapterToDelete = null }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }

    // Delete Subject Dialog
    if (showDeleteSubjectDialog && subject != null) {
        val sName = if (lang == AppLanguage.BANGLA && subject.nameBn.isNotBlank()) subject.nameBn else subject.nameEn

        AlertDialog(
            onDismissRequest = { showDeleteSubjectDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "বিষয় মুছে ফেলবেন?" else "Delete Subject?",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = AppStrings.deleteSubjectPrompt(sName, lang),
                    color = textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSubject(subject.id)
                    showDeleteSubjectDialog = false
                    onBack()
                }) {
                    Text(AppStrings.deleteAction(lang), color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSubjectDialog = false }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }
}

@Composable
fun OverviewRow(
    label: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = textSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
    }
}
