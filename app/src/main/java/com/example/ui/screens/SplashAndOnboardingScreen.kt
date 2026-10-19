package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.RoyalBlue
import com.example.util.AppLanguage
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(isDarkTheme: Boolean, onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1200L)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RoyalBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SH",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "StudyHub",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Smart Study Planner & Assistant",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun OnboardingFlowScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    onComplete: (Int, Int, Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val lang by viewModel.currentLanguage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val title = when (page) {
                        0 -> if (lang == AppLanguage.BANGLA) "স্মার্ট স্টাডি প্ল্যানার" else "Smart Study Planner"
                        1 -> if (lang == AppLanguage.BANGLA) "ফোকাস ও প্রোডাক্টিভিটি" else "Focus & Productivity"
                        else -> if (lang == AppLanguage.BANGLA) "পরীক্ষার প্রস্তুতি ও ট্র্যাকিং" else "Exam Preparation & Tracking"
                    }
                    val desc = when (page) {
                        0 -> if (lang == AppLanguage.BANGLA) "আপনার সিলেবাস অনুযায়ী প্রতিদিনের পড়াশোনা নিখুঁতভাবে সাজান।" else "Organize your daily study routines seamlessly based on your syllabus."
                        1 -> if (lang == AppLanguage.BANGLA) "পোমোডোরো টাইমার দিয়ে মনোযোগ বাড়ান এবং ফোকাস করুন।" else "Boost concentration and stay focused with built-in Pomodoro timers."
                        else -> if (lang == AppLanguage.BANGLA) "ভর্তি পরীক্ষা ও অ্যাসাইনমেন্টের ডেডলাইন ট্র্যাক করুন।" else "Track admission tests, mock tests, and important deadlines."
                    }

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${page + 1}/3",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == i) 24.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (pagerState.currentPage == i) RoyalBlue else RoyalBlue.copy(alpha = 0.3f))
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            // next page
                        } else {
                            onComplete(4, 25, 5)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text(text = if (pagerState.currentPage == 2) "Get Started" else "Next")
                }
            }
        }
    }
}
