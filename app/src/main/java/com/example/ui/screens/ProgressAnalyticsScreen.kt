package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.CircularProgressMeter
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkMidnightSurface
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextWhite
import com.example.ui.theme.MintGreen
import com.example.ui.theme.NavyText
import com.example.ui.theme.RoyalBlue
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
                    text = "Progress",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = textPrimary
                )
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
}
