package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectEntity
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedLight
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkMidnightSurface
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextWhite
import com.example.ui.theme.NavyText
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateMuted
import com.example.util.AppLanguage
import com.example.util.AppStrings

// Restrained Apple-inspired palette for subjects
val SubjectColorPalette = listOf(
    "#2563EB", // Royal Blue
    "#10B981", // Emerald Green
    "#8B5CF6", // Violet / Purple
    "#F59E0B", // Amber
    "#EF4444", // Coral Red
    "#0D9488", // Teal
    "#6366F1", // Indigo
    "#EC4899"  // Rose
)

val SubjectIconsList = listOf(
    Icons.Default.Science to "Science",
    Icons.Default.Calculate to "Math",
    Icons.Default.MenuBook to "Literature",
    Icons.Default.Computer to "ICT",
    Icons.Default.Eco to "Biology",
    Icons.Default.Public to "General Knowledge",
    Icons.Default.Psychology to "Critical Thinking",
    Icons.Default.Book to "Textbook"
)

/**
 * Polished, minimal, Apple-inspired Bottom Sheet for creating a new Subject
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectBottomSheet(
    existingSubjects: List<SubjectEntity>,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (nameEn: String, nameBn: String, colorHex: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var nameEn by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(SubjectColorPalette[0]) }
    var selectedIconIndex by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val bg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted
    val borderColor = if (isDarkTheme) DarkBorder else SlateBorder

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(
                        color = if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .testTag("add_subject_sheet"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = AppStrings.createSubjectTitle(lang),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = AppStrings.createSubjectSubtitle(lang),
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                }
            }

            // Live Preview Card (Apple-style preview)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isDarkTheme) Color(0xFF131D31) else Color(0xFFF8FAFC),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = AppStrings.previewLabel(lang),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val parsedColor = try {
                        Color(android.graphics.Color.parseColor(selectedColor))
                    } catch (e: Exception) {
                        RoyalBlue
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(parsedColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = SubjectIconsList[selectedIconIndex].first,
                            contentDescription = null,
                            tint = parsedColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (nameEn.isNotBlank()) nameEn else if (lang == AppLanguage.BANGLA) "বিষয়ের নাম" else "Subject Name",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        if (nameBn.isNotBlank()) {
                            Text(
                                text = nameBn,
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "০ / ০ অধ্যায়" else "0 / 0 chapters",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            // Subject Name (English)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = AppStrings.subjectNameLabel(lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = {
                        nameEn = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "যেমনঃ পদার্থবিজ্ঞান বা ICT" else "e.g. Physics, Higher Math",
                            color = textSecondary
                        )
                    },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_subject_name"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )
            }

            // Subject Name (Bangla - Optional)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = AppStrings.subjectNameBnLabel(lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    placeholder = {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "যেমনঃ পদার্থবিজ্ঞান ১ম পত্র" else "Optional Bangla label",
                            color = textSecondary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_subject_name_bn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )
            }

            // Color Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppStrings.subjectColorLabel(lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(SubjectColorPalette) { colorHex ->
                        val itemColor = try {
                            Color(android.graphics.Color.parseColor(colorHex))
                        } catch (e: Exception) {
                            RoyalBlue
                        }
                        val isSelected = selectedColor == colorHex

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(itemColor)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) (if (isDarkTheme) Color.White else NavyText) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Icon Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppStrings.subjectIconLabel(lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(SubjectIconsList.indices.toList()) { index ->
                        val (icon, _) = SubjectIconsList[index]
                        val isSelected = selectedIconIndex == index
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) RoyalBlue else (if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                )
                                .clickable { selectedIconIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Localized Error message
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { err ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AlertRedLight, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = err,
                            fontSize = 12.sp,
                            color = AlertRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons: Create Subject & Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("button_cancel_subject"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Text(
                        text = AppStrings.cancelAction(lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        val trimmedEn = nameEn.trim()
                        val trimmedBn = nameBn.trim()
                        if (trimmedEn.isBlank() && trimmedBn.isBlank()) {
                            errorMessage = AppStrings.errEmptySubjectName(lang)
                            return@Button
                        }

                        // Duplicate validation
                        val finalEn = trimmedEn.ifBlank { trimmedBn }
                        val finalBn = trimmedBn.ifBlank { trimmedEn }

                        val isDuplicate = existingSubjects.any {
                            it.nameEn.equals(finalEn, ignoreCase = true) ||
                            (it.nameBn.isNotBlank() && it.nameBn.equals(finalBn, ignoreCase = true))
                        }

                        if (isDuplicate) {
                            errorMessage = AppStrings.errDuplicateSubject(lang)
                            return@Button
                        }

                        isSubmitting = true
                        onConfirm(finalEn, finalBn, selectedColor)
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("button_save_subject"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue
                    )
                ) {
                    Text(
                        text = AppStrings.createSubjectButton(lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Polished, minimal, Apple-inspired Bottom Sheet for creating a new Chapter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChapterBottomSheet(
    subjects: List<SubjectEntity>,
    preselectedSubjectId: Long?,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (subjectId: Long, titleEn: String, titleBn: String, priority: String, mins: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedSubjectId by remember {
        mutableLongStateOf(
            preselectedSubjectId ?: subjects.firstOrNull()?.id ?: 0L
        )
    }
    var titleEn by remember { mutableStateOf("") }
    var titleBn by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("HIGH") }
    var selectedHours by remember { mutableIntStateOf(2) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val bg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted
    val borderColor = if (isDarkTheme) DarkBorder else SlateBorder

    val isSubjectLocked = preselectedSubjectId != null && preselectedSubjectId != 0L
    val targetSubject = subjects.find { it.id == selectedSubjectId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(
                        color = if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .testTag("add_chapter_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = AppStrings.createChapterTitle(lang),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (targetSubject != null) {
                        val subName = if (lang == AppLanguage.BANGLA && targetSubject.nameBn.isNotBlank())
                            targetSubject.nameBn else targetSubject.nameEn
                        Text(
                            text = "${AppStrings.selectSubjectLabel(lang)}: $subName",
                            fontSize = 13.sp,
                            color = RoyalBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Subject Selector (if not locked by being inside Subject Details)
            if (!isSubjectLocked && subjects.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = AppStrings.selectSubjectLabel(lang),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(subjects, key = { it.id }) { s ->
                            val isSelected = s.id == selectedSubjectId
                            val sName = if (lang == AppLanguage.BANGLA && s.nameBn.isNotBlank()) s.nameBn else s.nameEn
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) RoyalBlue else (if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                    )
                                    .clickable { selectedSubjectId = s.id }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = sName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Chapter Title
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = AppStrings.chapterTitleLabel(lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                OutlinedTextField(
                    value = titleEn,
                    onValueChange = {
                        titleEn = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "যেমনঃ ভেক্টর ও বলবিদ্যা" else "e.g. Thermodynamics, Calculus",
                            color = textSecondary
                        )
                    },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_chapter_title"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )
            }

            // Chapter Title (Bangla - Optional)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = AppStrings.chapterTitleBnLabel(lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                OutlinedTextField(
                    value = titleBn,
                    onValueChange = { titleBn = it },
                    placeholder = {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "ঐচ্ছিক বাংলা নাম" else "Optional Bangla name",
                            color = textSecondary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_chapter_title_bn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )
            }

            // Target Hours Chips (1h, 2h, 3h, 4h, 6h)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppStrings.targetHoursLabel(lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3, 4, 6).forEach { hours ->
                        val isSelected = selectedHours == hours
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) RoyalBlue else (if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                )
                                .clickable { selectedHours = hours }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val hText = if (lang == AppLanguage.BANGLA)
                                "${AppStrings.toBengaliDigits(hours.toString())}ঘ"
                            else
                                "${hours}h"
                            Text(
                                text = hText,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else textPrimary
                            )
                        }
                    }
                }
            }

            // Priority Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppStrings.priorityLabel(lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val priorities = listOf(
                        "HIGH" to AppStrings.priorityHigh(lang),
                        "MEDIUM" to AppStrings.priorityMedium(lang),
                        "LOW" to AppStrings.priorityLow(lang)
                    )
                    priorities.forEach { (key, label) ->
                        val isSelected = selectedPriority == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) RoyalBlue else (if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                )
                                .clickable { selectedPriority = key }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else textPrimary
                            )
                        }
                    }
                }
            }

            // Localized Error
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { err ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AlertRedLight, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = err,
                            fontSize = 12.sp,
                            color = AlertRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("button_cancel_chapter"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Text(
                        text = AppStrings.cancelAction(lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        val trimmedEn = titleEn.trim()
                        val trimmedBn = titleBn.trim()
                        if (trimmedEn.isBlank() && trimmedBn.isBlank()) {
                            errorMessage = AppStrings.errEmptyChapterTitle(lang)
                            return@Button
                        }
                        if (selectedSubjectId == 0L) {
                            errorMessage = AppStrings.errSelectSubject(lang)
                            return@Button
                        }

                        val finalEn = trimmedEn.ifBlank { trimmedBn }
                        val finalBn = trimmedBn.ifBlank { trimmedEn }

                        isSubmitting = true
                        onConfirm(
                            selectedSubjectId,
                            finalEn,
                            finalBn,
                            selectedPriority,
                            selectedHours * 60
                        )
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("button_save_chapter"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue
                    )
                ) {
                    Text(
                        text = AppStrings.createChapterButton(lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Clean backward-compatible wrappers if invoked as Dialog
 */
@Composable
fun AddSubjectDialog(
    existingSubjects: List<SubjectEntity> = emptyList(),
    isDarkTheme: Boolean = false,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (nameEn: String, nameBn: String, colorHex: String) -> Unit
) {
    AddSubjectBottomSheet(
        existingSubjects = existingSubjects,
        isDarkTheme = isDarkTheme,
        lang = lang,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun AddChapterDialog(
    subjects: List<SubjectEntity>,
    preselectedSubjectId: Long,
    isDarkTheme: Boolean = false,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (subjectId: Long, titleEn: String, titleBn: String, priority: String, mins: Int) -> Unit
) {
    AddChapterBottomSheet(
        subjects = subjects,
        preselectedSubjectId = if (preselectedSubjectId != 0L) preselectedSubjectId else null,
        isDarkTheme = isDarkTheme,
        lang = lang,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

/**
 * Polished dialog for adding/editing target admission university
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAdmissionBottomSheet(
    isDarkTheme: Boolean,
    lang: AppLanguage,
    initialUni: String = "",
    initialUnit: String = "",
    initialDeadlineDays: Int = 15,
    initialExamDays: Int = 45,
    initialNotes: String = "",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (uni: String, unit: String, deadlineDays: Int, examDays: Int, notes: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var university by remember { mutableStateOf(initialUni) }
    var unit by remember { mutableStateOf(initialUnit) }
    var deadlineDays by remember { mutableIntStateOf(initialDeadlineDays) }
    var examDays by remember { mutableIntStateOf(initialExamDays) }
    var notes by remember { mutableStateOf(initialNotes) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val bg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText
    val textSecondary = if (isDarkTheme) DarkTextMuted else SlateMuted
    val borderColor = if (isDarkTheme) DarkBorder else SlateBorder

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(
                        color = if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .testTag("admission_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditing) AppStrings.editTargetUni(lang) else AppStrings.addTargetUni(lang),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            // University Name
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "বিশ্ববিদ্যালয়ের নাম" else "University Name",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                OutlinedTextField(
                    value = university,
                    onValueChange = {
                        university = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "যেমনঃ ঢাকা বিশ্ববিদ্যালয়, বুয়েট" else "e.g. Dhaka University, BUET",
                            color = textSecondary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_uni_name"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )
            }

            // Unit / Faculty
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "ইউনিট / অনুষদ" else "Unit / Faculty",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    placeholder = {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "যেমনঃ 'ক' ইউনিট (বিজ্ঞান)" else "e.g. 'A' Unit (Science)",
                            color = textSecondary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_unit_name"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )
            }

            // Deadlines
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "আবেদনের বাকি দিন" else "App Deadline (days)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    OutlinedTextField(
                        value = deadlineDays.toString(),
                        onValueChange = { deadlineDays = it.toIntOrNull() ?: 0 },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (lang == AppLanguage.BANGLA) "পরীক্ষার বাকি দিন" else "Exam In (days)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )
                    OutlinedTextField(
                        value = examDays.toString(),
                        onValueChange = { examDays = it.toIntOrNull() ?: 0 },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = borderColor,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                }
            }

            // Notes / Requirements
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "প্রয়োজনীয় নোট (ঐচ্ছিক)" else "Notes / Requirements (Optional)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "যেমনঃ জিপিএ ন্যূনতম ৮.৫" else "e.g. Min GPA 8.5, HSC Math required",
                            color = textSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )
            }

            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { err ->
                    Text(
                        text = err,
                        fontSize = 12.sp,
                        color = AlertRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Text(AppStrings.cancelAction(lang), fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (university.isBlank()) {
                            errorMessage = AppStrings.errEmptyUniversity(lang)
                            return@Button
                        }
                        onConfirm(university.trim(), unit.trim(), deadlineDays, examDays, notes.trim())
                    },
                    modifier = Modifier.weight(1.5f).height(48.dp).testTag("button_save_admission"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(
                        if (isEditing) AppStrings.saveChanges(lang) else AppStrings.addTargetUni(lang),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AddAdmissionDialog(
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (uni: String, unit: String, deadlineDays: Int, examDays: Int, notes: String) -> Unit
) {
    AddAdmissionBottomSheet(
        isDarkTheme = false,
        lang = lang,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}
