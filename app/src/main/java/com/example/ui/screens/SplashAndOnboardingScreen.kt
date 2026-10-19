package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkMidnightBg
import com.example.ui.theme.DarkMidnightSurface
import com.example.ui.theme.RoyalBlue
import com.example.util.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingFlowScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    onComplete: (Int, Int, Int) -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (isDarkTheme) DarkMidnightBg else Color(0xFFF8FAFC)
    ) {
        OnboardingFlow(
            viewModel = viewModel,
            isDarkTheme = isDarkTheme,
            lang = lang,
            onComplete = onComplete
        )
    }
}

@Composable
fun SplashScreen(isDarkTheme: Boolean, lang: AppLanguage) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "splashAlpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.85f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "splashScale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkTheme) {
                        listOf(DarkMidnightBg, DarkMidnightSurface)
                    } else {
                        listOf(Color(0xFFF8FAFC), Color(0xFFEEF2F6))
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scaleAnim)
                .alpha(alphaAnim)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = RoyalBlue,
                shadowElevation = 8.dp,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "StudyHub Logo",
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "StudyHub",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (lang == AppLanguage.BANGLA) "আপনার ভর্তি প্রস্তুতি সহচর" else "Your Ultimate Admission Prep Companion",
                fontSize = 14.sp,
                color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OnboardingFlow(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    lang: AppLanguage,
    onComplete: (Int, Int, Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    // Setup state
    var studyGoalHours by remember { mutableStateOf(4) }
    var focusMinutes by remember { mutableStateOf(45) }
    var breakMinutes by remember { mutableStateOf(10) }
    var tempUserName by remember { mutableStateOf("Arafat") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar with language switcher and skip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleLanguage() },
                modifier = Modifier.testTag("onboarding_lang_toggle")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = "Toggle Language",
                    tint = if (isDarkTheme) Color.White else Color(0xFF334155)
                )
            }

            if (pagerState.currentPage < 4) {
                Text(
                    text = if (lang == AppLanguage.BANGLA) "এড়িয়ে যান" else "Skip",
                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable {
                            scope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        }
                        .padding(8.dp)
                        .testTag("onboarding_skip_btn")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> OnboardingPageContent(
                    title = if (lang == AppLanguage.BANGLA) "আপনার লক্ষ্য। আপনার পরিকল্পনা। আপনার অগ্রগতি।" else "Your goals. Your plan. Your progress.",
                    description = if (lang == AppLanguage.BANGLA) "আপনার পড়াশোনা গোছাতে, ধারাবাহিকতা বজায় রাখতে এবং প্রতিটি পদক্ষেপে অগ্রগতি ট্র্যাক করতে StudyHub-এ স্বাগতম।" else "Organize your study, build consistency, and track every milestone with clarity and precision.",
                    icon = Icons.Default.School,
                    isDarkTheme = isDarkTheme
                )
                1 -> OnboardingPageContent(
                    title = if (lang == AppLanguage.BANGLA) "প্রতিটি অধ্যায়, সাফল্যের দিকে এক ধাপ।" else "Every chapter, one step closer.",
                    description = if (lang == AppLanguage.BANGLA) "বিষয় ও অধ্যায়গুলো সুন্দরভাবে সাজান, সম্পন্ন হওয়া কাজ ট্র্যাক করুন এবং বাকি থাকা প্রস্তুতি সহজেই বুঝুন।" else "Organize subjects and chapters effortlessly, track completion, and stay on top of your admission syllabus.",
                    icon = Icons.Default.MenuBook,
                    isDarkTheme = isDarkTheme
                )
                2 -> OnboardingPageContent(
                    title = if (lang == AppLanguage.BANGLA) "আপনার পড়ার প্রতিটি মিনিট কাজে লাগান।" else "Make your study time count.",
                    description = if (lang == AppLanguage.BANGLA) "ফোকাস সেশন, পড়াশোনার সময় ট্র্যাকিং এবং বিভ্রান্তি এড়ানোর ব্যবস্থা নিয়ে আপনার ফোকাস আরও দৃঢ় করুন." else "Leverage reliable focus timers, distraction controls, and deep work sessions to maximize retention.",
                    icon = Icons.Default.Timer,
                    isDarkTheme = isDarkTheme
                )
                3 -> OnboardingPageContent(
                    title = if (lang == AppLanguage.BANGLA) "আপনার পরবর্তী লক্ষ্য, সবসময় চোখের সামনে।" else "Your next milestone, always in sight.",
                    description = if (lang == AppLanguage.BANGLA) "স্মার্ট স্টাডি প্ল্যান, রিভিশন এবং ভর্তি পরীক্ষার ডেডলাইন ট্র্যাক করে সবসময় এগিয়ে থাকুন।" else "Stay ahead with intelligent daily plans, automated spaced revision, and admission deadline countdowns.",
                    icon = Icons.Default.CheckCircle,
                    isDarkTheme = isDarkTheme
                )
                4 -> FirstLaunchSetupContent(
                    studyGoalHours = studyGoalHours,
                    onGoalChange = { studyGoalHours = it },
                    focusMinutes = focusMinutes,
                    onFocusChange = { focusMinutes = it },
                    breakMinutes = breakMinutes,
                    onBreakChange = { breakMinutes = it },
                    userName = tempUserName,
                    onNameChange = { tempUserName = it },
                    isDarkTheme = isDarkTheme,
                    lang = lang
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(8.dp)
                        .width(if (isSelected) 24.dp else 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isSelected) RoyalBlue else if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom action button
        Button(
            onClick = {
                scope.launch {
                    if (pagerState.currentPage < 4) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    } else {
                        viewModel.updateUserName(tempUserName)
                        onComplete(studyGoalHours, focusMinutes, breakMinutes)
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_continue_btn"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (pagerState.currentPage < 4) {
                    if (lang == AppLanguage.BANGLA) "পরবর্তী" else "Continue"
                } else {
                    if (lang == AppLanguage.BANGLA) "StudyHub শুরু করুন" else "Enter StudyHub"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun OnboardingPageContent(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDarkTheme: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = RoyalBlue.copy(alpha = 0.12f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RoyalBlue,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            fontSize = 15.sp,
            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun FirstLaunchSetupContent(
    studyGoalHours: Int,
    onGoalChange: (Int) -> Unit,
    focusMinutes: Int,
    onFocusChange: (Int) -> Unit,
    breakMinutes: Int,
    onBreakChange: (Int) -> Unit,
    userName: String,
    onNameChange: (String) -> Unit,
    isDarkTheme: Boolean,
    lang: AppLanguage
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = if (lang == AppLanguage.BANGLA) "আপনার স্টাডি স্পেস সাজিয়ে নিন" else "Personalize Your Setup",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (lang == AppLanguage.BANGLA) "আপনার পছন্দ অনুযায়ী দৈনিক লক্ষ্য ও ফোকাস সময় সেট করুন" else "Customize your daily study goals and timer preferences",
            fontSize = 14.sp,
            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name input
        Text(
            text = if (lang == AppLanguage.BANGLA) "আপনার নাম" else "Your Name",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDarkTheme) Color.White else Color(0xFF334155)
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("setup_name_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Goal Hours
        Text(
            text = if (lang == AppLanguage.BANGLA) "দৈনিক পড়াশোনার লক্ষ্য: $studyGoalHours ঘণ্টা" else "Daily Study Goal: $studyGoalHours hours",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDarkTheme) Color.White else Color(0xFF334155)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = studyGoalHours.toFloat(),
            onValueChange = { onGoalChange(it.toInt()) },
            valueRange = 1f..12f,
            steps = 10,
            modifier = Modifier.testTag("setup_goal_slider")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Focus Session Duration
        Text(
            text = if (lang == AppLanguage.BANGLA) "ফোকাস সেশন সময়: $focusMinutes মিনিট" else "Focus Session Length: $focusMinutes minutes",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDarkTheme) Color.White else Color(0xFF334155)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(25, 30, 45, 60).forEach { mins ->
                val selected = focusMinutes == mins
                OutlinedButton(
                    onClick = { onFocusChange(mins) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected) RoyalBlue else Color.Transparent,
                        contentColor = if (selected) Color.White else if (isDarkTheme) Color.White else Color(0xFF334155)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "${mins}m")
                }
            }
        }
    }
}
