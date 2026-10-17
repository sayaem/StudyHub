package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkMidnightBg
import com.example.util.AppLanguage

@Composable
fun CalendarHubScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }
    val tabs = listOf(
        if (lang == AppLanguage.BANGLA) "স্মার্ট প্ল্যানার" else "Study Planner",
        if (lang == AppLanguage.BANGLA) "ভর্তি ক্যালেন্ডার" else "Admission Calendar"
    )

    Column(modifier = modifier.fillMaxSize().background(if (isDarkTheme) DarkMidnightBg else MaterialTheme.colorScheme.background)) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedTabIndex == 0) {
                SmartPlannerScreen(viewModel, isDarkTheme, lang)
            } else {
                CalendarScreen(viewModel, viewModel.admissions.collectAsState().value, isDarkTheme, lang)
            }
        }
    }
}
