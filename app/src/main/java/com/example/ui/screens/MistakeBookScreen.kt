package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChapterEntity
import com.example.data.model.MistakeEntity
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
fun MistakeBookScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val mistakes by viewModel.mistakes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "UNRESOLVED", "RESOLVED"
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var mistakeToEdit by remember { mutableStateOf<MistakeEntity?>(null) }
    var mistakeToDelete by remember { mutableStateOf<MistakeEntity?>(null) }
    var mistakeToReview by remember { mutableStateOf<MistakeEntity?>(null) }

    val cardBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else MaterialTheme.colorScheme.outlineVariant
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    val subjectsMap = remember(subjects) { subjects.associateBy { it.id } }
    val chaptersMap = remember(chapters) { chapters.associateBy { it.id } }

    val categories = listOf(
        "Conceptual Gap" to ("ধারণাগত ঘাটতি"),
        "Calculation Error" to ("গণনার ভুল"),
        "Careless Mistake" to ("অসাবধানতাবশত ভুল"),
        "Memory Issue" to ("স্মৃতিবিভ্রান্তি"),
        "Time Management" to ("সময় ব্যবস্থাপনা")
    )

    // Filter mistakes
    val filteredMistakes = remember(
        mistakes,
        searchQuery,
        selectedStatusFilter,
        selectedSubjectId,
        selectedCategoryFilter
    ) {
        mistakes.filter { m ->
            val matchStatus = when (selectedStatusFilter) {
                "UNRESOLVED" -> m.status != "RESOLVED"
                "RESOLVED" -> m.status == "RESOLVED"
                else -> true
            }
            val matchSubject = selectedSubjectId == null || m.subjectId == selectedSubjectId
            val matchCategory = selectedCategoryFilter == null || m.mistakeCategory.equals(selectedCategoryFilter, ignoreCase = true)
            val matchQuery = searchQuery.isBlank() ||
                m.questionDescription.contains(searchQuery, ignoreCase = true) ||
                m.mistakeDescription.contains(searchQuery, ignoreCase = true) ||
                m.correctApproach.contains(searchQuery, ignoreCase = true)

            matchStatus && matchSubject && matchCategory && matchQuery
        }.sortedByDescending { it.dateRecorded }
    }

    val totalCount = mistakes.size
    val unresolvedCount = mistakes.count { it.status != "RESOLVED" }
    val resolvedCount = mistakes.count { it.status == "RESOLVED" }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .testTag("mistake_book_screen")
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
                        modifier = Modifier.size(36.dp).testTag("mistake_book_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "মিস্টেক বুক ও দুর্বল টপিক" else "Mistake Book & Weak Topics",
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
                        "মক টেস্ট ও অনুশীলনের ভুলসমূহ লিপিবদ্ধ করুন এবং ভুল থেকে শিখুন"
                    else
                        "Document test mistakes, study correct concepts, and master weak topics",
                    fontSize = 12.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Summary Stats Card
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "মোট রেকর্ডকৃত ভুল" else "Total Mistakes",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = AppStrings.formatNumber(totalCount, lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = textPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDarkTheme) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFFFEF3C7)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "অমীমাংসিত" else "Unresolved",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFD97706)
                                    )
                                    Text(
                                        text = AppStrings.formatNumber(unresolvedCount, lang),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDarkTheme) MintGreen.copy(alpha = 0.15f) else MintGreenLight
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "সমাধানকৃত" else "Resolved",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MintGreen
                                    )
                                    Text(
                                        text = AppStrings.formatNumber(resolvedCount, lang),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MintGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search TextField
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (lang == AppLanguage.BANGLA) "প্রশ্ন বা কারণ খুঁজুন..." else "Search question or concept...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = borderCol,
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mistake_search_input")
                )
            }

            // Filter Pills: Status & Subjects
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterPill(
                            label = if (lang == AppLanguage.BANGLA) "সব ভুল" else "All",
                            isSelected = selectedStatusFilter == "ALL",
                            isDarkTheme = isDarkTheme,
                            onClick = { selectedStatusFilter = "ALL" }
                        )
                        FilterPill(
                            label = if (lang == AppLanguage.BANGLA) "অমীমাংসিত (${AppStrings.formatNumber(unresolvedCount, lang)})" else "Unresolved ($unresolvedCount)",
                            isSelected = selectedStatusFilter == "UNRESOLVED",
                            isDarkTheme = isDarkTheme,
                            onClick = { selectedStatusFilter = "UNRESOLVED" }
                        )
                        FilterPill(
                            label = if (lang == AppLanguage.BANGLA) "সমাধানকৃত (${AppStrings.formatNumber(resolvedCount, lang)})" else "Resolved ($resolvedCount)",
                            isSelected = selectedStatusFilter == "RESOLVED",
                            isDarkTheme = isDarkTheme,
                            onClick = { selectedStatusFilter = "RESOLVED" }
                        )
                    }

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
                }
            }

            // Mistake Cards List
            if (filteredMistakes.isEmpty()) {
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
                                    .background(MintGreenLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MintGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "কোনো ভুল পাওয়া যায়নি" else "No Mistakes Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA)
                                    "মক টেস্টে কোনো ভুল হলে তা এখানে সংরক্ষণ করুন।"
                                else
                                    "When you encounter errors in mock tests, record them here to review the concepts.",
                                fontSize = 13.sp,
                                color = textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                modifier = Modifier.testTag("record_first_mistake_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == AppLanguage.BANGLA) "ভুল যোগ করুন" else "Record Mistake")
                            }
                        }
                    }
                }
            } else {
                items(filteredMistakes, key = { it.id }) { mistake ->
                    val sub = mistake.subjectId?.let { subjectsMap[it] }
                    val chap = mistake.chapterId?.let { chaptersMap[it] }

                    val subName = if (lang == AppLanguage.BANGLA) sub?.nameBn ?: "বিষয়" else sub?.nameEn ?: "Subject"
                    val chapName = if (lang == AppLanguage.BANGLA) chap?.titleBn ?: chap?.titleEn ?: "" else chap?.titleEn ?: ""
                    val subColor = sub?.let {
                        try { Color(android.graphics.Color.parseColor(it.colorHex)) } catch (e: Exception) { RoyalBlue }
                    } ?: RoyalBlue

                    val categoryColor = when (mistake.mistakeCategory) {
                        "Conceptual Gap", "Conceptual" -> RoyalBlue
                        "Calculation Error", "Calculation" -> AlertRed
                        "Careless Mistake", "Careless" -> Color(0xFFF59E0B)
                        "Memory Issue", "Memory" -> Color(0xFF8B5CF6)
                        "Time Management" -> Color(0xFF0EA5E9)
                        else -> RoyalBlue
                    }

                    val dateFormatted = remember(mistake.dateRecorded) {
                        SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(mistake.dateRecorded))
                    }

                    var isExpanded by remember { mutableStateOf(false) }

                    StudyHubCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mistake_item_${mistake.id}"),
                        backgroundColor = cardBg,
                        borderColor = borderCol
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Category Pill + Status Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = categoryColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = mistake.mistakeCategory,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = categoryColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (mistake.status) {
                                        "RESOLVED" -> MintGreen.copy(alpha = 0.15f)
                                        "REVIEWED" -> RoyalBlue.copy(alpha = 0.15f)
                                        else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = when (mistake.status) {
                                            "RESOLVED" -> if (lang == AppLanguage.BANGLA) "সমাধানকৃত" else "RESOLVED"
                                            "REVIEWED" -> if (lang == AppLanguage.BANGLA) "পর্যালোচিত" else "REVIEWED"
                                            else -> if (lang == AppLanguage.BANGLA) "নতুন ভুল" else "NEW"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (mistake.status) {
                                            "RESOLVED" -> MintGreen
                                            "REVIEWED" -> RoyalBlue
                                            else -> Color(0xFFF59E0B)
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Question Title
                            Text(
                                text = mistake.questionDescription,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textPrimary
                            )

                            // Subject & Chapter Tag
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(subColor, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (chapName.isNotBlank()) "$subName • $chapName" else subName,
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• $dateFormatted",
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }

                            // Always visible or expanded details
                            Spacer(modifier = Modifier.height(10.dp))

                            // Mistake description box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isDarkTheme) AlertRed.copy(alpha = 0.1f) else Color(0xFFFEF2F2),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "আমার ভুল:" else "My Mistake:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AlertRed
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = mistake.mistakeDescription,
                                        fontSize = 12.sp,
                                        color = textPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Correct approach box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isDarkTheme) MintGreen.copy(alpha = 0.1f) else Color(0xFFF0FDF4),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == AppLanguage.BANGLA) "সঠিক নিয়ম ও সমাধান:" else "Correct Approach:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MintGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = mistake.correctApproach,
                                        fontSize = 12.sp,
                                        color = textPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Actions Row: Review Button, Edit, Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { mistakeToReview = mistake },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (mistake.status == "RESOLVED") MintGreen else RoyalBlue
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("review_mistake_button_${mistake.id}")
                                ) {
                                    Icon(
                                        imageVector = if (mistake.status == "RESOLVED") Icons.Default.Check else Icons.Default.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (mistake.status == "RESOLVED") {
                                            if (lang == AppLanguage.BANGLA) "পুনরায় দেখুন" else "Review Again"
                                        } else {
                                            if (lang == AppLanguage.BANGLA) "পর্যালোচনা করুন" else "Review Concept"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { mistakeToEdit = mistake },
                                        modifier = Modifier.size(32.dp).testTag("edit_mistake_${mistake.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { mistakeToDelete = mistake },
                                        modifier = Modifier.size(32.dp).testTag("delete_mistake_${mistake.id}")
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
                .testTag("fab_add_mistake"),
            containerColor = RoyalBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Mistake")
        }
    }

    // Add Mistake Dialog
    if (showAddDialog) {
        MistakeEditDialog(
            isDarkTheme = isDarkTheme,
            lang = lang,
            subjects = subjects,
            chapters = chapters,
            existingMistake = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { subId, chapId, quest, mist, corr, cat ->
                viewModel.addMistake(subId, chapId, quest, mist, corr, cat)
                showAddDialog = false
            }
        )
    }

    // Edit Mistake Dialog
    if (mistakeToEdit != null) {
        MistakeEditDialog(
            isDarkTheme = isDarkTheme,
            lang = lang,
            subjects = subjects,
            chapters = chapters,
            existingMistake = mistakeToEdit,
            onDismiss = { mistakeToEdit = null },
            onConfirm = { subId, chapId, quest, mist, corr, cat ->
                viewModel.updateMistake(
                    mistakeToEdit!!.copy(
                        subjectId = subId,
                        chapterId = chapId,
                        questionDescription = quest,
                        mistakeDescription = mist,
                        correctApproach = corr,
                        mistakeCategory = cat
                    )
                )
                mistakeToEdit = null
            }
        )
    }

    // Interactive Review Workflow Dialog
    if (mistakeToReview != null) {
        val m = mistakeToReview!!
        var markResolved by remember { mutableStateOf(m.status == "RESOLVED") }
        var addToSpacedRevision by remember { mutableStateOf(!markResolved) }

        AlertDialog(
            onDismissRequest = { mistakeToReview = null },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "ভুল পর্যালোচনা ও শিক্ষা" else "Review Mistake Concept",
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
                        text = m.questionDescription,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )

                    Surface(
                        color = if (isDarkTheme) MintGreen.copy(alpha = 0.1f) else MintGreenLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "সঠিক ধারণা ও সমাধান:" else "Correct Solution & Concept:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = m.correctApproach,
                                fontSize = 13.sp,
                                color = textPrimary
                            )
                        }
                    }

                    Divider(color = borderCol)

                    Text(
                        text = if (lang == AppLanguage.BANGLA) "পর্যালোচনার ফলাফল নির্বাচন করুন:" else "Choose Review Outcome:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = markResolved,
                            onClick = {
                                markResolved = true
                                addToSpacedRevision = false
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "সমাধানকৃত (ধারণা পরিষ্কার হয়েছে)" else "Resolved (Concept Mastered)",
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !markResolved,
                            onClick = {
                                markResolved = false
                                addToSpacedRevision = true
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "আরও রিভিশন প্রয়োজন" else "Needs Another Review",
                            fontSize = 13.sp,
                            color = textPrimary
                        )
                    }

                    if (!markResolved) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = addToSpacedRevision,
                                onCheckedChange = { addToSpacedRevision = it }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (lang == AppLanguage.BANGLA) "স্পেসড রিভিশন তালিকায় যোগ করুন" else "Add to Spaced Revision Schedule",
                                fontSize = 12.sp,
                                color = RoyalBlue
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reviewMistake(m, markResolved, addToSpacedRevision)
                        mistakeToReview = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier.testTag("confirm_review_button")
                ) {
                    Text(if (lang == AppLanguage.BANGLA) "সম্পন্ন করুন" else "Confirm Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { mistakeToReview = null }) {
                    Text(if (lang == AppLanguage.BANGLA) "বাতিল" else "Cancel", color = textSecondary)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (mistakeToDelete != null) {
        val mistake = mistakeToDelete!!
        AlertDialog(
            onDismissRequest = { mistakeToDelete = null },
            containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
            title = {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "ভুল রেকর্ড মুছবেন?" else "Delete Mistake?",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = if (lang == AppLanguage.BANGLA)
                        "আপনি কি এই ভুল রেকর্ডটি স্থায়ীভাবে মুছে ফেলতে চান?"
                    else
                        "Are you sure you want to permanently delete this mistake record?",
                    color = textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMistake(mistake)
                        mistakeToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_mistake")
                ) {
                    Text(if (lang == AppLanguage.BANGLA) "মুছুন" else "Delete", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mistakeToDelete = null }) {
                    Text(if (lang == AppLanguage.BANGLA) "বাতিল" else "Cancel", color = textSecondary)
                }
            }
        )
    }
}

@Composable
fun MistakeEditDialog(
    isDarkTheme: Boolean,
    lang: AppLanguage,
    subjects: List<SubjectEntity>,
    chapters: List<ChapterEntity>,
    existingMistake: MistakeEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        subjectId: Long?,
        chapterId: Long?,
        question: String,
        mistake: String,
        correct: String,
        category: String
    ) -> Unit
) {
    var questionText by remember { mutableStateOf(existingMistake?.questionDescription ?: "") }
    var mistakeText by remember { mutableStateOf(existingMistake?.mistakeDescription ?: "") }
    var correctText by remember { mutableStateOf(existingMistake?.correctApproach ?: "") }
    var selectedCategory by remember { mutableStateOf(existingMistake?.mistakeCategory ?: "Conceptual Gap") }
    var selectedSubjectId by remember { mutableStateOf(existingMistake?.subjectId) }
    var selectedChapterId by remember { mutableStateOf(existingMistake?.chapterId) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var chapterExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted

    val availableCategories = listOf(
        "Conceptual Gap",
        "Calculation Error",
        "Careless Mistake",
        "Memory Issue",
        "Time Management"
    )

    // Filter chapters based on selected subject
    val availableChapters = remember(selectedSubjectId, chapters) {
        if (selectedSubjectId != null) {
            chapters.filter { it.subjectId == selectedSubjectId }
        } else chapters
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDarkTheme) DarkMidnightSurface else Color.White,
        title = {
            Text(
                text = if (existingMistake == null) {
                    if (lang == AppLanguage.BANGLA) "ভুল লিপিবদ্ধ করুন" else "Record Mistake"
                } else {
                    if (lang == AppLanguage.BANGLA) "ভুল সম্পাদনা করুন" else "Edit Mistake"
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
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text(if (lang == AppLanguage.BANGLA) "প্রশ্ন বা টপিক" else "Question / Problem") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("mistake_question_input")
                )

                OutlinedTextField(
                    value = mistakeText,
                    onValueChange = { mistakeText = it },
                    label = { Text(if (lang == AppLanguage.BANGLA) "কী ভুল হয়েছিল?" else "What went wrong?") },
                    modifier = Modifier.fillMaxWidth().testTag("mistake_desc_input")
                )

                OutlinedTextField(
                    value = correctText,
                    onValueChange = { correctText = it },
                    label = { Text(if (lang == AppLanguage.BANGLA) "সঠিক সমাধান ও সূত্র" else "Correct solution / formula") },
                    modifier = Modifier.fillMaxWidth().testTag("mistake_correct_input")
                )

                // Category Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (lang == AppLanguage.BANGLA) "ভুলের ধরন" else "Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true },
                        trailingIcon = {
                            TextButton(onClick = { categoryExpanded = true }) {
                                Text(if (lang == AppLanguage.BANGLA) "নির্বাচন" else "Select")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        availableCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Subject Picker
                val currentSub = subjects.find { it.id == selectedSubjectId }
                val currentSubName = if (lang == AppLanguage.BANGLA) currentSub?.nameBn ?: "বিষয় নির্বাচন করুন" else currentSub?.nameEn ?: "Select Subject"

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentSubName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (lang == AppLanguage.BANGLA) "বিষয় (ঐচ্ছিক)" else "Subject (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { subjectExpanded = true },
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
                            text = { Text(if (lang == AppLanguage.BANGLA) "কোনোটি নয়" else "None") },
                            onClick = {
                                selectedSubjectId = null
                                selectedChapterId = null
                                subjectExpanded = false
                            }
                        )
                        subjects.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(if (lang == AppLanguage.BANGLA) s.nameBn else s.nameEn) },
                                onClick = {
                                    selectedSubjectId = s.id
                                    selectedChapterId = null
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                // Chapter Picker (Filtered by Subject)
                if (availableChapters.isNotEmpty()) {
                    val currentChap = availableChapters.find { it.id == selectedChapterId }
                    val currentChapName = if (lang == AppLanguage.BANGLA) currentChap?.titleBn ?: currentChap?.titleEn ?: "অধ্যায় নির্বাচন করুন" else currentChap?.titleEn ?: "Select Chapter"

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = currentChapName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (lang == AppLanguage.BANGLA) "অধ্যায় (ঐচ্ছিক)" else "Chapter (Optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { chapterExpanded = true },
                            trailingIcon = {
                                TextButton(onClick = { chapterExpanded = true }) {
                                    Text(if (lang == AppLanguage.BANGLA) "নির্বাচন" else "Select")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = chapterExpanded,
                            onDismissRequest = { chapterExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (lang == AppLanguage.BANGLA) "কোনোটি নয়" else "None") },
                                onClick = {
                                    selectedChapterId = null
                                    chapterExpanded = false
                                }
                            )
                            availableChapters.forEach { ch ->
                                DropdownMenuItem(
                                    text = { Text(if (lang == AppLanguage.BANGLA) ch.titleBn.ifBlank { ch.titleEn } else ch.titleEn) },
                                    onClick = {
                                        selectedChapterId = ch.id
                                        chapterExpanded = false
                                    }
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
                    if (questionText.isBlank()) {
                        errorMessage = if (lang == AppLanguage.BANGLA) "প্রশ্ন বা টপিক উল্লেখ করুন" else "Please enter question/topic"
                        return@Button
                    }
                    if (mistakeText.isBlank()) {
                        errorMessage = if (lang == AppLanguage.BANGLA) "কী ভুল হয়েছিল লিখুন" else "Please describe the mistake"
                        return@Button
                    }
                    if (correctText.isBlank()) {
                        errorMessage = if (lang == AppLanguage.BANGLA) "সঠিক সমাধান লিখুন" else "Please write the correct solution"
                        return@Button
                    }

                    onConfirm(
                        selectedSubjectId,
                        selectedChapterId,
                        questionText.trim(),
                        mistakeText.trim(),
                        correctText.trim(),
                        selectedCategory
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                modifier = Modifier.testTag("save_mistake_button")
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
