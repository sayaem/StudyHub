package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChapterItem
import com.example.ui.MainViewModel
import com.example.ui.SubjectItem
import com.example.ui.theme.RoyalBlue
import com.example.util.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    viewModel: MainViewModel,
    timerState: Any?,
    subjects: List<SubjectItem>,
    chapters: List<ChapterItem>,
    isDarkTheme: Boolean,
    lang: AppLanguage
) {
    var isRunning by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(1500) } // 25 mins

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lang == AppLanguage.BANGLA) "ফোকাস টাইমার" else "Focus Timer") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val mins = secondsLeft / 60
            val secs = secondsLeft % 60
            Text(
                text = String.format("%02d:%02d", mins, secs),
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold, fontSize = 64.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        isRunning = !isRunning
                        if (isRunning) {
                            viewModel.addStudySeconds(25 * 60)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(text = if (isRunning) "Pause" else "Start Focus")
                }
                OutlinedButton(onClick = {
                    isRunning = false
                    secondsLeft = 1500
                }) {
                    Text(text = "Reset")
                }
            }
        }
    }
}
