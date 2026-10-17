package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.StudyStats
import com.example.ui.components.AddAdmissionBottomSheet
import com.example.ui.components.AddSubjectBottomSheet
import com.example.ui.components.CircularProgressMeter
import com.example.ui.components.StudyHubCard
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
import com.example.ui.theme.RoyalBlueLight
import com.example.ui.theme.SlateMuted
import com.example.util.AppLanguage
import com.example.util.AppStrings
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    stats: StudyStats,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val admissions by viewModel.admissions.collectAsStateWithLifecycle()

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    val now = System.currentTimeMillis()
    val nearestExam = admissions.filter { it.examDate > now }.minByOrNull { it.examDate }

    // Active learning chapter (in progress or first pending)
    val inProgressChapter = chapters.firstOrNull { it.status == "IN_PROGRESS" }
        ?: chapters.firstOrNull { it.status == "NOT_STARTED" }
    val continueSubject = inProgressChapter?.let { ch -> subjects.find { it.id == ch.subjectId } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Bar: Logo + Brand + Notification Bell
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_studyhub_symbol),
                        contentDescription = "StudyHub Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "StudyHub",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = textPrimary
                    )
                }

                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = { onNavigate(ScreenDestination.NOTIFICATIONS) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    if (admissions.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp, end = 8.dp)
                                .size(6.dp)
                                .background(RoyalBlue, CircleShape)
                        )
                    }
                }
            }
        }

        // Hero Greeting Block with user's name
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    val data = when (hour) {
                        in 5..11 -> Triple(AppStrings.greetingMorning(lang) + ",", "🌅", R.drawable.bg_morning)
                        in 12..16 -> Triple(AppStrings.greetingAfternoon(lang) + ",", "☀️", R.drawable.bg_morning)
                        in 17..20 -> Triple(AppStrings.greetingEvening(lang) + ",", "🌆", R.drawable.bg_night)
                        else -> Triple(AppStrings.greetingNight(lang) + ",", "🌙", R.drawable.bg_night)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = data.first,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = textPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = data.second, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) {
                                if (isDarkTheme) "আজকের পরিশ্রম, আগামীকালের সাফল্য।" else "প্রতিদিনের নিয়মিত চর্চায় গড়ে তুলুন নিশ্চিত স্বপ্ন।"
                            } else {
                                if (isDarkTheme) "Discipline today, success tomorrow." else "Small steps every day, big dreams tomorrow."
                            },
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }

                    Image(
                        painter = painterResource(id = data.third),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(70.dp).padding(start = 8.dp)
                    )
                }
            }
        }

        // Today's Study Time Card (100% Calculated from user's recorded study sessions)
        item {
            StudyHubCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = cardBg,
                borderColor = borderCol,
                onClick = { onNavigate(ScreenDestination.FOCUS) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.todayStudyTime(lang),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Dynamic hours & minutes
                        val hours = stats.todayStudySeconds / 3600
                        val mins = (stats.todayStudySeconds % 3600) / 60

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = AppStrings.formatNumber(hours, lang),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = textPrimary
                            )
                            Text(
                                text = if (lang == AppLanguage.BANGLA) " ঘণ্টা " else "h ",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = textSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = AppStrings.formatNumber(mins, lang),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = textPrimary
                            )
                            Text(
                                text = if (lang == AppLanguage.BANGLA) " মি." else "m",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = textSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (stats.todayStudySeconds > 0)
                                (if (lang == AppLanguage.BANGLA) "দৈনিক লক্ষ্য: ৪ ঘণ্টা" else "Daily goal: 4h")
                            else
                                (if (lang == AppLanguage.BANGLA) "স্টাডি শুরু করতে ট্যাপ করুন" else "Tap to start studying"),
                            fontSize = 12.sp,
                            color = if (stats.todayStudySeconds > 0) textSecondary else RoyalBlue,
                            fontWeight = if (stats.todayStudySeconds > 0) FontWeight.Normal else FontWeight.SemiBold
                        )
                    }

                    // Circular Progress Meter based on target 4h (14400s)
                    val goalSecs = 4 * 3600f
                    val progressFloat = (stats.todayStudySeconds / goalSecs).coerceIn(0f, 1f)
                    val pct = (progressFloat * 100).toInt()

                    CircularProgressMeter(
                        progress = progressFloat,
                        size = 68.dp,
                        color = RoyalBlue,
                        backgroundColor = if (isDarkTheme) Color(0xFF1E2C46) else Color(0xFFE2E8F0),
                        strokeWidth = 6.dp
                    ) {
                        Text(
                            text = "${AppStrings.formatNumber(pct, lang)}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                }
            }
        }

        // Side-by-side Cards: Syllabus Progress & Next Exam (Calculated Dynamically)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Card: Syllabus Progress
                StudyHubCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = cardBg,
                    borderColor = borderCol,
                    onClick = { onNavigate(ScreenDestination.SYLLABUS) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        if (isDarkTheme) MintGreen.copy(alpha = 0.2f) else MintGreenLight,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MintGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.syllabusCompletion(lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${AppStrings.formatNumber(stats.completionPercent.toInt(), lang)}%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = textPrimary
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = AppStrings.chaptersCount(stats.completedChapters, stats.totalChapters, lang),
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }

                // Right Card: Next Exam (Calculated from user admission entries)
                val (examDaysText, examNameText) = if (nearestExam != null) {
                    val days = TimeUnit.MILLISECONDS.toDays(nearestExam.examDate - now).coerceAtLeast(0).toInt()
                    Pair(AppStrings.daysRemaining(days, lang), nearestExam.universityName)
                } else {
                    Pair(
                        if (lang == AppLanguage.BANGLA) "পরীক্ষা নেই" else "No exams",
                        if (lang == AppLanguage.BANGLA) "পরীক্ষা যোগ করুন" else "Tap to add exam"
                    )
                }

                StudyHubCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = cardBg,
                    borderColor = borderCol,
                    onClick = { onNavigate(ScreenDestination.CALENDAR) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) else RoyalBlueLight,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "পরবর্তী পরীক্ষা" else "Next Exam",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = examDaysText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = textPrimary,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = examNameText,
                            fontSize = 11.sp,
                            color = textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Continue Learning Section (Dynamic based on user chapters)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = AppStrings.continueLearning(lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = textPrimary
                )

                if (inProgressChapter != null && continueSubject != null) {
                    val remainingInSubject = chapters.count { it.subjectId == continueSubject.id && it.status != "COMPLETED" }
                    val subjectColor = try {
                        Color(android.graphics.Color.parseColor(continueSubject.colorHex))
                    } catch (e: Exception) {
                        RoyalBlue
                    }
                    val chapTitle = if (lang == AppLanguage.BANGLA && inProgressChapter.titleBn.isNotBlank())
                        inProgressChapter.titleBn else inProgressChapter.titleEn
                    val subName = if (lang == AppLanguage.BANGLA && continueSubject.nameBn.isNotBlank())
                        continueSubject.nameBn else continueSubject.nameEn

                    StudyHubCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = cardBg,
                        borderColor = borderCol,
                        onClick = { viewModel.openSubjectDetails(continueSubject.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(subjectColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = subjectColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = subName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = chapTitle,
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (lang == AppLanguage.BANGLA)
                                        "${AppStrings.toBengaliDigits(remainingInSubject.toString())} টি অধ্যায় বাকি"
                                    else
                                        "$remainingInSubject chapters left",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Open",
                                tint = textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    // Helpful empty state for Continue Learning
                    StudyHubCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = cardBg,
                        borderColor = borderCol,
                        onClick = { onNavigate(ScreenDestination.SYLLABUS) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(RoyalBlueLight, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) "পড়াশোনা শুরু করতে প্রস্তুত?" else "Ready to start studying?",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (lang == AppLanguage.BANGLA) "সিলেবাসে অধ্যায় যুক্ত করতে ট্যাপ করুন" else "Tap here to explore and add chapters",
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Open",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Section (Working actions)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = AppStrings.quickActions(lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = textPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Add Subject
                    QuickActionItem(
                        icon = Icons.Default.Add,
                        label = AppStrings.addSubjectAction(lang),
                        iconBg = if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) else RoyalBlueLight,
                        iconTint = RoyalBlue,
                        textPrimary = textPrimary,
                        onClick = { showAddSubjectDialog = true }
                    )

                    // 2. Plan Today
                    QuickActionItem(
                        icon = Icons.Default.CalendarMonth,
                        label = AppStrings.tabToday(lang),
                        iconBg = if (isDarkTheme) MintGreen.copy(alpha = 0.2f) else MintGreenLight,
                        iconTint = MintGreen,
                        textPrimary = textPrimary,
                        onClick = { onNavigate(ScreenDestination.SMART_PLANNER) }
                    )

                    // 3. Start Timer
                    QuickActionItem(
                        icon = Icons.Default.Timer,
                        label = AppStrings.startFocus(lang),
                        iconBg = if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) else RoyalBlueLight,
                        iconTint = RoyalBlue,
                        textPrimary = textPrimary,
                        onClick = { onNavigate(ScreenDestination.FOCUS) }
                    )

                    // 4. Add Exam
                    QuickActionItem(
                        icon = Icons.Default.CalendarToday,
                        label = AppStrings.addExam(lang),
                        iconBg = if (isDarkTheme) RoyalBlue.copy(alpha = 0.2f) else RoyalBlueLight,
                        iconTint = RoyalBlue,
                        textPrimary = textPrimary,
                        onClick = { showAddExamDialog = true }
                    )

                    // 5. Progress
                    QuickActionItem(
                        icon = Icons.Default.Info,
                        label = if (lang == AppLanguage.BANGLA) "অগ্রগতি" else "Progress",
                        iconBg = if (isDarkTheme) MintGreen.copy(alpha = 0.2f) else MintGreenLight,
                        iconTint = MintGreen,
                        textPrimary = textPrimary,
                        onClick = { onNavigate(ScreenDestination.PROGRESS_ANALYTICS) }
                    )
                }
            }
        }
    }

    if (showAddSubjectDialog) {
        AddSubjectBottomSheet(
            existingSubjects = subjects,
            isDarkTheme = isDarkTheme,
            lang = lang,
            onDismiss = { showAddSubjectDialog = false },
            onConfirm = { nameEn, nameBn, colorHex ->
                viewModel.addSubject(nameEn, nameBn, colorHex)
                showAddSubjectDialog = false
            }
        )
    }

    if (showAddExamDialog) {
        AddAdmissionBottomSheet(
            isDarkTheme = isDarkTheme,
            lang = lang,
            onDismiss = { showAddExamDialog = false },
            onConfirm = { uni, unit, dead, exam, notes ->
                viewModel.addAdmissionTarget(uni, unit, dead, exam, notes)
                showAddExamDialog = false
            }
        )
    }
}

@Composable
fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconBg: Color,
    iconTint: Color,
    textPrimary: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(iconBg, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textPrimary
        )
    }
}
