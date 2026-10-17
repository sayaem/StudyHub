package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.intelligence.InsightType
import com.example.ui.MainViewModel
import com.example.ui.components.CircularProgressMeter
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProgressAnalyticsScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analyticsRange by viewModel.analyticsRange.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val weeklyReview by viewModel.weeklyReview.collectAsStateWithLifecycle()
    val studyInsights by viewModel.studyInsights.collectAsStateWithLifecycle()
    val weeklyHoursGoal by viewModel.weeklyStudyHoursGoal.collectAsStateWithLifecycle()
    val weeklyChaptersGoal by viewModel.weeklyChaptersGoal.collectAsStateWithLifecycle()
    val admissionAnalytics by viewModel.admissionAnalytics.collectAsStateWithLifecycle()

    var showGoalsDialog by remember { mutableStateOf(false) }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    val now = System.currentTimeMillis()
    val rangeDays = when (analyticsRange) {
        "30_DAYS" -> 30
        "3_MONTHS" -> 90
        else -> 7
    }
    val dayMs = 86_400_000L
    val rangeStartMs = now - (rangeDays * dayMs)
    val prevRangeStartMs = rangeStartMs - (rangeDays * dayMs)

    val currentSessions = sessions.filter { it.timestamp >= rangeStartMs }
    val previousSessions = sessions.filter { it.timestamp in prevRangeStartMs until rangeStartMs }

    val totalRangeSeconds = currentSessions.sumOf { it.durationSeconds }
    val previousRangeSeconds = previousSessions.sumOf { it.durationSeconds }

    val formattedStudyTime = AppStrings.formatDuration(totalRangeSeconds, lang)

    // Dynamic trend comparison
    val (trendText, trendColor, isUp) = when {
        previousRangeSeconds > 0L -> {
            val diff = totalRangeSeconds - previousRangeSeconds
            val pct = ((diff.toFloat() / previousRangeSeconds.toFloat()) * 100f).toInt()
            if (pct >= 0) {
                Triple("+ $pct% than last period", MintGreen, true)
            } else {
                Triple("$pct% than last period", AlertRed, false)
            }
        }
        totalRangeSeconds > 0L -> Triple("Consistent study activity", MintGreen, true)
        else -> Triple("Start studying to track trends", textSecondary, true)
    }

    // Dynamic 7-day chart data based on actual logged sessions
    val midnightToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val dailyLabels = mutableListOf<String>()
    val dailySecondsList = mutableListOf<Long>()

    for (i in 6 downTo 0) {
        val dayStart = midnightToday - (i * dayMs)
        val dayEnd = dayStart + dayMs
        val secs = sessions.filter { it.timestamp in dayStart until dayEnd }.sumOf { it.durationSeconds }
        val label = SimpleDateFormat("EEE", Locale.US).format(Date(dayStart))
        dailyLabels.add(label)
        dailySecondsList.add(secs)
    }

    val maxDailySeconds = maxOf(4 * 3600L, dailySecondsList.maxOrNull() ?: 14400L)
    val barFractions = dailySecondsList.map { s ->
        if (s > 0) (s.toFloat() / maxDailySeconds.toFloat()).coerceIn(0.08f, 1f) else 0.04f
    }

    // Dynamic Syllabus Completion
    val completedChapters = chapters.count { it.status == "COMPLETED" }
    val totalChapters = chapters.size
    val completionProgress = if (totalChapters > 0) (completedChapters.toFloat() / totalChapters.toFloat()).coerceIn(0f, 1f) else 0f
    val completionPercentage = (completionProgress * 100).toInt()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("progress_analytics_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar: Back Arrow + "Progress"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (lang == AppLanguage.BANGLA) "অগ্রগতি ও রিভিউ" else "Progress & Review",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = textPrimary
                    )
                }

                IconButton(onClick = { showGoalsDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Goals",
                        tint = RoyalBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 1. Weekly Goals & Review Card (Section 4)
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
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "সাপ্তাহিক লক্ষ্য ও মূল্যায়ন" else "Weekly Goals & Review",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = textPrimary
                        )
                        Text(
                            text = "${weeklyReview.weekStartDateFormatted} - ${weeklyReview.weekEndDateFormatted}",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Study Time Progress vs Weekly Goal
                    val weeklyActualHours = weeklyReview.actualStudySeconds / 3600f
                    val weeklyHoursPct = (weeklyActualHours / weeklyHoursGoal.toFloat()).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "পড়ার সময় লক্ষ্য" else "Study Time Goal",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                        Text(
                            text = "${String.format("%.1f", weeklyActualHours)}h / ${weeklyHoursGoal}h",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { weeklyHoursPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RoyalBlue,
                        trackColor = if (isDarkTheme) Color(0xFF1E2C46) else Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Completed Chapters this week vs Goal
                    val chapsGoalPct = if (weeklyChaptersGoal > 0) {
                        (weeklyReview.completedChaptersThisWeek.toFloat() / weeklyChaptersGoal.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "সপ্তাহে অধ্যায় সমাপ্তি" else "Chapters Completed",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                        Text(
                            text = "${weeklyReview.completedChaptersThisWeek} / $weeklyChaptersGoal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { chapsGoalPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MintGreen,
                        trackColor = if (isDarkTheme) Color(0xFF1E2C46) else Color(0xFFE2E8F0)
                    )

                    // Subject Breakdown
                    if (weeklyReview.subjectBreakdown.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "বিষয়ভিত্তিক সময় বণ্টন" else "Subject Time Breakdown",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        weeklyReview.subjectBreakdown.take(4).forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) sub.subjectNameBn else sub.subjectNameEn,
                                    fontSize = 11.sp,
                                    color = textPrimary
                                )
                                Text(
                                    text = "${String.format("%.1f", sub.studySeconds / 3600f)}h (${String.format("%.0f", sub.percentOfTotal)}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textSecondary
                                )
                            }
                        }
                    }

                    // Actionable observations
                    val observations = if (lang == AppLanguage.BANGLA) weeklyReview.observationsBn else weeklyReview.observationsEn
                    if (observations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "বাস্তব পর্যবেক্ষণ:" else "Actionable Observations:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        observations.forEach { obs ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(4.dp)
                                        .background(RoyalBlue, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = obs,
                                    fontSize = 11.sp,
                                    color = textSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Study Insights Section (Section 5)
        if (studyInsights.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "স্টাডি ইনসাইট ও অ্যালার্ট" else "Study Insights",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = textPrimary
                    )

                    studyInsights.forEach { insight ->
                        val icon = when (insight.type) {
                            InsightType.WARNING -> Icons.Default.WarningAmber
                            InsightType.SUCCESS -> Icons.Default.CheckCircle
                            InsightType.INFO -> Icons.Default.Info
                        }
                        val tint = when (insight.type) {
                            InsightType.WARNING -> Color(0xFFF59E0B)
                            InsightType.SUCCESS -> MintGreen
                            InsightType.INFO -> RoyalBlue
                        }

                        StudyHubCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = cardBg,
                            borderColor = borderCol
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) insight.titleBn else insight.titleEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) insight.descriptionBn else insight.descriptionEn,
                                        fontSize = 11.sp,
                                        color = textSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.dismissStudyInsight(insight.id) },
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
            }
        }

        // Filter Pills: 7 Days | 30 Days | 3 Months
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SyllabusTopPill(
                    label = "7 Days",
                    isSelected = analyticsRange == "7_DAYS",
                    isDarkTheme = isDarkTheme,
                    onClick = { viewModel.setAnalyticsRange("7_DAYS") }
                )
                SyllabusTopPill(
                    label = "30 Days",
                    isSelected = analyticsRange == "30_DAYS",
                    isDarkTheme = isDarkTheme,
                    onClick = { viewModel.setAnalyticsRange("30_DAYS") }
                )
                SyllabusTopPill(
                    label = "3 Months",
                    isSelected = analyticsRange == "3_MONTHS",
                    isDarkTheme = isDarkTheme,
                    onClick = { viewModel.setAnalyticsRange("3_MONTHS") }
                )
            }
        }

        // Study Time Card with Real Chart
        item {
            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderCol
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Study Time",
                        fontSize = 12.sp,
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formattedStudyTime,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (previousRangeSeconds > 0L) {
                            Icon(
                                imageVector = if (isUp) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = trendColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = trendText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7 Bars Chart rendered from actual database sessions
                    val topHours = (maxDailySeconds / 3600L).coerceAtLeast(1L)
                    val midHours = topHours / 2

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Y-Axis labels
                        Column(
                            modifier = Modifier
                                .height(110.dp)
                                .padding(end = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${topHours}h", fontSize = 10.sp, color = textSecondary)
                            Text(text = "${midHours}h", fontSize = 10.sp, color = textSecondary)
                            Text(text = "0h", fontSize = 10.sp, color = textSecondary)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            ) {
                                val barWidth = 14.dp.toPx()
                                val count = 7
                                val spacing = (size.width - (barWidth * count)) / (count - 1)
                                val chartH = size.height

                                for (i in 0 until count) {
                                    val x = i * (barWidth + spacing)
                                    val fillH = chartH * barFractions[i]
                                    val y = chartH - fillH
                                    val hasStudy = dailySecondsList[i] > 0

                                    val col = if (hasStudy) RoyalBlue else if (isDarkTheme) Color(0xFF1E2C46) else Color(0xFFE2E8F0)

                                    drawRoundRect(
                                        color = col,
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, fillH),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                dailyLabels.forEach { d ->
                                    Text(
                                        text = d,
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Admission Performance & Mock Test Intelligence Card
        item {
            StudyHubCard(
                modifier = Modifier.fillMaxWidth().testTag("admission_analytics_card"),
                backgroundColor = cardBg,
                borderColor = borderCol
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "মক টেস্ট ও ভর্তি মূল্যায়ন" else "Mock Test & Exam Analytics",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = textPrimary
                        )

                        TextButton(
                            onClick = { viewModel.navigateTo(com.example.ui.ScreenDestination.MOCK_TESTS) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "সব টেস্ট দেখুন" else "View Tests",
                                fontSize = 12.sp,
                                color = RoyalBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (admissionAnalytics.totalTestsTaken == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) "এখনও কোনো মক টেস্ট যুক্ত করা হয়নি" else "No mock tests recorded yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) "ভর্তি পরীক্ষার প্রস্তুতি মাপতে মক টেস্ট রেকর্ড করুন।" else "Log your mock test scores to unlock exam readiness analytics.",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.navigateTo(com.example.ui.ScreenDestination.MOCK_TESTS) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(if (lang == AppLanguage.BANGLA) "টেস্ট যোগ করুন" else "Log Mock Test", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        // Metric 3-box summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Avg %
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "গড় শতাংশ" else "Average %",
                                        fontSize = 10.sp,
                                        color = textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${AppStrings.formatNumber(String.format(Locale.US, "%.1f", admissionAnalytics.overallAveragePercentage), lang)}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = textPrimary
                                    )
                                }
                            }

                            // Tests Taken
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "মোট টেস্ট" else "Total Tests",
                                        fontSize = 10.sp,
                                        color = textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = AppStrings.formatNumber(admissionAnalytics.totalTestsTaken, lang),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = textPrimary
                                    )
                                }
                            }

                            // Delta
                            val delta = admissionAnalytics.deltaPercentageFromPrevious
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "স্কোর পরিবর্তন" else "Delta",
                                        fontSize = 10.sp,
                                        color = textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (delta != null) "${if (delta >= 0) "+" else ""}${AppStrings.formatNumber(String.format(Locale.US, "%.1f", delta), lang)}%" else "--",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (delta != null && delta >= 0) MintGreen else textPrimary
                                    )
                                }
                            }
                        }

                        // Target Progress Bar
                        Spacer(modifier = Modifier.height(12.dp))
                        val progressFraction = (admissionAnalytics.overallAveragePercentage / admissionAnalytics.targetScorePercentage).coerceIn(0.0, 1.0).toFloat()
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) "টার্গেট স্কোরের দিকে অগ্রগতি" else "Progress toward Target Score",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                                Text(
                                    text = "${AppStrings.formatNumber(admissionAnalytics.targetScorePercentage.toInt(), lang)}% target",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = RoyalBlue,
                                trackColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                            )
                        }

                        // Weakest subject and topics
                        val weakest = admissionAnalytics.weakestSubject
                        if (weakest != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = if (isDarkTheme) AlertRed.copy(alpha = 0.1f) else AlertRedLight,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (lang == AppLanguage.BANGLA) "সবচেয়ে দুর্বল বিষয়: ${weakest.subjectNameBn}" else "Lowest Scoring: ${weakest.subjectNameEn}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AlertRed
                                        )
                                        Text(
                                            text = if (lang == AppLanguage.BANGLA) "গড় নম্বর ${String.format(Locale.US, "%.1f", weakest.averagePercentage)}%। আরও অনুশীলনের প্রয়োজন।" else "Average score ${String.format(Locale.US, "%.1f", weakest.averagePercentage)}%. Focus extra time here.",
                                            fontSize = 11.sp,
                                            color = textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Mistake Book Summary
                        if (admissionAnalytics.totalMistakesCount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.BANGLA)
                                        "মিস্টেক বুকে ${AppStrings.formatNumber(admissionAnalytics.unresolvedMistakesCount, lang)}টি অমীমাংসিত প্রশ্ন রয়েছে"
                                    else
                                        "${admissionAnalytics.unresolvedMistakesCount} unresolved questions in Mistake Book",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                                TextButton(
                                    onClick = { viewModel.navigateTo(com.example.ui.ScreenDestination.MISTAKE_BOOK) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(if (lang == AppLanguage.BANGLA) "ভুলসমূহ খুলুন" else "Open Mistakes", fontSize = 11.sp, color = RoyalBlue)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Syllabus Completion Card dynamically calculated from Room
        item {
            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderCol
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Syllabus Completion",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressMeter(
                            progress = completionProgress,
                            size = 72.dp,
                            strokeWidth = 7.dp,
                            color = RoyalBlue
                        ) {
                            Text(
                                text = "$completionPercentage%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = if (totalChapters > 0) "$completedChapters / $totalChapters chapters" else "No chapters added",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (totalChapters > 0) "${totalChapters - completedChapters} chapters remaining" else "Add subjects in Syllabus to track progress",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // Weekly Goals Configuration Dialog
    if (showGoalsDialog) {
        var tempGoalHours by remember { mutableIntStateOf(weeklyHoursGoal) }
        var tempGoalChapters by remember { mutableIntStateOf(weeklyChaptersGoal) }

        AlertDialog(
            onDismissRequest = { showGoalsDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "সাপ্তাহিক লক্ষ্য নির্ধারণ" else "Configure Weekly Goals",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = tempGoalHours.toString(),
                        onValueChange = { tempGoalHours = it.toIntOrNull() ?: 20 },
                        label = { Text(if (lang == AppLanguage.BANGLA) "সাপ্তাহিক পড়ার সময় (ঘণ্টা)" else "Weekly Study Time (hours)") },
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
                        value = tempGoalChapters.toString(),
                        onValueChange = { tempGoalChapters = it.toIntOrNull() ?: 5 },
                        label = { Text(if (lang == AppLanguage.BANGLA) "সাপ্তাহিক অধ্যায় সমাপ্তির লক্ষ্য" else "Weekly Chapters Target") },
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
                        viewModel.setWeeklyGoals(tempGoalHours, tempGoalChapters)
                        showGoalsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(if (lang == AppLanguage.BANGLA) "সংরক্ষণ করুন" else "Save Goals", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalsDialog = false }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }
}
