package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectEntity
import com.example.ui.theme.NavyText
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateMuted
import com.example.util.AppLanguage

@Composable
fun AddChapterDialog(
    subjects: List<SubjectEntity>,
    preselectedSubjectId: Long,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (subjectId: Long, titleEn: String, titleBn: String, priority: String, mins: Int) -> Unit
) {
    var subjectId by remember { mutableLongStateOf(preselectedSubjectId) }
    var titleEn by remember { mutableStateOf("") }
    var titleBn by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("HIGH") }
    var estimatedMins by remember { mutableIntStateOf(60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Chapter",
                fontWeight = FontWeight.Bold,
                color = NavyText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titleEn,
                    onValueChange = { titleEn = it },
                    label = { Text("Chapter Title (e.g. Modern Physics)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_chapter_title")
                )

                OutlinedTextField(
                    value = titleBn,
                    onValueChange = { titleBn = it },
                    label = { Text("অধ্যায়ের নাম (বাংলা)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleEn.isNotBlank() || titleBn.isNotBlank()) {
                        val en = titleEn.ifBlank { titleBn }
                        val bn = titleBn.ifBlank { titleEn }
                        onConfirm(subjectId, en, bn, priority, estimatedMins)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                modifier = Modifier.testTag("button_save_chapter")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddSubjectDialog(
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (nameEn: String, nameBn: String, colorHex: String) -> Unit
) {
    var nameEn by remember { mutableStateOf("") }
    var nameBn by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#6366F1") }

    val colors = listOf("#6366F1", "#10B981", "#8B5CF6", "#EF4444", "#2563EB", "#F59E0B")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Subject",
                fontWeight = FontWeight.Bold,
                color = NavyText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Subject Name (e.g. ICT)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_subject_name")
                )

                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("বিষয়ের নাম (বাংলা)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameEn.isNotBlank() || nameBn.isNotBlank()) {
                        val en = nameEn.ifBlank { nameBn }
                        val bn = nameBn.ifBlank { nameEn }
                        onConfirm(en, bn, selectedColor)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                modifier = Modifier.testTag("button_save_subject")
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddAdmissionDialog(
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (uni: String, unit: String, deadlineDays: Int, examDays: Int, notes: String) -> Unit
) {
    var university by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var deadlineDays by remember { mutableIntStateOf(15) }
    var examDays by remember { mutableIntStateOf(45) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Admission Target",
                fontWeight = FontWeight.Bold,
                color = NavyText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = university,
                    onValueChange = { university = it },
                    label = { Text("University (e.g. Jahangirnagar)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_uni_name")
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit / Faculty (e.g. D Unit)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = examDays.toString(),
                        onValueChange = { examDays = it.toIntOrNull() ?: 45 },
                        label = { Text("Days to Exam") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (university.isNotBlank()) {
                        onConfirm(university, unit, deadlineDays, examDays, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                modifier = Modifier.testTag("button_save_admission")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
