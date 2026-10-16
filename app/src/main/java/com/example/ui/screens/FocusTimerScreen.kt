package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChapterEntity
import com.example.data.model.SubjectEntity
import com.example.ui.MainViewModel
import com.example.ui.TimerState
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkMidnightBg
import com.example.ui.theme.DarkMidnightSurface
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextWhite
import com.example.ui.theme.RoyalBlue
import com.example.util.AppLanguage
import com.example.util.AppStrings

@Composable
fun FocusTimerScreen(
    viewModel: MainViewModel,
    timerState: TimerState,
    subjects: List<SubjectEntity>,
    chapters: List<ChapterEntity>,
    lang: AppLanguage,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.studyStats.collectAsStateWithLifecycle()

    val bg = DarkMidnightBg
    val cardBg = DarkMidnightSurface
    val textPrimary = DarkTextWhite
    val textSecondary = DarkTextMuted

    val selectedSubject = subjects.find { it.id == timerState.selectedSubjectId } ?: subjects.firstOrNull()
    val relevantChapters = chapters.filter { it.subjectId == (selectedSubject?.id ?: 0L) }
    val selectedChapter = relevantChapters.find { it.id == timerState.selectedChapterId } ?: relevantChapters.firstOrNull()

    val subjectTitle = if (lang == AppLanguage.BANGLA && selectedSubject?.nameBn?.isNotBlank() == true)
        selectedSubject.nameBn
    else
        selectedSubject?.nameEn ?: (if (lang == AppLanguage.BANGLA) "সাধারণ অধ্যয়ন" else "General Focus")

    val chapterTitle = if (lang == AppLanguage.BANGLA && selectedChapter?.titleBn?.isNotBlank() == true)
        selectedChapter.titleBn
    else
        selectedChapter?.titleEn ?: (if (lang == AppLanguage.BANGLA) "স্টাডি সেশন" else "Study Session")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .testTag("focus_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar: Back Arrow, "Study Timer", Menu icon
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }

                Text(
                    text = AppStrings.timerFocus(lang),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = textPrimary
                )

                IconButton(onClick = { /* Menu */ }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = textPrimary
                    )
                }
            }
        }

        // Tagged Subject Section (Picker if multiple subjects)
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(RoyalBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subjectTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = chapterTitle,
                    fontSize = 13.sp,
                    color = RoyalBlue.copy(alpha = 0.9f)
                )

                if (!timerState.isRunning) {
                    // Subject Selector Chips
                    if (subjects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(subjects, key = { it.id }) { s ->
                                val isSel = s.id == selectedSubject?.id
                                val sLabel = if (lang == AppLanguage.BANGLA && s.nameBn.isNotBlank()) s.nameBn else s.nameEn
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectTimerSubject(s.id) },
                                    color = if (isSel) RoyalBlue else DarkMidnightSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) RoyalBlue else DarkBorder)
                                ) {
                                    Text(
                                        text = sLabel,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color.White else textSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Chapter Selector Chips
                    if (relevantChapters.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(relevantChapters, key = { it.id }) { ch ->
                                val isSel = ch.id == selectedChapter?.id
                                val cLabel = if (lang == AppLanguage.BANGLA && ch.titleBn.isNotBlank()) ch.titleBn else ch.titleEn
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.selectTimerChapter(ch.id) },
                                    color = if (isSel) RoyalBlue.copy(alpha = 0.25f) else Color.Transparent,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) RoyalBlue else DarkBorder.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = cLabel,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else textSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Preset Duration Selector (25m, 45m, 60m, 90m)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            25 * 60L to AppStrings.timerDurationPreset(25, lang),
                            45 * 60L to AppStrings.timerDurationPreset(45, lang),
                            60 * 60L to AppStrings.timerDurationPreset(60, lang),
                            90 * 60L to AppStrings.timerDurationPreset(90, lang)
                        ).forEach { (secs, label) ->
                            val isSel = timerState.targetSeconds == secs
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setTimerDuration(secs) },
                                color = if (isSel) RoyalBlue else DarkMidnightSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) RoyalBlue else DarkBorder)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Circular Timer Dial
        item {
            Spacer(modifier = Modifier.height(4.dp))

            val progress = (timerState.currentSeconds.toFloat() / timerState.targetSeconds.toFloat()).coerceIn(0f, 1f)
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 500),
                label = "timerDial"
            )

            Box(
                modifier = Modifier
                    .size(230.dp)
                    .testTag("timer_display"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    drawCircle(
                        color = Color(0xFF1E2C46).copy(alpha = 0.6f),
                        radius = (size.minDimension - 12.dp.toPx()) / 2,
                        style = stroke
                    )
                    drawArc(
                        color = RoyalBlue,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = stroke
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val minutes = timerState.currentSeconds / 60
                    val seconds = timerState.currentSeconds % 60
                    val timeStr = "%02d:%02d".format(minutes, seconds)
                    val displayTime = if (lang == AppLanguage.BANGLA) AppStrings.toBengaliDigits(timeStr) else timeStr

                    Text(
                        text = displayTime,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (lang == AppLanguage.BANGLA) "অধ্যয়ন সময়" else "Study Time",
                        fontSize = 13.sp,
                        color = Color(0xFF93C5FD)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Timer Control Buttons Row: Restart | Play/Pause | Stop
        item {
            val isRunning = timerState.isRunning && !timerState.isPaused

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Restart
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.resetTimer() }
                        .padding(8.dp)
                        .testTag("button_restart_timer")
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(DarkMidnightSurface, CircleShape)
                            .border(1.dp, DarkBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = AppStrings.restartTimer(lang),
                            tint = textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = AppStrings.restartTimer(lang),
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                // 2. Play / Pause Big Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(RoyalBlue)
                        .clickable {
                            if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                        }
                        .testTag("button_start_pause_timer"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) AppStrings.pauseTimer(lang) else AppStrings.startTimer(lang),
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                // 3. Stop
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.finishSession() }
                        .padding(8.dp)
                        .testTag("button_stop_timer")
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(DarkMidnightSurface, CircleShape)
                            .border(1.dp, DarkBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = AppStrings.stopTimer(lang),
                            tint = textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = AppStrings.stopTimer(lang),
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }
            }
        }

        // "Session 1 of 4"
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                val sessText = if (lang == AppLanguage.BANGLA)
                    "সেশন ${AppStrings.formatNumber(timerState.currentSession, lang)} / ${AppStrings.formatNumber(timerState.totalSessions, lang)}"
                else
                    "Session ${timerState.currentSession} of ${timerState.totalSessions}"

                Text(
                    text = sessText,
                    fontSize = 12.sp,
                    color = textSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.width(90.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i <= timerState.currentSession) RoyalBlue else Color(0xFF1E2C46))
                        )
                    }
                }
            }
        }

        // Bottom Card: "Today's Progress" (Real live session data)
        item {
            val formattedToday = AppStrings.formatDuration(stats.todayStudySeconds, lang)
            val todayProgress = (stats.todayStudySeconds.toFloat() / 14400f).coerceIn(0f, 1f)

            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = DarkBorder
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = AppStrings.sessionProgress(lang),
                        fontSize = 12.sp,
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (lang == AppLanguage.BANGLA) "$formattedToday / ৪ ঘণ্টা" else "$formattedToday / 4h",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { todayProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RoyalBlue,
                        trackColor = Color(0xFF1E2C46)
                    )
                }
            }
        }
    }
}
