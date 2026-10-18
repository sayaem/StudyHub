package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.PlannerTaskEntity
import com.example.domain.intelligence.RecoveryStatus
import com.example.ui.CalendarHubTab
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
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateMuted
import com.example.util.AppLanguage
import com.example.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    val currentHubTab by viewModel.calendarHubTab.collectAsStateWithLifecycle()
    val timeRange by viewModel.plannerTimeRange.collectAsStateWithLifecycle()
    val allTasks by viewModel.plannerTasks.collectAsStateWithLifecycle()
    val recoveryReport by viewModel.syllabusRecovery.collectAsStateWithLifecycle()
    val suggestedPlan by viewModel.suggestedDailyPlan.collectAsStateWithLifecycle()
    val autoPlanMessage by viewModel.autoPlanMessage.collectAsStateWithLifecycle()
    val availableHours by viewModel.dailyAvailableStudyHours.collectAsStateWithLifecycle()
    val studyPeriod by viewModel.preferredStudyPeriod.collectAsStateWithLifecycle()

    var filterType by remember { mutableStateOf("ALL") } // "ALL", "UNIVERSITY", "COLLEGE"
    var showAddAdmissionDialog by remember { mutableStateOf(false) }
    var targetToDelete by remember { mutableStateOf<AdmissionTargetEntity?>(null) }
    var targetToEdit by remember { mutableStateOf<AdmissionTargetEntity?>(null) }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAvailabilityDialog by remember { mutableStateOf(false) }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    val now = System.currentTimeMillis()

    val filteredTasks = allTasks.filter { it.dateCategory == timeRange }
    val completedCount = filteredTasks.count { it.isCompleted }
    val totalCount = filteredTasks.size

    val filteredAdmissions = admissions.filter { target ->
        when (filterType) {
            "UNIVERSITY" -> !target.universityName.contains("College", ignoreCase = true)
            "COLLEGE" -> target.universityName.contains("College", ignoreCase = true)
            else -> true
        }
    }

    val rawSubtitle = when (timeRange) {
        "TODAY" -> SimpleDateFormat("EEE, dd MMM", Locale.US).format(Date())
        "TOMORROW" -> SimpleDateFormat("EEE, dd MMM", Locale.US).format(Date(now + 86400000L))
        else -> if (lang == AppLanguage.BANGLA) "আসন্ন ৭ দিন" else "Upcoming 7 Days"
    }
    val dateSubtitle = if (lang == AppLanguage.BANGLA && timeRange != "THIS_WEEK")
        AppStrings.toBengaliDigits(rawSubtitle) else rawSubtitle

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
            item {
                Column {
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
                            text = AppStrings.planAndCalendar(lang),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = AppStrings.unifiedCalendarSubtitle(lang),
                        fontSize = 12.sp,
                        color = textSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // Segmented Hub Tabs: Study Planner vs Admission Calendar
            item {
                CalendarHubSegmentedControl(
                    selectedTab = currentHubTab,
                    onTabSelected = { viewModel.setCalendarHubTab(it) },
                    isDarkTheme = isDarkTheme,
                    lang = lang
                )
            }

            // -------------------------------------------------------------
            // SECTION 1: STUDY PLANNER
            // -------------------------------------------------------------
            if (currentHubTab == CalendarHubTab.STUDY_PLANNER) {
                // Time Range Pills (Today, Tomorrow, Upcoming)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SyllabusTopPill(
                                label = AppStrings.tabToday(lang),
                                isSelected = timeRange == "TODAY",
                                isDarkTheme = isDarkTheme,
                                onClick = { viewModel.setPlannerTimeRange("TODAY") }
                            )
                            SyllabusTopPill(
                                label = AppStrings.tabTomorrow(lang),
                                isSelected = timeRange == "TOMORROW",
                                isDarkTheme = isDarkTheme,
                                onClick = { viewModel.setPlannerTimeRange("TOMORROW") }
                            )
                            SyllabusTopPill(
                                label = AppStrings.tabThisWeek(lang),
                                isSelected = timeRange == "THIS_WEEK",
                                isDarkTheme = isDarkTheme,
                                onClick = { viewModel.setPlannerTimeRange("THIS_WEEK") }
                            )
                        }

                        Text(
                            text = dateSubtitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSecondary
                        )
                    }
                }

                // Syllabus Recovery Engine Status Card
                item {
                    val statusColor = when (recoveryReport.recoveryStatus) {
                        RecoveryStatus.ON_TRACK, RecoveryStatus.COMPLETED -> MintGreen
                        RecoveryStatus.MODERATE_BEHIND -> Color(0xFFF59E0B)
                        RecoveryStatus.CRITICALLY_BEHIND -> AlertRed
                        RecoveryStatus.NO_EXAM_SET -> RoyalBlue
                    }
                    val statusBg = if (isDarkTheme) statusColor.copy(alpha = 0.15f) else when (recoveryReport.recoveryStatus) {
                        RecoveryStatus.ON_TRACK, RecoveryStatus.COMPLETED -> MintGreenLight
                        RecoveryStatus.MODERATE_BEHIND -> Color(0xFFFEF3C7)
                        RecoveryStatus.CRITICALLY_BEHIND -> AlertRedLight
                        RecoveryStatus.NO_EXAM_SET -> RoyalBlueLight
                    }

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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(statusBg, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (recoveryReport.recoveryStatus) {
                                                RecoveryStatus.ON_TRACK, RecoveryStatus.COMPLETED -> Icons.Default.Check
                                                RecoveryStatus.MODERATE_BEHIND, RecoveryStatus.CRITICALLY_BEHIND -> Icons.Default.WarningAmber
                                                RecoveryStatus.NO_EXAM_SET -> Icons.Default.Info
                                            },
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) recoveryReport.headlineBn else recoveryReport.headlineEn,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = textPrimary
                                    )
                                }

                                if (recoveryReport.nearestExamDays != null) {
                                    Text(
                                        text = AppStrings.daysRemaining(recoveryReport.nearestExamDays!!, lang),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (lang == AppLanguage.BANGLA) recoveryReport.recommendationBn else recoveryReport.recommendationEn,
                                fontSize = 11.5.sp,
                                color = textSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Action Bar: Smart Auto-Plan, Add Task, Preferences
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Smart Auto-Plan Button
                        Button(
                            onClick = { viewModel.generateSuggestedPlan(timeRange) },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("action_auto_plan")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.autoPlanAction(lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Right actions: Preferences & Add Task
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showAvailabilityDialog = true },
                                modifier = Modifier.testTag("action_preferences")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Preferences",
                                    tint = textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { showAddTaskDialog = true },
                                modifier = Modifier.testTag("action_add_task")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(RoyalBlueLight, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = AppStrings.addTask(lang),
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Auto-Plan Feedback Banner (if message is active)
                if (autoPlanMessage != null) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = autoPlanMessage!!,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.clearAutoPlanMessage() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Suggested Plan Preview Card
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
                                            text = AppStrings.autoPlanProposalTitle(lang),
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
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_apply_schedule"),
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = AppStrings.applySchedule(lang),
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

                // Tasks List Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "নির্ধারিত পড়াশোনা" else "Scheduled Tasks",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = textPrimary
                        )

                        if (totalCount > 0) {
                            Text(
                                text = "${AppStrings.formatNumber(completedCount, lang)}/${AppStrings.formatNumber(totalCount, lang)} " +
                                        (if (lang == AppLanguage.BANGLA) "সম্পন্ন" else "done"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = textSecondary
                            )
                        }
                    }
                }

                // Empty State for Tasks
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
                                    text = if (lang == AppLanguage.BANGLA) "কোনো পড়ার রুটিন যোগ করা হয়নি" else "No Tasks Scheduled Yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppStrings.emptyPlannerDesc(lang),
                                    fontSize = 12.sp,
                                    color = textSecondary,
                                    lineHeight = 17.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.generateSuggestedPlan(timeRange) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = AppStrings.autoPlanAction(lang),
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { showAddTaskDialog = true },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = AppStrings.addTask(lang),
                                            fontSize = 12.sp,
                                            color = textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Task Items List
                    items(filteredTasks, key = { it.id }) { task ->
                        PlannerTaskCard(
                            task = task,
                            isDarkTheme = isDarkTheme,
                            cardBg = cardBg,
                            borderCol = borderCol,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onToggle = { viewModel.togglePlannerTask(task.id, task.isCompleted) },
                            onDelete = { viewModel.deletePlannerTask(task.id) }
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // SECTION 2: ADMISSION CALENDAR
            // -------------------------------------------------------------
            if (currentHubTab == CalendarHubTab.ADMISSION_CALENDAR) {
                // Filter Pills: All | University | College
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                        IconButton(
                            onClick = { showAddAdmissionDialog = true },
                            modifier = Modifier.testTag("action_add_admission")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(RoyalBlueLight, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = AppStrings.addTargetUni(lang),
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
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

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { showAddAdmissionDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(AppStrings.addTargetUni(lang), color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    // Admission Targets Cards List
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
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("edit_admission_${target.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = AppStrings.editTargetUni(lang),
                                                tint = textSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { targetToDelete = target },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("delete_admission_${target.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = AlertRed.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = AppStrings.examDate(lang),
                                            fontSize = 11.sp,
                                            color = textSecondary
                                        )
                                        Text(
                                            text = examDateStr,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary
                                        )
                                    }

                                    // Countdown Tag
                                    Box(
                                        modifier = Modifier
                                            .background(examBadgeBg, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = AppStrings.daysRemaining(daysUntilExam, lang),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = examBadgeFg
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = AppStrings.appDeadline(lang),
                                            fontSize = 11.sp,
                                            color = textSecondary
                                        )
                                        Text(
                                            text = deadlineDateStr,
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                    }

                                    // Deadline Tag
                                    Box(
                                        modifier = Modifier
                                            .background(deadlineBadgeBg, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = deadlineBadgeText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = deadlineBadgeFg
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Admission Target Bottom Sheet
    if (showAddAdmissionDialog) {
        AddAdmissionBottomSheet(
            isDarkTheme = isDarkTheme,
            lang = lang,
            onDismiss = { showAddAdmissionDialog = false },
            onConfirm = { uni, unit, dead, exam, notes ->
                viewModel.addAdmissionTarget(uni, unit, dead, exam, notes)
                showAddAdmissionDialog = false
            }
        )
    }

    // Edit Admission Target Bottom Sheet
    if (targetToEdit != null) {
        val t = targetToEdit!!
        val dDays = TimeUnit.MILLISECONDS.toDays(t.applicationDeadline - now).toInt().coerceAtLeast(0)
        val eDays = TimeUnit.MILLISECONDS.toDays(t.examDate - now).toInt().coerceAtLeast(0)
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

    // Delete Target Dialog
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

    // Availability / Preferences Dialog
    if (showAvailabilityDialog) {
        var tempHours by remember { mutableIntStateOf(availableHours) }
        var tempPeriod by remember { mutableStateOf(studyPeriod) }

        AlertDialog(
            onDismissRequest = { showAvailabilityDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "দৈনিক পড়ার সময় ও অগ্রাধিকার" else "Study Time Preferences",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "দৈনিক পড়ার সময় (ঘণ্টা):" else "Daily Available Hours:",
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

@Composable
fun CalendarHubSegmentedControl(
    selectedTab: CalendarHubTab,
    onTabSelected: (CalendarHubTab) -> Unit,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val containerBg = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val activeBg = RoyalBlue
    val activeFg = Color.White
    val inactiveFg = if (isDarkTheme) DarkTextMuted else SlateMuted

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerBg)
            .padding(4.dp)
    ) {
        // Study Planner Tab
        val isPlannerSelected = selectedTab == CalendarHubTab.STUDY_PLANNER
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(if (isPlannerSelected) activeBg else Color.Transparent)
                .clickable { onTabSelected(CalendarHubTab.STUDY_PLANNER) }
                .padding(vertical = 9.dp)
                .testTag("tab_study_planner"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (isPlannerSelected) activeFg else inactiveFg,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.studyPlannerTab(lang),
                    fontSize = 12.5.sp,
                    fontWeight = if (isPlannerSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isPlannerSelected) activeFg else inactiveFg
                )
            }
        }

        // Admission Calendar Tab
        val isAdmissionSelected = selectedTab == CalendarHubTab.ADMISSION_CALENDAR
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(if (isAdmissionSelected) activeBg else Color.Transparent)
                .clickable { onTabSelected(CalendarHubTab.ADMISSION_CALENDAR) }
                .padding(vertical = 9.dp)
                .testTag("tab_admission_calendar"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = if (isAdmissionSelected) activeFg else inactiveFg,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.admissionCalendarTab(lang),
                    fontSize = 12.5.sp,
                    fontWeight = if (isAdmissionSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isAdmissionSelected) activeFg else inactiveFg
                )
            }
        }
    }
}

@Composable
fun PlannerTaskCard(
    task: PlannerTaskEntity,
    isDarkTheme: Boolean,
    cardBg: Color,
    borderCol: Color,
    textPrimary: Color,
    textSecondary: Color,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val tagColor = try {
        Color(android.graphics.Color.parseColor(task.colorHex))
    } catch (e: Exception) {
        RoyalBlue
    }

    StudyHubCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("planner_task_${task.id}"),
        backgroundColor = cardBg,
        borderColor = if (task.isCompleted) (if (isDarkTheme) DarkBorder else SlateBorder) else borderCol
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = RoyalBlue,
                    uncheckedColor = textSecondary,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.testTag("task_check_${task.id}")
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(tagColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.subjectTitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagColor
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = task.timeSlot,
                        fontSize = 10.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = task.topicTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) textSecondary else textPrimary
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("task_delete_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
