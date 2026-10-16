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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.components.AddSubjectBottomSheet
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
import com.example.util.AppStrings

@Composable
fun SyllabusScreen(
    viewModel: MainViewModel,
    subjects: List<SubjectEntity>,
    chapters: List<ChapterEntity>,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onOpenSubject: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "BY_SUBJECT", "BY_STATUS"
    var statusSubFilter by remember { mutableStateOf("ALL") } // "ALL", "IN_PROGRESS", "COMPLETED", "NOT_STARTED"
    var subjectSubFilter by remember { mutableStateOf<Long?>(null) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    val displayedSubjects = subjects.filter { sub ->
        val matchesSubject = if (selectedFilter == "BY_SUBJECT" && subjectSubFilter != null) {
            sub.id == subjectSubFilter
        } else true

        val matchesStatus = if (selectedFilter == "BY_STATUS" && statusSubFilter != "ALL") {
            val subChapters = chapters.filter { it.subjectId == sub.id }
            subChapters.any { it.status == statusSubFilter }
        } else true

        matchesSubject && matchesStatus
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("syllabus_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header: "Syllabus"
        item {
            Text(
                text = AppStrings.syllabusTitle(lang),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = textPrimary
            )
        }

        // Filter Pills: All Subjects, By Subject, By Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SyllabusTopPill(
                    label = AppStrings.allSubjects(lang),
                    isSelected = selectedFilter == "ALL",
                    isDarkTheme = isDarkTheme,
                    onClick = {
                        selectedFilter = "ALL"
                        subjectSubFilter = null
                        statusSubFilter = "ALL"
                    }
                )
                SyllabusTopPill(
                    label = AppStrings.bySubject(lang),
                    isSelected = selectedFilter == "BY_SUBJECT",
                    isDarkTheme = isDarkTheme,
                    onClick = { selectedFilter = "BY_SUBJECT" }
                )
                SyllabusTopPill(
                    label = AppStrings.byStatus(lang),
                    isSelected = selectedFilter == "BY_STATUS",
                    isDarkTheme = isDarkTheme,
                    onClick = { selectedFilter = "BY_STATUS" }
                )
            }
        }

        if (selectedFilter == "BY_SUBJECT" && subjects.isNotEmpty()) {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SyllabusTopPill(
                            label = AppStrings.allPill(lang),
                            isSelected = subjectSubFilter == null,
                            isDarkTheme = isDarkTheme,
                            onClick = { subjectSubFilter = null }
                        )
                    }
                    items(subjects, key = { it.id }) { s ->
                        val subName = if (lang == AppLanguage.BANGLA && s.nameBn.isNotBlank()) s.nameBn else s.nameEn
                        SyllabusTopPill(
                            label = subName,
                            isSelected = subjectSubFilter == s.id,
                            isDarkTheme = isDarkTheme,
                            onClick = { subjectSubFilter = s.id }
                        )
                    }
                }
            }
        }

        if (selectedFilter == "BY_STATUS") {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SyllabusTopPill(
                            label = AppStrings.allStatusesPill(lang),
                            isSelected = statusSubFilter == "ALL",
                            isDarkTheme = isDarkTheme,
                            onClick = { statusSubFilter = "ALL" }
                        )
                    }
                    item {
                        SyllabusTopPill(
                            label = AppStrings.statusInProgress(lang),
                            isSelected = statusSubFilter == "IN_PROGRESS",
                            isDarkTheme = isDarkTheme,
                            onClick = { statusSubFilter = "IN_PROGRESS" }
                        )
                    }
                    item {
                        SyllabusTopPill(
                            label = AppStrings.statusCompleted(lang),
                            isSelected = statusSubFilter == "COMPLETED",
                            isDarkTheme = isDarkTheme,
                            onClick = { statusSubFilter = "COMPLETED" }
                        )
                    }
                    item {
                        SyllabusTopPill(
                            label = AppStrings.statusNotStarted(lang),
                            isSelected = statusSubFilter == "NOT_STARTED",
                            isDarkTheme = isDarkTheme,
                            onClick = { statusSubFilter = "NOT_STARTED" }
                        )
                    }
                }
            }
        }

        // Empty state when no subjects exist
        if (displayedSubjects.isEmpty()) {
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
                                .size(56.dp)
                                .background(RoyalBlueLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = AppStrings.emptySubjectsTitle(lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = AppStrings.emptySubjectsDesc(lang),
                            fontSize = 13.sp,
                            color = textSecondary,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        } else {
            // Subject Cards
            items(displayedSubjects, key = { it.id }) { subject ->
                val subjectChapters = chapters.filter { it.subjectId == subject.id }
                val completedCount = subjectChapters.count { it.status == "COMPLETED" }
                val totalCount = subjectChapters.size
                val percentage = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat() * 100).toInt() else 0

                val subjectColor = try {
                    Color(android.graphics.Color.parseColor(subject.colorHex))
                } catch (e: Exception) {
                    RoyalBlue
                }

                val icon = when {
                    subject.nameEn.contains("Physics", ignoreCase = true) -> Icons.Default.Science
                    subject.nameEn.contains("Chemistry", ignoreCase = true) -> Icons.Default.Science
                    subject.nameEn.contains("Math", ignoreCase = true) -> Icons.Default.Calculate
                    subject.nameEn.contains("ICT", ignoreCase = true) || subject.nameEn.contains("Computer", ignoreCase = true) -> Icons.Default.Computer
                    subject.nameEn.contains("Biology", ignoreCase = true) -> Icons.Default.Eco
                    else -> Icons.Default.MenuBook
                }

                val displayName = if (lang == AppLanguage.BANGLA && subject.nameBn.isNotBlank())
                    subject.nameBn else subject.nameEn

                StudyHubCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = cardBg,
                    borderColor = borderCol,
                    onClick = { onOpenSubject(subject.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(subjectColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = subjectColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
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

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${AppStrings.formatNumber(percentage, lang)}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (percentage == 100) RoyalBlue else textSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val barProgress = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = { barProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = subjectColor,
                            trackColor = if (isDarkTheme) Color(0xFF1E2C46) else Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        // Full Width "+ Add Subject" Bottom Button
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showAddSubjectDialog = true }
                    .testTag("add_subject_button"),
                color = if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) else RoyalBlueLight,
                shape = RoundedCornerShape(14.dp)
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.addSubjectAction(lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                }
            }
        }
    }

    if (showAddSubjectDialog) {
        AddSubjectBottomSheet(
            existingSubjects = subjects,
            isDarkTheme = isDarkTheme,
            lang = lang,
            onDismiss = { showAddSubjectDialog = false },
            onConfirm = { nameEn, nameBn, colorHex ->
                viewModel.addSubject(nameEn, nameBn, colorHex)
                showAddSubjectDialog = false
            }
        )
    }
}

@Composable
fun SyllabusTopPill(
    label: String,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) RoyalBlue else if (isDarkTheme) DarkMidnightSurface else Color.White
    val fg = if (isSelected) Color.White else if (isDarkTheme) DarkTextMuted else SlateMuted
    val border = if (isSelected) RoyalBlue else if (isDarkTheme) DarkBorder else SlateBorder

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = bg,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}
