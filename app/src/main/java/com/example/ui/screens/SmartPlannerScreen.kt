package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.domain.intelligence.RecoveryStatus
import com.example.ui.MainViewModel
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkMidnightSurface
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextWhite
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenLight
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

@Composable
fun SmartPlannerScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val timeRange by viewModel.plannerTimeRange.collectAsStateWithLifecycle()
    val allTasks by viewModel.plannerTasks.collectAsStateWithLifecycle()
    val recoveryReport by viewModel.syllabusRecovery.collectAsStateWithLifecycle()
    val suggestedPlan by viewModel.suggestedDailyPlan.collectAsStateWithLifecycle()
    val availableHours by viewModel.dailyAvailableStudyHours.collectAsStateWithLifecycle()
    val studyPeriod by viewModel.preferredStudyPeriod.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAvailabilityDialog by remember { mutableStateOf(false) }

    val filteredTasks = allTasks.filter { it.dateCategory == timeRange }
    val completedCount = filteredTasks.count { it.isCompleted }
    val totalCount = filteredTasks.size

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    val rawSubtitle = when (timeRange) {
        "TODAY" -> SimpleDateFormat("EEE, dd MMM", Locale.US).format(Date())
        "TOMORROW" -> SimpleDateFormat("EEE, dd MMM", Locale.US).format(Date(System.currentTimeMillis() + 86400000L))
        else -> if (lang == AppLanguage.BANGLA) "আগামী ৭ দিন" else "Upcoming 7 Days"
    }
    val dateSubtitle = if (lang == AppLanguage.BANGLA && timeRange != "THIS_WEEK")
        AppStrings.toBengaliDigits(rawSubtitle) else rawSubtitle

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("smart_planner_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) else RoyalBlueLight,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = AppStrings.plannerTitle(lang),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showAvailabilityDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Preferences",
                            tint = textSecondary
                        )
                    }
                    IconButton(onClick = { showAddTaskDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = AppStrings.addTask(lang),
                            tint = RoyalBlue
                        )
                    }
                }
            }
        }

        // 1. Smart Syllabus Recovery Engine Card
        item {
            val statusColor = when (recoveryReport.recoveryStatus) {
                RecoveryStatus.ON_TRACK -> MintGreen
                RecoveryStatus.MODERATE_BEHIND -> Color(0xFFF59E0B)
                RecoveryStatus.CRITICALLY_BEHIND -> AlertRed
                RecoveryStatus.NO_EXAM_SET -> RoyalBlue
                RecoveryStatus.COMPLETED -> MintGreen
            }

            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = if (recoveryReport.isBehind) statusColor.copy(alpha = 0.5f) else borderCol
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
                                    .size(8.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA) recoveryReport.headlineBn else recoveryReport.headlineEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = textPrimary
                            )
                        }

                        if (recoveryReport.nearestExamDays != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.BANGLA)
                                        "${AppStrings.toBengaliDigits(recoveryReport.nearestExamDays.toString())} দিন বাকি"
                                    else
                                        "${recoveryReport.nearestExamDays}d left",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (lang == AppLanguage.BANGLA) recoveryReport.recommendationBn else recoveryReport.recommendationEn,
                        fontSize = 12.sp,
                        color = textSecondary,
                        lineHeight = 17.sp
                    )

                    // Prioritized recovery chapters if behind
                    if (recoveryReport.isBehind && recoveryReport.prioritizedChapters.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "রিকভারির জন্য শীর্ষ অগ্রাধিকার:" else "Priority Recovery Focus:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        recoveryReport.prioritizedChapters.take(3).forEach { ch ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(RoyalBlue, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${ch.subjectNameEn}: ${ch.titleEn}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${ch.estimatedMinutes}m",
                                    fontSize = 10.sp,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filter Pills: Today | Tomorrow | This Week
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterPill(
                    label = AppStrings.tabToday(lang),
                    isSelected = timeRange == "TODAY",
                    isDarkTheme = isDarkTheme,
                    onClick = { viewModel.setPlannerTimeRange("TODAY") }
                )
                FilterPill(
                    label = AppStrings.tabTomorrow(lang),
                    isSelected = timeRange == "TOMORROW",
                    isDarkTheme = isDarkTheme,
                    onClick = { viewModel.setPlannerTimeRange("TOMORROW") }
                )
                FilterPill(
                    label = AppStrings.tabThisWeek(lang),
                    isSelected = timeRange == "THIS_WEEK",
                    isDarkTheme = isDarkTheme,
                    onClick = { viewModel.setPlannerTimeRange("THIS_WEEK") }
                )
            }
        }

        // Date Subtitle & Generate Plan Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateSubtitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = textPrimary
                    )
                    Text(
                        text = if (lang == AppLanguage.BANGLA)
                            "দৈনিক প্রাপ্যতা: ${AppStrings.toBengaliDigits(availableHours.toString())} ঘণ্টা ($studyPeriod)"
                        else
                            "Availability: ${availableHours}h daily ($studyPeriod)",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.generateSuggestedPlan(timeRange) },
                    color = if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) else RoyalBlueLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "প্ল্যান তৈরি করুন" else "Generate Plan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                    }
                }
            }
        }

        // 2. Suggested Plan Preview Card (Distinct preview state)
        if (suggestedPlan.isNotEmpty()) {
            item {
                StudyHubCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                    borderColor = RoyalBlue
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) "প্রস্তাবিত স্টাডি প্ল্যান (রিভিউ)" else "Suggested Plan Preview",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textPrimary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearSuggestedPlan() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        suggestedPlan.forEach { sugg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${sugg.timeSlot} • ${sugg.subjectTitle}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlue
                                    )
                                    Text(
                                        text = sugg.topicTitle,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) sugg.reasonBn else sugg.reasonEn,
                                        fontSize = 10.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.acceptSuggestedPlan(timeRange) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) "প্ল্যান গ্রহণ করুন" else "Accept Plan",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.generateSuggestedPlan(timeRange) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = textPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) "পুনরায় তৈরি" else "Regenerate",
                                    fontSize = 12.sp,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scheduled Study Tasks
        if (filteredTasks.isEmpty() && suggestedPlan.isEmpty()) {
            item {
                StudyHubCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = cardBg,
                    borderColor = borderCol
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(RoyalBlueLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = AppStrings.emptyPlannerTitle(lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppStrings.emptyPlannerDesc(lang),
                            fontSize = 12.sp,
                            color = textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { block ->
                val accentBorder = if (block.isCompleted) MintGreen else RoyalBlue

                StudyHubCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = cardBg,
                    borderColor = borderCol,
                    onClick = { viewModel.togglePlannerTask(block.id, block.isCompleted) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 3.dp, height = 36.dp)
                                    .background(accentBorder, RoundedCornerShape(2.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) AppStrings.toBengaliDigits(block.timeSlot) else block.timeSlot,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = block.subjectTitle,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    if (block.topicTitle.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(${block.topicTitle})",
                                            fontSize = 13.sp,
                                            color = textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (block.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .background(MintGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .border(1.5.dp, if (isDarkTheme) Color(0xFF475569) else Color(0xFFCBD5E1), CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            IconButton(
                                onClick = { viewModel.deletePlannerTask(block.id) },
                                modifier = Modifier.size(28.dp)
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
                }
            }
        }

        // Motivational / Consistency Card
        item {
            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderCol
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (isDarkTheme) MintGreen.copy(alpha = 0.2f) else MintGreenLight,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = MintGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "ধারাবাহিক থাকুন!" else "Keep going!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = AppStrings.motivationalPrompt(completedCount, totalCount, lang),
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }

    // Availability Dialog (Configuring Study Time and Preferred Period)
    if (showAvailabilityDialog) {
        var tempHours by remember { mutableIntStateOf(availableHours) }
        var tempPeriod by remember { mutableStateOf(studyPeriod) }

        AlertDialog(
            onDismissRequest = { showAvailabilityDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "পড়ার সময় ও প্রাপ্যতা কনফিগার করুন" else "Configure Study Availability",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "দৈনিক পড়ার লক্ষ্যমাত্রা (ঘণ্টা):" else "Daily Available Study Hours:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 4, 6, 8).forEach { h ->
                            val isSel = tempHours == h
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { tempHours = h },
                                color = if (isSel) RoyalBlue else if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${h}h",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSel) Color.White else textPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (lang == AppLanguage.BANGLA) "পছন্দের পড়ার সময়:" else "Preferred Study Period:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL" to "All Day", "MORNING" to "Morning", "EVENING" to "Evening").forEach { (code, label) ->
                            val isSel = tempPeriod == code
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { tempPeriod = code },
                                color = if (isSel) RoyalBlue else if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSel) Color.White else textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setDailyAvailableHours(tempHours)
                        viewModel.setPreferredStudyPeriod(tempPeriod)
                        showAvailabilityDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(if (lang == AppLanguage.BANGLA) "সংরক্ষণ করুন" else "Save Settings", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvailabilityDialog = false }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }

    // Add Custom Task Dialog
    if (showAddTaskDialog) {
        var timeSlot by remember { mutableStateOf("09:00 - 10:30") }
        var subject by remember { mutableStateOf("") }
        var topic by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "স্টাডি ব্লক যোগ করুন" else "Add Study Block",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = timeSlot,
                        onValueChange = { timeSlot = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "সময় (যেমনঃ 09:00 - 10:30)" else "Time Slot (e.g. 09:00 - 10:30)") },
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
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "বিষয় (যেমনঃ পদার্থবিজ্ঞান)" else "Subject (e.g. Physics)") },
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
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "টপিক (যেমনঃ আলোর প্রতিফলন)" else "Topic (e.g. Wave Optics)") },
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
                        if (subject.isNotBlank()) {
                            viewModel.addPlannerTask(timeSlot, subject, topic, timeRange)
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(AppStrings.addTask(lang), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }
}
