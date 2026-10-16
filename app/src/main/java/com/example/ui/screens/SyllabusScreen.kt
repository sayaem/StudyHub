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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterEntity
import com.example.data.model.SubjectEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AddSubjectDialog
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedLight
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkMidnightSurface
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextWhite
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenLight
import com.example.ui.theme.NavyText
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurpleSubject
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueDark
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
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

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
                text = "Syllabus",
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
                    label = "All Subjects",
                    isSelected = selectedFilter == "ALL",
                    isDarkTheme = isDarkTheme,
                    onClick = { selectedFilter = "ALL" }
                )
                SyllabusTopPill(
                    label = "By Subject",
                    isSelected = selectedFilter == "BY_SUBJECT",
                    isDarkTheme = isDarkTheme,
                    onClick = { selectedFilter = "BY_SUBJECT" }
                )
                SyllabusTopPill(
                    label = "By Status",
                    isSelected = selectedFilter == "BY_STATUS",
                    isDarkTheme = isDarkTheme,
                    onClick = { selectedFilter = "BY_STATUS" }
                )
            }
        }

        // Subject Cards
        items(subjects, key = { it.id }) { subject ->
            val subjectChapters = chapters.filter { it.subjectId == subject.id }
            val completedCount = subjectChapters.count { it.status == "COMPLETED" }
            val totalCount = subjectChapters.size.coerceAtLeast(1)
            val percentage = (completedCount.toFloat() / totalCount.toFloat() * 100).toInt()

            val (icon, iconBg, iconTint) = when (subject.nameEn) {
                "Physics" -> Triple(Icons.Default.Science, if (isDarkTheme) PurpleSubject.copy(alpha = 0.2f) else PurpleLight, PurpleSubject)
                "Chemistry" -> Triple(Icons.Default.Science, if (isDarkTheme) MintGreen.copy(alpha = 0.2f) else MintGreenLight, MintGreen)
                "Mathematics" -> Triple(Icons.Default.Calculate, if (isDarkTheme) PurpleSubject.copy(alpha = 0.2f) else PurpleLight, PurpleSubject)
                "English" -> Triple(Icons.Default.MenuBook, if (isDarkTheme) AlertRed.copy(alpha = 0.2f) else AlertRedLight, AlertRed)
                else -> Triple(Icons.Default.Eco, if (isDarkTheme) MintGreen.copy(alpha = 0.2f) else MintGreenLight, MintGreen)
            }

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
                                    .size(40.dp)
                                    .background(iconBg, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = subject.nameEn,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$completedCount / $totalCount chapters",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                        }

                        Text(
                            text = "$percentage%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RoyalBlue,
                        trackColor = if (isDarkTheme) Color(0xFF1E2C46) else Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // Full Width "+ Add Subject" Bottom Button
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { showAddSubjectDialog = true }
                    .testTag("add_subject_button"),
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
                        text = "Add Subject",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                }
            }
        }
    }

    if (showAddSubjectDialog) {
        AddSubjectDialog(
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
