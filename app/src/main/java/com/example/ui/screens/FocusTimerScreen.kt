package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChapterEntity
import com.example.data.model.SubjectEntity
import com.example.service.StudyHubAccessibilityService
import com.example.ui.MainViewModel
import com.example.ui.TimerState
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.AppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    viewModel: MainViewModel,
    timerState: TimerState,
    subjects: List<SubjectEntity>,
    chapters: List<ChapterEntity>,
    isDarkTheme: Boolean = false,
    lang: AppLanguage,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val stats by viewModel.studyStats.collectAsStateWithLifecycle()
    val blockedApps by viewModel.blockedApps.collectAsStateWithLifecycle()
    val isBlockerEnabled by viewModel.isDistractionBlockerEnabled.collectAsStateWithLifecycle()
    val isAccessibilityActive by viewModel.isAccessibilityServiceActive.collectAsStateWithLifecycle()
    val hasNotifPermission by viewModel.hasNotificationPermission.collectAsStateWithLifecycle()
    val customTimerMinutes by viewModel.customTimerMinutes.collectAsStateWithLifecycle()

    // Dialog & sheet states
    var showAppPickerSheet by remember { mutableStateOf(false) }
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var showSubjectPickerSheet by remember { mutableStateOf(false) }
    var showDiscardConfirmDialog by remember { mutableStateOf(false) }
    var showSessionCompleteDialog by remember { mutableStateOf(false) }
    var testNotifSentBanner by remember { mutableStateOf(false) }

    // Session completion inputs
    var productivityRating by remember { mutableIntStateOf(5) }
    var sessionNotes by remember { mutableStateOf("") }

    // Runtime permission launcher for Android 13+
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.refreshNotificationPermission(context)
    }

    // Refresh accessibility and notification state on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAccessibilityStatus(context)
                viewModel.refreshNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Theme Tokens
    val bg = if (isDarkTheme) DarkMidnightBg else Color(0xFFF8FAFC)
    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else SlateBorder
    val textPrimary = if (isDarkTheme) DarkTextWhite else SlateDark
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted
    val pillInactiveBg = if (isDarkTheme) DarkMidnightSurface else Color(0xFFEDF2F7)

    // Current subject & chapter
    val selectedSubject = subjects.find { it.id == timerState.selectedSubjectId }
    val relevantChapters = chapters.filter { it.subjectId == (selectedSubject?.id ?: 0L) }
    val selectedChapter = relevantChapters.find { it.id == timerState.selectedChapterId }

    val subjectTitle = if (selectedSubject != null) {
        if (lang == AppLanguage.BANGLA && selectedSubject.nameBn.isNotBlank()) selectedSubject.nameBn else selectedSubject.nameEn
    } else {
        if (lang == AppLanguage.BANGLA) "সাধারণ ফোকাস" else "General Focus"
    }

    val chapterTitle = if (selectedChapter != null) {
        if (lang == AppLanguage.BANGLA && selectedChapter.titleBn.isNotBlank()) selectedChapter.titleBn else selectedChapter.titleEn
    } else {
        if (lang == AppLanguage.BANGLA) "স্টাডি সেশন" else "Study Session"
    }

    val isRunning = timerState.isRunning && !timerState.isPaused
    val isPaused = timerState.isRunning && timerState.isPaused
    val isIdle = !timerState.isRunning

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .testTag("focus_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("button_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(40.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = AppStrings.timerFocus(lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = textPrimary
                    )
                    Text(
                        text = AppStrings.focusSubtitle(lang),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = textSecondary
                    )
                }

                IconButton(
                    onClick = { showCustomDurationDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("button_focus_options")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Duration Settings",
                        tint = textPrimary
                    )
                }
            }
        }

        // 2. Subject & Chapter Tag
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = RoyalBlue.copy(alpha = if (isDarkTheme) 0.15f else 0.10f),
                border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.35f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(enabled = isIdle) { showSubjectPickerSheet = true }
                    .testTag("subject_tag_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$subjectTitle • $chapterTitle",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) Color(0xFF93C5FD) else RoyalBlue
                    )
                    if (isIdle) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 3. Preset Duration Selector Pills
        item {
            if (isIdle) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                            color = if (isSel) RoyalBlue else pillInactiveBg,
                            border = BorderStroke(1.dp, if (isSel) RoyalBlue else borderCol),
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
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }

                    // Custom duration pill
                    item {
                        val isCustomSelected = presets.none { it.first == timerState.targetSeconds }
                        val customMins = (timerState.targetSeconds / 60L).toInt()
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCustomSelected) RoyalBlue else pillInactiveBg,
                            border = BorderStroke(1.dp, if (isCustomSelected) RoyalBlue else borderCol),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showCustomDurationDialog = true }
                                .testTag("timer_preset_custom")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = if (isCustomSelected) Color.White else textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCustomSelected) "${AppStrings.formatNumber(customMins, lang)}m" else AppStrings.customDuration(lang),
                                    fontSize = 12.sp,
                                    fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCustomSelected) Color.White else textSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                // When running or paused, show non-editable session pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = (if (isRunning) MintGreen else AmberWarning).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, (if (isRunning) MintGreen else AmberWarning).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isRunning) MintGreen else AmberWarning)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRunning) {
                                if (lang == AppLanguage.BANGLA) "সক্রিয় ফোকাস" else "Deep Focus Running"
                            } else {
                                if (lang == AppLanguage.BANGLA) "বিরতি / স্থগিত" else "Session Paused"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) MintGreen else AmberWarning
                        )
                    }
                }
            }
        }

        // 4. Circular Countdown Timer Visualization
        item {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (timerState.targetSeconds > 0) {
                    (timerState.currentSeconds.toFloat() / timerState.targetSeconds.toFloat()).coerceIn(0f, 1f)
                } else 0f

                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 800),
                    label = "timerDial"
                )

                val ringTrackColor = if (isDarkTheme) Color(0xFF1E2C46) else Color(0xFFE2E8F0)
                val ringActiveColor = if (isPaused) AmberWarning else RoyalBlue

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("timer_display")
                ) {
                    val strokeWidth = 14.dp.toPx()
                    drawCircle(
                        color = ringTrackColor,
                        radius = size.minDimension / 2 - strokeWidth / 2,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ringActiveColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val totalSecs = timerState.currentSeconds
                    val hours = totalSecs / 3600
                    val minutes = (totalSecs % 3600) / 60
                    val seconds = totalSecs % 60
                    val timeStr = if (hours > 0) {
                        "%02d:%02d:%02d".format(hours, minutes, seconds)
                    } else {
                        "%02d:%02d".format(minutes, seconds)
                    }
                    val displayTime = if (lang == AppLanguage.BANGLA) AppStrings.toBengaliDigits(timeStr) else timeStr

                    Text(
                        text = displayTime,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (hours > 0) 36.sp else 46.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = when {
                            isRunning -> if (lang == AppLanguage.BANGLA) "মনোযোগ দিন" else "Stay Focused"
                            isPaused -> if (lang == AppLanguage.BANGLA) "বিরতি চলছে" else "Take a Breath"
                            else -> if (lang == AppLanguage.BANGLA) "শুরু করতে প্রস্তুত" else "Ready to Focus"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) Color(0xFF93C5FD) else RoyalBlue
                    )
                }
            }
        }

        // 5. Primary & Secondary Timer Controls Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary 1: Restart
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val elapsed = timerState.targetSeconds - timerState.currentSeconds
                            if (timerState.isRunning && elapsed > 10L) {
                                showDiscardConfirmDialog = true
                            } else {
                                viewModel.resetTimer()
                            }
                        }
                        .padding(8.dp)
                        .testTag("button_restart_timer")
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(cardBg, CircleShape)
                            .border(1.dp, borderCol, CircleShape),
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

                // Primary Action: Start / Pause / Resume (Big RoyalBlue circular button)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(RoyalBlue)
                        .clickable {
                            if (isRunning) {
                                viewModel.pauseTimer()
                            } else {
                                viewModel.startTimer()
                            }
                        }
                        .testTag("button_start_pause_timer"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) AppStrings.pauseTimer(lang) else AppStrings.startTimer(lang),
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Secondary 2: Finish Session
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
                            .background(cardBg, CircleShape)
                            .border(1.dp, borderCol, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = AppStrings.stopTimer(lang),
                            tint = MintGreen,
                            modifier = Modifier.size(26.dp)
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

        // 6. Session Progress Indicators (1 to 4 dots)
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 2.dp)
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

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.width(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 1..timerState.totalSessions) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (i <= timerState.currentSession) RoyalBlue
                                    else if (isDarkTheme) Color(0xFF1E2C46) else Color(0xFFE2E8F0)
                                )
                        )
                    }
                }
            }
        }

        // 7. Distraction Control & App Blocker Card (Accessibility Based)
        item {
            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderCol
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header of the card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RoyalBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = AppStrings.distractionControlTitle(lang),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = AppStrings.blockedAppsCount(blockedApps.size, lang),
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isBlockerEnabled,
                            onCheckedChange = { viewModel.toggleDistractionBlocker() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = RoyalBlue
                            ),
                            modifier = Modifier.testTag("switch_distraction_blocker")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Overall Blocking Status Alert Badge
                    val statusText: String
                    val statusBg: Color
                    val statusContentColor: Color
                    val statusIcon = when {
                        !isBlockerEnabled -> {
                            statusText = if (lang == AppLanguage.BANGLA) "ব্লকার বন্ধ রয়েছে" else "Blocker is turned OFF"
                            statusBg = pillInactiveBg
                            statusContentColor = textSecondary
                            Icons.Default.Block
                        }
                        !isAccessibilityActive || blockedApps.isEmpty() -> {
                            statusText = AppStrings.blockerStatusSetupNeeded(lang)
                            statusBg = AmberWarning.copy(alpha = 0.12f)
                            statusContentColor = AmberWarning
                            Icons.Default.WarningAmber
                        }
                        isPaused -> {
                            statusText = AppStrings.blockerStatusPaused(lang)
                            statusBg = AmberWarning.copy(alpha = 0.12f)
                            statusContentColor = AmberWarning
                            Icons.Default.PauseCircle
                        }
                        isRunning -> {
                            statusText = AppStrings.blockerStatusActive(lang)
                            statusBg = MintGreen.copy(alpha = 0.12f)
                            statusContentColor = MintGreen
                            Icons.Default.CheckCircle
                        }
                        else -> {
                            statusText = AppStrings.blockerStatusReady(lang)
                            statusBg = RoyalBlue.copy(alpha = 0.12f)
                            statusContentColor = RoyalBlue
                            Icons.Default.Info
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = statusBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusContentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusContentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 1: Accessibility Service Status & Open Settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(pillInactiveBg)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (isAccessibilityActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isAccessibilityActive) MintGreen else AlertRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = AppStrings.accessibilityServiceTitle(lang),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = if (isAccessibilityActive) AppStrings.accessibilityActive(lang) else AppStrings.accessibilityInactive(lang),
                                    fontSize = 11.sp,
                                    color = if (isAccessibilityActive) MintGreen else AlertRed
                                )
                            }
                        }

                        if (!isAccessibilityActive) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("button_open_accessibility_settings")
                            ) {
                                Text(
                                    text = AppStrings.openAccessibilitySettings(lang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Manage Blocked Apps Action
                    OutlinedButton(
                        onClick = { showAppPickerSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, borderCol),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_manage_blocked_apps")
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${AppStrings.manageBlockedApps(lang)} (${blockedApps.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Privacy Note
                    Text(
                        text = AppStrings.accessibilityExplanation(lang),
                        fontSize = 10.sp,
                        color = textSecondary,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // 8. Real Android Push / System Notifications Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RoyalBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = AppStrings.notificationPermissionTitle(lang),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = if (hasNotifPermission) AppStrings.notificationPermissionGranted(lang) else AppStrings.notificationPermissionRequired(lang),
                                    fontSize = 11.sp,
                                    color = if (hasNotifPermission) MintGreen else AmberWarning
                                )
                            }
                        }

                        if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Button(
                                onClick = {
                                    notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("button_request_notification_permission")
                            ) {
                                Text(AppStrings.grantPermission(lang), fontSize = 11.sp)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MintGreen.copy(alpha = 0.15f),
                                contentColor = MintGreen
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = MintGreen)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "সক্রিয় রয়েছে" else "Active",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 9. Today's Progress Card (Live Stats)
        item {
            val formattedToday = AppStrings.formatDuration(stats.todayStudySeconds, lang)
            val todayProgress = (stats.todayStudySeconds.toFloat() / 14400f).coerceIn(0f, 1f)

            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderCol
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = AppStrings.sessionProgress(lang),
                        fontSize = 12.sp,
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "$formattedToday / ৪ ঘণ্টা লক্ষ্য" else "$formattedToday / 4h Goal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = textPrimary
                        )

                        Text(
                            text = "${(todayProgress * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { todayProgress },
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
    }

    // --- MODALS & DIALOGS ---

    // 1. App Picker Dialog (Lists installed launchable apps for blocking)
    if (showAppPickerSheet) {
        InstalledAppsPickerDialog(
            context = context,
            initialBlockedApps = blockedApps,
            isDarkTheme = isDarkTheme,
            lang = lang,
            onDismiss = { showAppPickerSheet = false },
            onSave = { updatedApps ->
                viewModel.setBlockedApps(updatedApps)
                showAppPickerSheet = false
            }
        )
    }

    // 2. Custom Duration Dialog (1–240 minutes)
    if (showCustomDurationDialog) {
        CustomDurationDialog(
            initialMinutes = customTimerMinutes,
            isDarkTheme = isDarkTheme,
            lang = lang,
            onDismiss = { showCustomDurationDialog = false },
            onApply = { newMinutes ->
                viewModel.setCustomTimerMinutes(newMinutes)
                showCustomDurationDialog = false
            }
        )
    }

    // 3. Subject & Chapter Selector Dialog
    if (showSubjectPickerSheet) {
        SubjectChapterPickerDialog(
            subjects = subjects,
            chapters = chapters,
            selectedSubjectId = timerState.selectedSubjectId,
            selectedChapterId = timerState.selectedChapterId,
            isDarkTheme = isDarkTheme,
            lang = lang,
            onDismiss = { showSubjectPickerSheet = false },
            onSelect = { subId, chapId ->
                viewModel.selectTimerSubject(subId)
                viewModel.selectTimerChapter(chapId)
                showSubjectPickerSheet = false
            }
        )
    }

    // 4. Discard/Restart Confirmation Dialog
    if (showDiscardConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmDialog = false },
            containerColor = cardBg,
            title = {
                Text(
                    text = AppStrings.discardConfirmTitle(lang),
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = AppStrings.discardConfirmMessage(lang),
                    color = textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetTimer()
                        showDiscardConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    modifier = Modifier.testTag("button_confirm_restart")
                ) {
                    Text(AppStrings.restartTimer(lang), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirmDialog = false }) {
                    Text(AppStrings.cancelAction(lang), color = textSecondary)
                }
            }
        )
    }

    // 5. Session Complete Summary Dialog
    if (showSessionCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showSessionCompleteDialog = false },
            containerColor = cardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "সেশন সফলভাবে সম্পন্ন!" else "Focus Session Complete!",
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (lang == AppLanguage.BANGLA)
                            "অসাধারণ মনোযোগ! সেশনের বিবরণ এবং অগ্রগতি সফলভাবে ডাটাবেজে সংরক্ষণ করা হয়েছে।"
                        else
                            "Great job staying in the zone! Your focus duration and study stats have been recorded.",
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

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (star in 1..5) {
                            IconButton(
                                onClick = { productivityRating = star },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (star <= productivityRating) AmberWarning else textSecondary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = sessionNotes,
                        onValueChange = { sessionNotes = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "নোট বা অগ্রগতি (ঐচ্ছিক)" else "Notes / Topics Covered (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSessionCompleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier.testTag("button_done_session_summary")
                ) {
                    Text(AppStrings.doneAction(lang))
                }
            }
        )
    }
}

/**
 * Dialog to select installed apps for Distraction Blocker
 */
@Composable
fun InstalledAppsPickerDialog(
    context: Context,
    initialBlockedApps: Set<String>,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedApps by remember { mutableStateOf(initialBlockedApps.toMutableSet()) }
    var installedApps by remember { mutableStateOf<List<InstalledAppItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val pkgAppsList: List<ResolveInfo> = pm.queryIntentActivities(mainIntent, 0)

            val ownPackage = context.packageName
            val apps = pkgAppsList.mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == ownPackage || StudyHubAccessibilityService.SYSTEM_WHITELIST.contains(pkgName)) {
                    null
                } else {
                    val appLabel = resolveInfo.loadLabel(pm).toString()
                    val icon = try { resolveInfo.loadIcon(pm) } catch (_: Exception) { null }
                    InstalledAppItem(pkgName, appLabel, icon)
                }
            }.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }

            installedApps = apps
            isLoading = false
        }
    }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val textPrimary = if (isDarkTheme) DarkTextWhite else SlateDark
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted
    val borderCol = if (isDarkTheme) DarkBorder else SlateBorder

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, borderCol),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Title & Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.selectAppsToBlock(lang),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "${selectedApps.size} apps selected",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(AppStrings.searchApps(lang), fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_apps"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Select All / Clear All buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        val all = installedApps.map { it.packageName }.toSet()
                        selectedApps = all.toMutableSet()
                    }) {
                        Text(AppStrings.selectAll(lang), fontSize = 12.sp)
                    }

                    TextButton(onClick = {
                        selectedApps = mutableSetOf()
                    }) {
                        Text(AppStrings.clearAll(lang), fontSize = 12.sp, color = AlertRed)
                    }
                }

                Divider(color = borderCol)

                // List of Apps
                if (isLoading) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RoyalBlue)
                    }
                } else {
                    val filteredApps = installedApps.filter {
                        it.appName.contains(searchQuery, ignoreCase = true) ||
                                it.packageName.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("list_installed_apps"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { appItem ->
                            val isChecked = selectedApps.contains(appItem.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val newSet = selectedApps.toMutableSet()
                                        if (isChecked) newSet.remove(appItem.packageName)
                                        else newSet.add(appItem.packageName)
                                        selectedApps = newSet
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (appItem.icon != null) {
                                    val bitmap = remember(appItem.packageName) {
                                        try { appItem.icon.toBitmap(96, 96).asImageBitmap() } catch (_: Exception) { null }
                                    }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(36.dp), tint = textSecondary)
                                    }
                                } else {
                                    Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(36.dp), tint = textSecondary)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = appItem.appName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = appItem.packageName,
                                        fontSize = 10.sp,
                                        color = textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        val newSet = selectedApps.toMutableSet()
                                        if (checked) newSet.add(appItem.packageName)
                                        else newSet.remove(appItem.packageName)
                                        selectedApps = newSet
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = RoyalBlue)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Done Button
                Button(
                    onClick = { onSave(selectedApps) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_save_blocked_apps")
                ) {
                    Text(AppStrings.doneAction(lang), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

/**
 * Custom Duration Dialog (1 to 240 mins)
 */
@Composable
fun CustomDurationDialog(
    initialMinutes: Int,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onApply: (Int) -> Unit
) {
    var minutesValue by remember { mutableFloatStateOf(initialMinutes.toFloat()) }
    var textInput by remember { mutableStateOf(initialMinutes.toString()) }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val textPrimary = if (isDarkTheme) DarkTextWhite else SlateDark
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted
    val borderCol = if (isDarkTheme) DarkBorder else SlateBorder

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        title = {
            Text(
                text = AppStrings.customDurationTitle(lang),
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = AppStrings.customDurationPrompt(lang),
                    fontSize = 12.sp,
                    color = textSecondary
                )

                // Large Number display
                Text(
                    text = "${AppStrings.formatNumber(minutesValue.toInt(), lang)} ${AppStrings.minutesLabel(lang)}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                )

                // Slider from 1 to 240 mins
                Slider(
                    value = minutesValue,
                    onValueChange = {
                        minutesValue = it
                        textInput = it.toInt().toString()
                    },
                    valueRange = 1f..240f,
                    steps = 238,
                    colors = SliderDefaults.colors(
                        thumbColor = RoyalBlue,
                        activeTrackColor = RoyalBlue
                    )
                )

                // Quick preset tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15, 30, 50, 75, 120).forEach { m ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (minutesValue.toInt() == m) RoyalBlue else (if (isDarkTheme) Color(0xFF1E2C46) else Color(0xFFEDF2F7)),
                            border = BorderStroke(1.dp, borderCol),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    minutesValue = m.toFloat()
                                    textInput = m.toString()
                                }
                        ) {
                            Text(
                                text = "${m}m",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (minutesValue.toInt() == m) Color.White else textSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(minutesValue.toInt().coerceIn(1, 240)) },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                modifier = Modifier.testTag("button_apply_custom_duration")
            ) {
                Text(AppStrings.applyAction(lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.cancelAction(lang), color = textSecondary)
            }
        }
    )
}

/**
 * Subject & Chapter Picker Dialog
 */
@Composable
fun SubjectChapterPickerDialog(
    subjects: List<SubjectEntity>,
    chapters: List<ChapterEntity>,
    selectedSubjectId: Long?,
    selectedChapterId: Long?,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSelect: (Long?, Long?) -> Unit
) {
    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val textPrimary = if (isDarkTheme) DarkTextWhite else SlateDark
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted
    val borderCol = if (isDarkTheme) DarkBorder else SlateBorder

    var tempSubjectId by remember { mutableStateOf(selectedSubjectId) }
    var tempChapterId by remember { mutableStateOf(selectedChapterId) }

    val relevantChapters = chapters.filter { it.subjectId == (tempSubjectId ?: -1L) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, borderCol),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Text(
                    text = AppStrings.selectSubject(lang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Option 0: General Focus
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (tempSubjectId == null) RoyalBlue.copy(alpha = 0.15f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (tempSubjectId == null) RoyalBlue else borderCol),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    tempSubjectId = null
                                    tempChapterId = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AllInclusive, contentDescription = null, tint = RoyalBlue)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) "সাধারণ ফোকাস (কোনো বিষয় নয়)" else "General Focus (No subject)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                            }
                        }
                    }

                    items(subjects) { sub ->
                        val isSel = tempSubjectId == sub.id
                        val name = if (lang == AppLanguage.BANGLA && sub.nameBn.isNotBlank()) sub.nameBn else sub.nameEn
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) RoyalBlue.copy(alpha = 0.15f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSel) RoyalBlue else borderCol),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    tempSubjectId = sub.id
                                    tempChapterId = null
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )

                                if (isSel && relevantChapters.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = AppStrings.selectChapter(lang),
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    relevantChapters.forEach { ch ->
                                        val isChSel = tempChapterId == ch.id
                                        val chTitle = if (lang == AppLanguage.BANGLA && ch.titleBn.isNotBlank()) ch.titleBn else ch.titleEn
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { tempChapterId = ch.id }
                                                .padding(vertical = 4.dp, horizontal = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isChSel,
                                                onClick = { tempChapterId = ch.id },
                                                colors = RadioButtonDefaults.colors(selectedColor = RoyalBlue)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = chTitle,
                                                fontSize = 12.sp,
                                                color = if (isChSel) RoyalBlue else textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.cancelAction(lang), color = textSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSelect(tempSubjectId, tempChapterId) },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier.testTag("button_confirm_subject_selection")
                    ) {
                        Text(AppStrings.doneAction(lang))
                    }
                }
            }
        }
    }
}
