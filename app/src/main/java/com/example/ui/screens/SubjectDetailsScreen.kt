package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun SubjectDetailsScreen(
    viewModel: MainViewModel,
    subjectId: Long,
    subjects: List<SubjectItem>,
    chapters: List<ChapterItem>,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: () -> Unit
) {
    val subject = subjects.find { it.id == subjectId } ?: subjects.first()
    val subChapters = chapters.filter { it.subjectId == subject.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subChapters) { ch ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = ch.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                        Checkbox(checked = ch.isCompleted, onCheckedChange = {})
                    }
                }
            }
        }
    }
}
