package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AdmissionTargetEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.RoyalBlue
import com.example.util.AppLanguage
import com.example.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    admissions: List<AdmissionTargetEntity>,
    isDarkTheme: Boolean,
    lang: AppLanguage
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lang == AppLanguage.BANGLA) "ক্যালেন্ডার ও রুটিন" else "Calendar & Planner", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(if (lang == AppLanguage.BANGLA) "স্মার্ট প্ল্যানার" else "Study Planner") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(if (lang == AppLanguage.BANGLA) "ভর্তি ক্যালেন্ডার" else "Admission Calendar") }
                )
            }

            if (selectedTab == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "আজকের পড়াশোনার পরিকল্পনা" else "Today's Study Schedule",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "• সকাল ০৯:০০ - পদার্থবিজ্ঞান ভেক্টর বিশ্লেষণ" else "• 09:00 AM - Physics Vector Analysis",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "• দুপুর ০২:০০ - রসায়ন পারমাণবিক গঠন" else "• 02:00 PM - Chemistry Atomic Structure",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                if (admissions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(
                            text = if (lang == AppLanguage.BANGLA) "কোনো বিশ্ববিদ্যালয়ের ভর্তি পরীক্ষার তারিখ যোগ করা হয়নি" else "No admission target dates added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(admissions, key = { it.id }) { adm ->
                            val dateStr = dateFormat.format(Date(adm.examDate))
                            val displayDate = if (lang == AppLanguage.BANGLA) AppStrings.toBengaliDigits(dateStr) else dateStr

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = adm.universityName + if (adm.unit.isNotBlank()) " (${adm.unit})" else "",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${if (lang == AppLanguage.BANGLA) "পরীক্ষার তারিখ: " else "Exam Date: "}$displayDate",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = RoyalBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
