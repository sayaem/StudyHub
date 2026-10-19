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
import com.example.ui.ChapterItem
import com.example.ui.MainViewModel
import com.example.ui.SubjectItem
import com.example.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    viewModel: MainViewModel,
    subjects: List<SubjectItem>,
    chapters: List<ChapterItem>,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onOpenSubject: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lang == AppLanguage.BANGLA) "সিলেবাস" else "Syllabus") },
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
            items(subjects) { subject ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSubject(subject.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = subject.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { subject.progress }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${(subject.progress * 100).toInt()}% completed", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
