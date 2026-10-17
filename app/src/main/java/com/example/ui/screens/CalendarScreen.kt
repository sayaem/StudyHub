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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdmissionTargetEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AddAdmissionBottomSheet
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
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueLight
import com.example.ui.theme.SlateMuted
import com.example.util.AppLanguage
import com.example.util.AppStrings
import java.util.concurrent.TimeUnit

@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    admissions: List<AdmissionTargetEntity>,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var filterType by remember { mutableStateOf("ALL") } // "ALL", "UNIVERSITY", "COLLEGE"
    var showAddDialog by remember { mutableStateOf(false) }
    var targetToDelete by remember { mutableStateOf<AdmissionTargetEntity?>(null) }
    var targetToEdit by remember { mutableStateOf<AdmissionTargetEntity?>(null) }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    val now = System.currentTimeMillis()

    val filteredAdmissions = admissions.filter { target ->
        when (filterType) {
            "UNIVERSITY" -> !target.universityName.contains("College", ignoreCase = true)
            "COLLEGE" -> target.universityName.contains("College", ignoreCase = true)
            else -> true
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("calendar_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Bar with Back Arrow + "Admission Calendar"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = AppStrings.admissionCalendar(lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = textPrimary
                    )
                }
            }

            item {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "আসন্ন বিশ্ববিদ্যালয় ও কলেজ ভর্তি পরীক্ষার সময়সূচী" else "Track application deadlines and university admission exams",
                    fontSize = 12.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Filter Pills: All | University | College
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SyllabusTopPill(
                        label = AppStrings.allPill(lang),
                        isSelected = filterType == "ALL",
                        isDarkTheme = isDarkTheme,
                        onClick = { filterType = "ALL" }
                    )
                    SyllabusTopPill(
                        label = if (lang == AppLanguage.BANGLA) "বিশ্ববিদ্যালয়" else "University",
                        isSelected = filterType == "UNIVERSITY",
                        isDarkTheme = isDarkTheme,
                        onClick = { filterType = "UNIVERSITY" }
                    )
                    SyllabusTopPill(
                        label = if (lang == AppLanguage.BANGLA) "কলেজ" else "College",
                        isSelected = filterType == "COLLEGE",
                        isDarkTheme = isDarkTheme,
                        onClick = { filterType = "COLLEGE" }
                    )
                }
            }

            if (filteredAdmissions.isEmpty()) {
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
                                    .size(52.dp)
                                    .background(RoyalBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = AppStrings.emptyCalendarTitle(lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = AppStrings.emptyCalendarDesc(lang),
                                fontSize = 13.sp,
                                color = textSecondary,
                                lineHeight = 18.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Admission Targets Cards List matching Screen 7
                items(filteredAdmissions, key = { it.id }) { target ->
                    val daysUntilExam = TimeUnit.MILLISECONDS.toDays(target.examDate - now).toInt()
                    val daysUntilDeadline = TimeUnit.MILLISECONDS.toDays(target.applicationDeadline - now).toInt()

                    val (examBadgeBg, examBadgeFg) = when {
                        daysUntilExam <= 7 -> if (isDarkTheme) AlertRed.copy(alpha = 0.2f) to AlertRed else AlertRedLight to AlertRed
                        daysUntilExam <= 30 -> if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) to RoyalBlue else RoyalBlueLight to RoyalBlue
                        else -> if (isDarkTheme) MintGreen.copy(alpha = 0.2f) to MintGreen else MintGreenLight to MintGreen
                    }

                    val deadlineBadgeText = when {
                        target.status == "APPLIED" || target.status == "ADMIT_DOWNLOADED" || target.status == "COMPLETED" ->
                            if (lang == AppLanguage.BANGLA) "আবেদন সম্পন্ন" else "Application Submitted"
                        daysUntilDeadline < 0 ->
                            if (lang == AppLanguage.BANGLA) "আবেদনের মেয়াদ শেষ" else "Deadline Passed"
                        daysUntilDeadline == 0 ->
                            if (lang == AppLanguage.BANGLA) "আবেদনের শেষ দিন আজ!" else "Deadline Today!"
                        else ->
                            if (lang == AppLanguage.BANGLA) "আবেদনের বাকি ${AppStrings.toBengaliDigits(daysUntilDeadline.toString())} দিন" else "Apply within ${daysUntilDeadline}d"
                    }

                    val (deadlineBadgeBg, deadlineBadgeFg) = when {
                        target.status == "APPLIED" || target.status == "ADMIT_DOWNLOADED" || target.status == "COMPLETED" ->
                            (if (isDarkTheme) MintGreen.copy(alpha = 0.15f) else MintGreenLight) to MintGreen
                        daysUntilDeadline < 0 ->
                            (if (isDarkTheme) AlertRed.copy(alpha = 0.15f) else AlertRedLight) to AlertRed
                        daysUntilDeadline <= 3 ->
                            (if (isDarkTheme) AlertRed.copy(alpha = 0.2f) else AlertRedLight) to AlertRed
                        else ->
                            (if (isDarkTheme) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFFFEF3C7)) to Color(0xFFD97706)
                    }

                    val examDateStr = AppStrings.formatDate(target.examDate, lang)
                    val deadlineDateStr = AppStrings.formatDate(target.applicationDeadline, lang)

                    val statusLabel = when (target.status) {
                        "APPLIED" -> AppStrings.statusApplied(lang)
                        "ADMIT_DOWNLOADED" -> AppStrings.statusAdmitDownloaded(lang)
                        else -> AppStrings.statusNotApplied(lang)
                    }

                    StudyHubCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admission_item_${target.id}"),
                        backgroundColor = cardBg,
                        borderColor = borderCol,
                        onClick = { viewModel.cycleAdmissionStatus(target) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
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
                                            .size(42.dp)
                                            .background(
                                                if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) else RoyalBlueLight,
                                                RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = RoyalBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = target.universityName,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${target.unit} • $statusLabel",
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { targetToEdit = target },
                                        modifier = Modifier.size(32.dp).testTag("edit_admission_${target.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = AppStrings.editAction(lang),
                                            tint = textSecondary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(2.dp))

                                    IconButton(
                                        onClick = { targetToDelete = target },
                                        modifier = Modifier.size(32.dp).testTag("delete_admission_${target.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = AppStrings.deleteAction(lang),
                                            tint = textSecondary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Badges Row: Application Deadline Status & Exam Countdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = deadlineBadgeBg
                                ) {
                                    Text(
                                        text = deadlineBadgeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = deadlineBadgeFg,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = examBadgeBg
                                ) {
                                    Text(
                                        text = if (daysUntilExam >= 0)
                                            AppStrings.daysRemaining(daysUntilExam, lang)
                                        else
                                            (if (lang == AppLanguage.BANGLA) "পরীক্ষা শেষ" else "Exam Over"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = examBadgeFg,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Dates & Notes Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = textSecondary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${if (lang == AppLanguage.BANGLA) "পরীক্ষা:" else "Exam:"} $examDateStr",
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                }

                                if (target.websiteUrl.isNotBlank()) {
                                    Text(
                                        text = target.websiteUrl,
                                        fontSize = 11.sp,
                                        color = RoyalBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (target.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = target.notes,
                                    fontSize = 11.sp,
                                    color = textSecondary.copy(alpha = 0.8f),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 88.dp, end = 20.dp)
                .testTag("fab_add_admission"),
            containerColor = RoyalBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = AppStrings.addTargetUni(lang))
        }
    }

    if (showAddDialog) {
        AddAdmissionBottomSheet(
            isDarkTheme = isDarkTheme,
            lang = lang,
            onDismiss = { showAddDialog = false },
            onConfirm = { uni, unit, dead, exam, notes ->
                viewModel.addAdmissionTarget(uni, unit, dead, exam, notes)
                showAddDialog = false
            }
        )
    }

    if (targetToEdit != null) {
        val t = targetToEdit!!
        val dDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(t.applicationDeadline - now).toInt().coerceAtLeast(0)
        val eDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(t.examDate - now).toInt().coerceAtLeast(0)
        AddAdmissionBottomSheet(
            isDarkTheme = isDarkTheme,
            lang = lang,
            initialUni = t.universityName,
            initialUnit = t.unit,
            initialDeadlineDays = dDays,
            initialExamDays = eDays,
            initialNotes = t.notes,
            isEditing = true,
            onDismiss = { targetToEdit = null },
            onConfirm = { uni, unit, dead, exam, notes ->
                viewModel.updateAdmissionTarget(t.id, uni, unit, dead, exam, notes)
                targetToEdit = null
            }
        )
    }

    if (targetToDelete != null) {
        val target = targetToDelete!!
        AlertDialog(
            onDismissRequest = { targetToDelete = null },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "বিশ্ববিদ্যালয় মুছবেন?" else "Delete Target?",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = if (lang == AppLanguage.BANGLA)
                        "আপনি কি \"${target.universityName}\" ভর্তি লক্ষ্য মুছে ফেলতে চান?"
                    else
                        "Are you sure you want to remove '${target.universityName}' from your calendar?",
                    color = textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAdmissionTarget(target.id)
                    targetToDelete = null
                }) {
                    Text(AppStrings.deleteAction(lang), color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { targetToDelete = null }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }
}
