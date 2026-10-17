package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChapterEntity
import com.example.data.model.FocusScheduleEntity
import com.example.data.model.SubjectEntity
import com.example.ui.MainViewModel
import com.example.ui.TimerState
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
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
    val focusSchedules by viewModel.focusSchedules.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var showSessionCompleteDialog by remember { mutableStateOf(false) }
    var productivityRating by remember { mutableIntStateOf(5) }
    var sessionNotes by remember { mutableStateOf("") }

    val hasUsagePermission = remember { viewModel.hasUsageAccessPermission() }

    val bg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

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

    var showMenu by remember { mutableStateOf(false) }

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

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = textPrimary
                        )
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("History") },
                            onClick = { /* Navigate to History */ showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = { /* Navigate to Settings */ showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Custom Duration") },
                            onClick = { 
                                showMenu = false
                                // Logic to show custom duration dialog
                            }
                        )
                    }
                }
            }
        }

        // Tagged Subject Section (Picker if multiple subjects)
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = RoyalBlue.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$subjectTitle • $chapterTitle",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF93C5FD)
                        )
                    }
                }
            }
        }

        // Preset Duration Selector Pills (25m, 45m, 60m, 90m)
        item {
            if (!timerState.isRunning) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    val presets = listOf(
                        25 * 60L to AppStrings.timerDurationPreset(25, lang),
                        45 * 60L to AppStrings.timerDurationPreset(45, lang),
                        60 * 60L to AppStrings.timerDurationPreset(60, lang),
                        90 * 60L to AppStrings.timerDurationPreset(90, lang)
                    )
                    items(presets) { (secs, label) ->
                        val isSel = timerState.targetSeconds == secs
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) RoyalBlue else DarkMidnightSurface,
                            border = BorderStroke(1.dp, if (isSel) RoyalBlue else DarkBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.setTimerDuration(secs) }
                                .testTag("timer_preset_${secs / 60}")
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else textSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Circular Countdown Timer Display
        item {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress = (timerState.currentSeconds.toFloat() / timerState.targetSeconds.toFloat()).coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 800),
                    label = "timerDial"
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("timer_display")
                ) {
                    val strokeWidth = 12.dp.toPx()
                    drawCircle(
                        color = DarkBorder,
                        radius = size.minDimension / 2 - strokeWidth / 2,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = RoyalBlue,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
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

                // 3. Stop / Finish
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.finishSession()
                            showSessionCompleteDialog = true
                        }
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

        // Hardcore Focus Mode & Distraction Control Section
        item {
            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = DarkBorder
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MintGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "ডিসট্রাকশন কন্ট্রোল ও অ্যাপ ব্লকিং" else "Distraction Control & App Blocking",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        TextButton(onClick = { showAddScheduleDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (lang == AppLanguage.BANGLA) "নিয়ম যোগ করুন" else "Add Rule", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Usage Access Permission Banner
                    val permissionGranted = remember { viewModel.hasUsageAccessPermission() }
                    Surface(
                        color = if (permissionGranted) MintGreen.copy(alpha = 0.12f) else AlertRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (permissionGranted) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = if (permissionGranted) MintGreen else AlertRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (permissionGranted) {
                                        if (lang == AppLanguage.BANGLA) "ইউসেজ এক্সেস অনুমতি সক্রিয়" else "Usage Access Permission: Granted"
                                    } else {
                                        if (lang == AppLanguage.BANGLA) "অ্যাপ ব্লকিংয়ের জন্য ইউসেজ এক্সেস প্রয়োজন" else "Usage Access Required for App Blocking"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (permissionGranted) MintGreen else AlertRed
                                )
                                Text(
                                    text = if (permissionGranted) {
                                        if (lang == AppLanguage.BANGLA) "ফোকাস চলাকালীন নির্ধারিত অ্যাপসমূহ সফলভাবে ফিল্টার করা হচ্ছে।" else "Restricted apps are actively monitored during scheduled focus periods."
                                    } else {
                                        if (lang == AppLanguage.BANGLA) "ট্যাপ করে সিস্টেম সেটিংসে ইউসেজ এক্সেস পারমিশন দিন।" else "Tap to open system settings and grant Usage Access permission."
                                    },
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }
                            if (!permissionGranted) {
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // fallback
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (lang == AppLanguage.BANGLA) "অনুমতি দিন" else "Grant", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    if (focusSchedules.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "সক্রিয় ব্লকিং শিডিউল:" else "Active Blocking Schedules:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        focusSchedules.forEach { sched ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val startHour = sched.startTime / 60
                                val startMin = sched.startTime % 60
                                val endHour = sched.endTime / 60
                                val endMin = sched.endTime % 60
                                Text(
                                    text = "🕒 %02d:%02d - %02d:%02d (%d apps)".format(startHour, startMin, endHour, endMin, sched.restrictedApps.size),
                                    fontSize = 12.sp,
                                    color = textPrimary
                                )
                                Switch(
                                    checked = sched.isEnabled,
                                    onCheckedChange = { enabled ->
                                        viewModel.updateFocusSchedule(sched.copy(isEnabled = enabled))
                                    }
                                )
                            }
                        }
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

    // Add Focus Schedule Dialog
    if (showAddScheduleDialog) {
        var startHour by remember { mutableStateOf("09") }
        var startMin by remember { mutableStateOf("00") }
        var endHour by remember { mutableStateOf("12") }
        var endMin by remember { mutableStateOf("00") }
        var appListStr by remember { mutableStateOf("com.facebook.katana, com.instagram.android, com.zhiliaoapp.musically") }

        AlertDialog(
            onDismissRequest = { showAddScheduleDialog = false },
            containerColor = cardBg,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "নতুন ফোকাস শিডিউল যোগ করুন" else "Add Focus Schedule",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "সময়সীমা (ঘণ্টা:মিনিট)" else "Time Range (24h format)",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startHour,
                            onValueChange = { startHour = it },
                            label = { Text("Start HH") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = startMin,
                            onValueChange = { startMin = it },
                            label = { Text("Start MM") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = endHour,
                            onValueChange = { endHour = it },
                            label = { Text("End HH") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endMin,
                            onValueChange = { endMin = it },
                            label = { Text("End MM") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = appListStr,
                        onValueChange = { appListStr = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "ব্লক করার অ্যাপ প্যাকেজ (কমা দিয়ে আলাদা করুন)" else "Restricted App Package Names (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sh = startHour.toIntOrNull() ?: 9
                        val sm = startMin.toIntOrNull() ?: 0
                        val eh = endHour.toIntOrNull() ?: 12
                        val em = endMin.toIntOrNull() ?: 0
                        val startMinutes = sh * 60 + sm
                        val endMinutes = eh * 60 + em
                        val apps = appListStr.split(",").map { it.trim() }.filter { it.isNotBlank() }

                        viewModel.addFocusSchedule(
                            days = listOf(1, 2, 3, 4, 5, 6, 7),
                            startMin = startMinutes,
                            endMin = endMinutes,
                            apps = apps.ifEmpty { listOf("com.facebook.katana") }
                        )
                        showAddScheduleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(if (lang == AppLanguage.BANGLA) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddScheduleDialog = false }) {
                    Text(if (lang == AppLanguage.BANGLA) "বাতিল" else "Cancel", color = textSecondary)
                }
            }
        )
    }

    // Session Completion Summary Dialog
    if (showSessionCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showSessionCompleteDialog = false },
            containerColor = cardBg,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "স্টাডি সেশন সম্পন্ন!" else "Focus Session Completed!",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (lang == AppLanguage.BANGLA)
                            "দারুণ ফোকাস! আপনার সেশন সফলভাবে সংরক্ষিত হয়েছে।"
                        else
                            "Great focus! Your session has been recorded successfully.",
                        fontSize = 13.sp,
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (lang == AppLanguage.BANGLA) "উৎপাদনশীলতা রেটিং:" else "Productivity Rating:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (star in 1..5) {
                            IconButton(
                                onClick = { productivityRating = star },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (star <= productivityRating) Color(0xFFFFD700) else textSecondary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = sessionNotes,
                        onValueChange = { sessionNotes = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "নোট বা অগ্রগতি (ঐচ্ছিক)" else "Notes / Progress Summary (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSessionCompleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(if (lang == AppLanguage.BANGLA) "ঠিক আছে" else "Done")
                }
            }
        )
    }
}
