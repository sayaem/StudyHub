package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.AppStrings

@Composable
fun AboutScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isDarkTheme) DarkMidnightBg else Color(0xFFF8FAFC)
    val textPrimary = if (isDarkTheme) DarkTextWhite else NavyText

    Box(modifier = modifier.fillMaxSize().background(bg).testTag("about_screen")) {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textPrimary)
                    }
                    Text(if (lang == AppLanguage.BANGLA) "অ্যাপ সম্পর্কিত" else "About StudyHub", style = MaterialTheme.typography.titleLarge, color = textPrimary)
                }
            }
            item {
                Text(text = "StudyHub v1.0", style = MaterialTheme.typography.bodyLarge, color = textPrimary)
            }
        }
    }
}
