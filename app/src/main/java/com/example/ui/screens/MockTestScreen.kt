package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Score
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MockTestEntity
import com.example.data.model.SubjectEntity
import com.example.ui.MainViewModel
import com.example.ui.components.StudyHubCard
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val mockTests by viewModel.mockTests.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val analytics by viewModel.admissionAnalytics.collectAsStateWithLifecycle()

    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
    var selectedProvider by remember { mutableStateOf<String?>(null) }
    var selectedDateRange by remember { mutableStateOf("ALL") } // "ALL", "7_DAYS", "30_DAYS"

    var showAddDialog by remember { mutableStateOf(false) }
    var testToEdit by remember { mutableStateOf<MockTestEntity?>(null) }
    var testToDelete by remember { mutableStateOf<MockTestEntity?>(null) }
    var showTargetDialog by remember { mutableStateOf(false) }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    val subjectsMap = remember(subjects) { subjects.associateBy { it.id } }
    val distinctProviders = remember(mockTests) {
        mockTests.map { it.examProvider.trim().lowercase(java.util.Locale.US) }
            .distinct()
            .filter { it.isNotBlank() }
            .map { provider ->
                when(provider) {
                    "udvash" -> "Udvash"
                    else -> provider.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.US) else it.toString() }
                }
            }
    }

    val now = System.currentTimeMillis()
    val dayMs = 86_400_000L

    // Filter tests
    val filteredTests = remember(mockTests, selectedSubjectId, selectedProvider, selectedDateRange) {
        mockTests.filter { test ->
            val matchSubject = selectedSubjectId == null || test.subjectId == selectedSubjectId
            val matchProvider = selectedProvider == null || test.examProvider.equals(selectedProvider, ignoreCase = true)
            val matchDate = when (selectedDateRange) {
                "7_DAYS" -> test.testDate >= (now - (7 * dayMs))
                "30_DAYS" -> test.testDate >= (now - (30 * dayMs))
                else -> true
            }
            matchSubject && matchProvider && matchDate
        }.sortedByDescending { it.testDate }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .testTag("mock_test_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Back Arrow + Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp).testTag("mock_test_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "মক টেস্ট ও মূল্যায়ন" else "Mock Tests & Performance",
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
                    text = if (lang == AppLanguage.BANGLA)
                        "মক টেস্টের ফলাফল সংরক্ষণ করুন এবং প্রস্তুতি বিশ্লেষণ দেখুন"
                    else
                        "Record your mock exam scores and track your admission test readiness",
                    fontSize = 12.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Performance Analytics Summary Card
            item {
                StudyHubCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = cardBg,
                    borderColor = borderCol
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "সামগ্রিক পারফরম্যান্স" else "Performance Summary",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textPrimary
                            )

                            TextButton(
                                onClick = { showTargetDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.BANGLA)
                                        "টার্গেট: ${AppStrings.formatNumber(analytics.targetScorePercentage.toInt(), lang)}%"
                                    else
                                        "Target: ${analytics.targetScorePercentage.toInt()}%",
                                    fontSize = 12.sp,
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Avg % Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isDarkTheme) RoyalBlue.copy(alpha = 0.15f) else RoyalBlueLight,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "গড় নম্বর" else "Average",
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (analytics.totalTestsTaken > 0)
                                            "${AppStrings.formatNumber(String.format(Locale.US, "%.1f", analytics.overallAveragePercentage), lang)}%"
                                        else "--",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (analytics.totalTestsTaken > 0)
                                            "${AppStrings.formatNumber(String.format(Locale.US, "%.1f", analytics.overallAverageScore), lang)} pts"
                                        else "0 tests",
                                        fontSize = 10.sp,
                                        color = textSecondary
                                    )
                                }
                            }

                            // Latest vs Previous Box
                            val delta = analytics.deltaPercentageFromPrevious
                            val (deltaBg, deltaFg) = when {
                                delta == null -> (if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)) to textSecondary
                                delta >= 0 -> (if (isDarkTheme) MintGreen.copy(alpha = 0.15f) else MintGreenLight) to MintGreen
                                else -> (if (isDarkTheme) AlertRed.copy(alpha = 0.15f) else AlertRedLight) to AlertRed
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(deltaBg, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "সর্বশেষ স্কোর" else "Latest Score",
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (analytics.latestTestPercentage != null)
                                            "${AppStrings.formatNumber(String.format(Locale.US, "%.1f", analytics.latestTestPercentage), lang)}%"
                                        else "--",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (delta != null) {
                                            Icon(
                                                imageVector = if (delta >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = deltaFg,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${if (delta >= 0) "+" else ""}${AppStrings.formatNumber(String.format(Locale.US, "%.1f", delta), lang)}%",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = deltaFg
                                            )
                                        } else {
                                            Text(
                                                text = if (lang == AppLanguage.BANGLA) "পূর্বের টেস্ট নেই" else "No prior test",
                                                fontSize = 10.sp,
                                                color = textSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            // Target Progress Box
                            val gap = analytics.targetGapPercentage
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "টার্গেট গ্যাপ" else "Target Gap",
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (gap != null && analytics.totalTestsTaken > 0) {
                                            if (gap <= 0) "Met!"
                                            else "-${AppStrings.formatNumber(String.format(Locale.US, "%.0f", gap), lang)}%"
                                        } else "--",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = if (gap != null && gap <= 0) MintGreen else textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${AppStrings.formatNumber(analytics.totalTestsTaken, lang)} ${if (lang == AppLanguage.BANGLA) "টি টেস্ট" else "tests"}",
                                        fontSize = 10.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                        }

                        // Small sample notice
                        if (analytics.isSmallSample) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == AppLanguage.BANGLA)
                                        "কম সংখ্যক (${AppStrings.formatNumber(analytics.totalTestsTaken, lang)}টি) টেস্টের উপর ভিত্তি করে তৈরি। আরও টেস্ট দিলে বিশ্লেষণ নির্ভুল হবে।"
                                    else
                                        "Based on a small sample (${analytics.totalTestsTaken} test). Complete more tests for reliable trends.",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }
                        }

                        // Score trend sparkline canvas
                        if (mockTests.size >= 2) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "স্কোর প্রবণতা (টাইমলাইন)" else "Score Trend (Chronological)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val chronologicalTests = remember(mockTests) { mockTests.sortedBy { it.testDate } }
                            val points = remember(chronologicalTests) {
                                chronologicalTests.map { test ->
                                    val net = (test.obtainedMarks - test.negativeMarks).coerceAtLeast(0.0)
                                    if (test.totalMarks > 0) (net / test.totalMarks) * 100.0 else 0.0
                                }
                            }

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                            ) {
                                if (points.size >= 2) {
                                    val maxVal = 100.0
                                    val minVal = 0.0
                                    val stepX = size.width / (points.size - 1)
                                    val path = Path()

                                    points.forEachIndexed { i, pct ->
                                        val x = i * stepX
                                        val y = size.height - ((pct - minVal) / (maxVal - minVal) * size.height).toFloat()
                                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }

                                    // Draw subtle target guide line
                                    val targetY = size.height - (analytics.targetScorePercentage / 100.0 * size.height).toFloat()
                                    drawLine(
                                        color = MintGreen.copy(alpha = 0.4f),
                                        start = Offset(0f, targetY),
                                        end = Offset(size.width, targetY),
                                        strokeWidth = 1.dp.toPx()
                                    )

                                    // Draw line
                                    drawPath(
                                        path = path,
                                        color = RoyalBlue,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // Draw points
                                    points.forEachIndexed { i, pct ->
                                        val x = i * stepX
                                        val y = size.height - ((pct - minVal) / (maxVal - minVal) * size.height).toFloat()
                                        drawCircle(
                                            color = Color.White,
                                            radius = 4.dp.toPx(),
                                            center = Offset(x, y)
                                        )
                                        drawCircle(
                                            color = RoyalBlue,
                                            radius = 2.5.dp.toPx(),
                                            center = Offset(x, y)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Filters: Subjects & Date Ranges
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Subject Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        item {
                            FilterPill(
                                label = if (lang == AppLanguage.BANGLA) "সব বিষয়" else "All Subjects",
                                isSelected = selectedSubjectId == null,
                                isDarkTheme = isDarkTheme,
                                onClick = { selectedSubjectId = null }
                            )
                        }
                        items(subjects, key = { it.id }) { sub ->
                            FilterPill(
                                label = if (lang == AppLanguage.BANGLA) sub.nameBn else sub.nameEn,
                                isSelected = selectedSubjectId == sub.id,
                                isDarkTheme = isDarkTheme,
                                onClick = { selectedSubjectId = sub.id }
                            )
                        }
                    }

                    // Provider Pills
                    if (distinctProviders.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            item {
                                FilterPill(
                                    label = if (lang == AppLanguage.BANGLA) "সব প্রতিষ্ঠান" else "All Providers",
                                    isSelected = selectedProvider == null,
                                    isDarkTheme = isDarkTheme,
                                    onClick = { selectedProvider = null }
                                )
                            }
                            items(distinctProviders) { prov ->
                                FilterPill(
                                    label = prov,
                                    isSelected = selectedProvider.equals(prov, ignoreCase = true),
                                    isDarkTheme = isDarkTheme,
                                    onClick = { selectedProvider = prov }
                                )
                            }
                        }
                    }

                    // Date Range Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterPill(
                            label = if (lang == AppLanguage.BANGLA) "সব সময়" else "All Time",
                            isSelected = selectedDateRange == "ALL",
                            isDarkTheme = isDarkTheme,
                            onClick = { selectedDateRange = "ALL" }
                        )
                        FilterPill(
                            label = if (lang == AppLanguage.BANGLA) "গত ৭ দিন" else "Last 7 Days",
                            isSelected = selectedDateRange == "7_DAYS",
                            isDarkTheme = isDarkTheme,
                            onClick = { selectedDateRange = "7_DAYS" }
                        )
                        FilterPill(
                            label = if (lang == AppLanguage.BANGLA) "গত ৩০ দিন" else "Last 30 Days",
                            isSelected = selectedDateRange == "30_DAYS",
                            isDarkTheme = isDarkTheme,
                            onClick = { selectedDateRange = "30_DAYS" }
                        )
                    }
                }
            }

            // Test Records List
            if (filteredTests.isEmpty()) {
                item {
                    StudyHubCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = cardBg,
                        borderColor = borderCol
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(RoyalBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Score,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "কোনো মক টেস্ট পাওয়া যায়নি" else "No Mock Tests Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA)
                                    "নিচের '+' বাটনে ট্যাপ করে নতুন মক টেস্টের ফলাফল যোগ করুন।"
                                else
                                    "Tap the '+' button below to record your first mock test result.",
                                fontSize = 13.sp,
                                color = textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                modifier = Modifier.testTag("record_first_mock_test_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == AppLanguage.BANGLA) "টেস্ট যোগ করুন" else "Record Test")
                            }
                        }
                    }
                }
            } else {
                items(filteredTests, key = { it.id }) { test ->
                    val sub = test.subjectId?.let { subjectsMap[it] }
                    val subName = if (lang == AppLanguage.BANGLA) sub?.nameBn ?: "পূর্ণাঙ্গ টেস্ট" else sub?.nameEn ?: "Full Test"
                    val subColor = sub?.let {
                        try { Color(android.graphics.Color.parseColor(it.colorHex)) } catch (e: Exception) { RoyalBlue }
                    } ?: RoyalBlue

                    val netMarks = (test.obtainedMarks - test.negativeMarks).coerceAtLeast(0.0)
                    val pct = if (test.totalMarks > 0) (netMarks / test.totalMarks) * 100.0 else 0.0

                    val dateFormatted = remember(test.testDate) {
                        SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(test.testDate))
                    }

                    val badgeColor = when {
                        pct >= 80.0 -> MintGreen
                        pct >= 60.0 -> RoyalBlue
                        pct >= 40.0 -> Color(0xFFF59E0B)
                        else -> AlertRed
                    }

                    StudyHubCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mock_test_item_${test.id}"),
                        backgroundColor = cardBg,
                        borderColor = borderCol
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                            .size(8.dp)
                                            .background(subColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = test.testName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = textPrimary,
                                        maxLines = 1
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = badgeColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${AppStrings.formatNumber(String.format(Locale.US, "%.1f", pct), lang)}%",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Provider & Subject Badges
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = test.examProvider,
                                        fontSize = 11.sp,
                                        color = textSecondary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = subColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = subName,
                                        fontSize = 11.sp,
                                        color = subColor,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (test.durationMinutes != null && test.durationMinutes > 0) {
                                    Text(
                                        text = "${AppStrings.formatNumber(test.durationMinutes, lang)} ${if (lang == AppLanguage.BANGLA) "মি." else "min"}",
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Score Breakdown row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${AppStrings.formatNumber(String.format(Locale.US, "%.1f", netMarks), lang)} / ${AppStrings.formatNumber(String.format(Locale.US, "%.0f", test.totalMarks), lang)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = textPrimary
                                    )
                                    if (test.negativeMarks > 0) {
                                        Text(
                                            text = "${if (lang == AppLanguage.BANGLA) "নেগেটিভ" else "Negative"}: -${AppStrings.formatNumber(String.format(Locale.US, "%.2f", test.negativeMarks), lang)}",
                                            fontSize = 11.sp,
                                            color = AlertRed
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dateFormatted,
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { testToEdit = test },
                                        modifier = Modifier.size(32.dp).testTag("edit_mock_test_${test.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { testToDelete = test },
                                        modifier = Modifier.size(32.dp).testTag("delete_mock_test_${test.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = textSecondary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
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
                .testTag("fab_add_mock_test"),
            containerColor = RoyalBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Mock Test")
        }
    }

    // Add Mock Test Dialog
    if (showAddDialog) {
        MockTestEditDialog(
            isDarkTheme = isDarkTheme,
            lang = lang,
            subjects = subjects,
            existingTest = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, provider, subId, date, total, obtained, neg, duration ->
                viewModel.addMockTest(name, provider, subId, date, total, obtained, neg, duration)
                showAddDialog = false
            }
        )
    }

    // Edit Mock Test Dialog
    if (testToEdit != null) {
        MockTestEditDialog(
            isDarkTheme = isDarkTheme,
            lang = lang,
            subjects = subjects,
            existingTest = testToEdit,
            onDismiss = { testToEdit = null },
            onConfirm = { name, provider, subId, date, total, obtained, neg, duration ->
                viewModel.updateMockTest(testToEdit!!.id, name, provider, subId, date, total, obtained, neg, duration)
                testToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (testToDelete != null) {
        val test = testToDelete!!
        AlertDialog(
            onDismissRequest = { testToDelete = null },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "মক টেস্ট মুছবেন?" else "Delete Mock Test?",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = if (lang == AppLanguage.BANGLA)
                        "আপনি কি \"${test.testName}\" রেকর্ডটি মুছে ফেলতে চান?"
                    else
                        "Are you sure you want to permanently delete '${test.testName}'?",
                    color = textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMockTest(test)
                        testToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_mock_test")
                ) {
                    Text(if (lang == AppLanguage.BANGLA) "মুছুন" else "Delete", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { testToDelete = null }) {
                    Text(if (lang == AppLanguage.BANGLA) "বাতিল" else "Cancel", color = textSecondary)
                }
            }
        )
    }

    // Target Score Setting Dialog
    if (showTargetDialog) {
        var targetInput by remember { mutableStateOf(analytics.targetScorePercentage.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "টার্গেট স্কোর নির্ধারণ" else "Set Target Score %",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = if (lang == AppLanguage.BANGLA)
                            "ভর্তি পরীক্ষার জন্য আপনার লক্ষ্যমাত্রা শতকরা কত?"
                        else
                            "What is your target percentage score for admission tests?",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { targetInput = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Target % (e.g. 80)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("target_percentage_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = targetInput.toFloatOrNull() ?: 80f
                        viewModel.setMockTestTargetScorePercentage(num)
                        showTargetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier.testTag("save_target_percentage_button")
                ) {
                    Text(if (lang == AppLanguage.BANGLA) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text(if (lang == AppLanguage.BANGLA) "বাতিল" else "Cancel", color = textSecondary)
                }
            }
        )
    }
}

@Composable
fun FilterPill(
    label: String,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) {
        RoyalBlue
    } else {
        if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    }
    val fg = if (isSelected) Color.White else (if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B))

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun MockTestEditDialog(
    isDarkTheme: Boolean,
    lang: AppLanguage,
    subjects: List<SubjectEntity>,
    existingTest: MockTestEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        provider: String,
        subjectId: Long?,
        date: Long,
        total: Double,
        obtained: Double,
        neg: Double,
        duration: Int?
    ) -> Unit
) {
    var testName by remember { mutableStateOf(existingTest?.testName ?: "") }
    var examProvider by remember { mutableStateOf(existingTest?.examProvider ?: "") }
    var selectedSubjectId by remember { mutableStateOf(existingTest?.subjectId) }
    var totalMarksStr by remember { mutableStateOf(existingTest?.totalMarks?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "100") }
    var obtainedMarksStr by remember { mutableStateOf(existingTest?.obtainedMarks?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var negativeMarksStr by remember { mutableStateOf(existingTest?.negativeMarks?.let { if (it == 0.0) "" else it.toString() } ?: "") }
    var durationStr by remember { mutableStateOf(existingTest?.durationMinutes?.toString() ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var subjectExpanded by remember { mutableStateOf(false) }

    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
        title = {
            Text(
                text = if (existingTest == null) {
                    if (lang == AppLanguage.BANGLA) "নতুন মক টেস্ট যোগ করুন" else "Record Mock Test"
                } else {
                    if (lang == AppLanguage.BANGLA) "মক টেস্ট সম্পাদনা" else "Edit Mock Test"
                },
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    Surface(
                        color = AlertRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = AlertRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = testName,
                    onValueChange = { testName = it },
                    label = { Text(if (lang == AppLanguage.BANGLA) "টেস্টের নাম" else "Test Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("test_name_input")
                )

                OutlinedTextField(
                    value = examProvider,
                    onValueChange = { examProvider = it },
                    label = { Text(if (lang == AppLanguage.BANGLA) "কোচিং / প্রোভাইডার (ঐচ্ছিক)" else "Provider (e.g. Udvash, Retina)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("exam_provider_input")
                )

                // Subject Selector
                val currentSubName = remember(selectedSubjectId, subjects) {
                    if (selectedSubjectId == null) {
                        if (lang == AppLanguage.BANGLA) "পূর্ণাঙ্গ মডেল টেস্ট (সব বিষয়)" else "Full Model Test (All Subjects)"
                    } else {
                        val s = subjects.find { it.id == selectedSubjectId }
                        if (lang == AppLanguage.BANGLA) s?.nameBn ?: "বিষয়" else s?.nameEn ?: "Subject"
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentSubName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (lang == AppLanguage.BANGLA) "বিষয়" else "Subject") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { subjectExpanded = true }
                            .testTag("select_subject_field"),
                        trailingIcon = {
                            TextButton(onClick = { subjectExpanded = true }) {
                                Text(if (lang == AppLanguage.BANGLA) "নির্বাচন" else "Select")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (lang == AppLanguage.BANGLA) "পূর্ণাঙ্গ মডেল টেস্ট" else "Full Model Test") },
                            onClick = {
                                selectedSubjectId = null
                                subjectExpanded = false
                            }
                        )
                        subjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(if (lang == AppLanguage.BANGLA) sub.nameBn else sub.nameEn) },
                                onClick = {
                                    selectedSubjectId = sub.id
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalMarksStr,
                        onValueChange = { totalMarksStr = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "মোট নম্বর" else "Total Marks") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("total_marks_input")
                    )

                    OutlinedTextField(
                        value = obtainedMarksStr,
                        onValueChange = { obtainedMarksStr = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "প্রাপ্ত নম্বর" else "Obtained") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("obtained_marks_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = negativeMarksStr,
                        onValueChange = { negativeMarksStr = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "নেগেটিভ মার্কস" else "Negative (opt)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("negative_marks_input")
                    )

                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { durationStr = it },
                        label = { Text(if (lang == AppLanguage.BANGLA) "সময় (মিনিট)" else "Mins (opt)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("duration_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (testName.isBlank()) {
                        errorMessage = if (lang == AppLanguage.BANGLA) "টেস্টের নাম দিন" else "Please enter test name"
                        return@Button
                    }
                    val total = totalMarksStr.toDoubleOrNull()
                    if (total == null || total <= 0.0) {
                        errorMessage = if (lang == AppLanguage.BANGLA) "সঠিক মোট নম্বর দিন" else "Total marks must be > 0"
                        return@Button
                    }
                    val obtained = obtainedMarksStr.toDoubleOrNull()
                    if (obtained == null) {
                        errorMessage = if (lang == AppLanguage.BANGLA) "প্রাপ্ত নম্বর দিন" else "Please enter obtained marks"
                        return@Button
                    }
                    if (obtained > total) {
                        errorMessage = if (lang == AppLanguage.BANGLA) "প্রাপ্ত নম্বর মোট নম্বরের চেয়ে বেশি হতে পারে না" else "Obtained marks cannot exceed total marks"
                        return@Button
                    }
                    if (obtained < 0.0) {
                        errorMessage = if (lang == AppLanguage.BANGLA) "প্রাপ্ত নম্বর ০ এর কম হতে পারে না" else "Obtained marks cannot be negative"
                        return@Button
                    }
                    val neg = negativeMarksStr.toDoubleOrNull() ?: 0.0
                    if (neg < 0.0) {
                        errorMessage = if (lang == AppLanguage.BANGLA) "নেগেটিভ মার্কস ঋণাত্মক হতে পারে না" else "Negative marks cannot be negative"
                        return@Button
                    }
                    val dur = durationStr.toIntOrNull()

                    val date = existingTest?.testDate ?: System.currentTimeMillis()

                    onConfirm(
                        testName.trim(),
                        examProvider.trim().ifBlank { "Self" },
                        selectedSubjectId,
                        date,
                        total,
                        obtained,
                        neg,
                        dur
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                modifier = Modifier.testTag("save_mock_test_button")
            ) {
                Text(if (lang == AppLanguage.BANGLA) "সংরক্ষণ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (lang == AppLanguage.BANGLA) "বাতিল" else "Cancel", color = textSecondary)
            }
        }
    )
}
