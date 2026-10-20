package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ChapterEntity
import com.example.data.model.SubjectEntity
import com.example.ui.MainViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    viewModel: MainViewModel,
    subjects: List<SubjectEntity>,
    chapters: List<ChapterEntity>,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onOpenSubject: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppStrings.navSyllabus(lang), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(subjects, key = { it.id }) { subject ->
                val subChs = chapters.filter { it.subjectId == subject.id }
                val compChs = subChs.count { it.status == "COMPLETED" }
                val progress = if (subChs.isNotEmpty()) compChs.toFloat() / subChs.size else 0f
                val subjectName = if (lang == AppLanguage.BANGLA) subject.nameBn else subject.nameEn
                val progressPercent = (progress * 100).toInt()
                val progressText = if (lang == AppLanguage.BANGLA) {
                    "${AppStrings.toBengaliDigits(progressPercent.toString())}% সম্পন্ন (${AppStrings.toBengaliDigits(compChs.toString())}/${AppStrings.toBengaliDigits(subChs.size.toString())})"
                } else {
                    "$progressPercent% completed ($compChs/${subChs.size})"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSubject(subject.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = subjectName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
