package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.StudyHubCard
import com.example.util.AppLanguage

@Composable
fun MistakeBookScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mistakes by viewModel.mistakes.collectAsStateWithLifecycle()
    val bg = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(modifier = modifier.fillMaxSize().background(bg).testTag("mistake_book_screen")) {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textPrimary)
                    }
                    Text(if (lang == AppLanguage.BANGLA) "ভুলসমূহ" else "Mistake Book", style = MaterialTheme.typography.titleLarge, color = textPrimary)
                }
            }
            items(mistakes) { mistake ->
                StudyHubCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(mistake.questionDescription, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text(mistake.mistakeDescription, color = textSecondary)
                    }
                }
            }
        }
    }
}
