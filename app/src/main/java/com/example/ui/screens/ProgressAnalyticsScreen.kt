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
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkMidnightSurface
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextWhite
import com.example.ui.theme.MintGreen
import com.example.ui.theme.NavyText
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SlateMuted
import com.example.util.AppLanguage

@Composable
fun ProgressAnalyticsScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analyticsRange by viewModel.analyticsRange.collectAsStateWithLifecycle()

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

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

        // Study Time Card with Chart
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
                        text = "18h 45m",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropUp,
                            contentDescription = null,
                            tint = MintGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "+ 12% than last week",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7 Bars Chart matching Screen 8
                    val barHeights = listOf(0.45f, 0.65f, 0.40f, 0.85f, 0.55f, 0.70f, 0.50f)
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Y-Axis labels (4h, 2h, 0h)
                        Column(
                            modifier = Modifier
                                .height(110.dp)
                                .padding(end = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "4h", fontSize = 10.sp, color = textSecondary)
                            Text(text = "2h", fontSize = 10.sp, color = textSecondary)
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
                                    val fillH = chartH * barHeights[i]
                                    val y = chartH - fillH

                                    drawRoundRect(
                                        color = RoyalBlue,
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
                                days.forEach { d ->
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

        // Syllabus Completion Card
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
                            progress = 0.42f,
                            size = 72.dp,
                            strokeWidth = 7.dp,
                            color = RoyalBlue
                        ) {
                            Text(
                                text = "42%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            text = "21 / 50 chapters",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }
}
