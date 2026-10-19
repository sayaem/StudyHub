package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.StudyHubNotificationHelper
import com.example.ui.BottomTab
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DataManagementScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MistakeBookScreen
import com.example.ui.screens.MockTestScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingFlowScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressAnalyticsScreen
import com.example.ui.screens.RevisionTrackerScreen
import com.example.ui.screens.SmartPlannerScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SubjectDetailsScreen
import com.example.ui.screens.SyllabusScreen
import com.example.ui.screens.TermsScreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkMidnightBg
import com.example.ui.theme.DarkMidnightSurface
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextWhite
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueLight
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateMuted
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.PreferencesManager
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Android notification channels & schedule background checks
        StudyHubNotificationHelper.initNotificationChannels(applicationContext)
        StudyHubNotificationHelper.scheduleAdaptivePeriodicAlarm(applicationContext)

        handleNavigationIntent(intent)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
            val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()

            // Cold launch splash lifecycle: shown once per cold start, preserved across background/resume
            var showSplash by rememberSaveable { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1000)
                showSplash = false
            }

            // Notification permission request on Android 13+ (gracefully on first launch)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notifPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    Log.d("MainActivity", "Notification permission granted: $isGranted")
                    if (isGranted) {
                        viewModel.syncNotificationsOnResume()
                    }
                }

                LaunchedEffect(Unit) {
                    val prefs = PreferencesManager(applicationContext)
                    if (!prefs.hasAskedNotificationPermission &&
                        ContextCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        prefs.hasAskedNotificationPermission = true
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSplash) {
                        SplashScreen(isDarkTheme = isDarkTheme, lang = lang)
                    } else if (!isOnboardingCompleted) {
                        OnboardingFlowScreen(
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme,
                            onComplete = { goal, focus, breakMins ->
                                viewModel.completeOnboarding(goal, focus, breakMins)
                            }
                        )
                    } else {
                        StudyHubApp(viewModel = viewModel, isDarkTheme = isDarkTheme)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncNotificationsOnResume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        val target = intent?.getStringExtra("navigate_to") ?: return
        try {
            val dest = ScreenDestination.valueOf(target)
            viewModel.navigateTo(dest)
        } catch (_: Exception) {
            when (target) {
                "FOCUS" -> viewModel.navigateTo(ScreenDestination.FOCUS)
                "CALENDAR" -> viewModel.navigateTo(ScreenDestination.CALENDAR)
                "SMART_PLANNER" -> viewModel.navigateTo(ScreenDestination.SMART_PLANNER)
                "REVISION_TRACKER" -> viewModel.navigateTo(ScreenDestination.REVISION_TRACKER)
                "SYLLABUS" -> viewModel.navigateTo(ScreenDestination.SYLLABUS)
                "MISTAKE_BOOK" -> viewModel.navigateTo(ScreenDestination.MISTAKE_BOOK)
                "NOTIFICATIONS" -> viewModel.navigateTo(ScreenDestination.NOTIFICATIONS)
                "PRIVACY" -> viewModel.navigateTo(ScreenDestination.PRIVACY)
                "TERMS" -> viewModel.navigateTo(ScreenDestination.TERMS)
                "PROFILE" -> viewModel.navigateTo(ScreenDestination.PROFILE)
                else -> {}
            }
        }
    }
}

@Composable
fun StudyHubApp(viewModel: MainViewModel, isDarkTheme: Boolean) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val stats by viewModel.studyStats.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val admissions by viewModel.admissions.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsStateWithLifecycle()

    // Handle back button on sub-screens to return to their parent
    BackHandler(enabled = currentScreen != ScreenDestination.HOME) {
        when (currentScreen) {
            ScreenDestination.SUBJECT_DETAILS -> viewModel.navigateTo(ScreenDestination.SYLLABUS)
            ScreenDestination.SMART_PLANNER, ScreenDestination.CALENDAR -> viewModel.navigateTo(ScreenDestination.HOME)
            ScreenDestination.REVISION_TRACKER -> viewModel.navigateTo(ScreenDestination.SYLLABUS)
            ScreenDestination.PROGRESS_ANALYTICS -> viewModel.navigateBack()
            ScreenDestination.NOTIFICATIONS -> viewModel.navigateTo(ScreenDestination.HOME)
            ScreenDestination.PRIVACY, ScreenDestination.TERMS -> viewModel.navigateBack()
            ScreenDestination.ABOUT, ScreenDestination.DATA_MANAGEMENT -> viewModel.navigateTo(ScreenDestination.PROFILE)
            else -> viewModel.navigateTo(ScreenDestination.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            StudyHubBottomNavBar(
                currentTab = currentTab,
                isDarkTheme = isDarkTheme,
                onTabSelected = { viewModel.selectTab(it) },
                lang = lang
            )
        }
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            label = "screenTransition",
            modifier = Modifier.padding(innerPadding)
        ) { screen ->
            when (screen) {
                ScreenDestination.HOME -> HomeScreen(
                    viewModel = viewModel,
                    stats = stats,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                ScreenDestination.SYLLABUS -> SyllabusScreen(
                    viewModel = viewModel,
                    subjects = subjects,
                    chapters = chapters,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onOpenSubject = { viewModel.openSubjectDetails(it) }
                )
                ScreenDestination.SUBJECT_DETAILS -> SubjectDetailsScreen(
                    viewModel = viewModel,
                    subjectId = selectedSubjectId ?: (subjects.firstOrNull()?.id ?: 1L),
                    subjects = subjects,
                    chapters = chapters,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateTo(ScreenDestination.SYLLABUS) }
                )
                ScreenDestination.FOCUS -> FocusTimerScreen(
                    viewModel = viewModel,
                    timerState = timerState,
                    subjects = subjects,
                    chapters = chapters,
                    isDarkTheme = isDarkTheme,
                    lang = lang
                )
                ScreenDestination.SMART_PLANNER, ScreenDestination.CALENDAR -> CalendarScreen(
                    viewModel = viewModel,
                    admissions = admissions,
                    isDarkTheme = isDarkTheme,
                    lang = lang
                )
                ScreenDestination.REVISION_TRACKER -> RevisionTrackerScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateTo(ScreenDestination.SYLLABUS) }
                )
                ScreenDestination.PROGRESS_ANALYTICS -> ProgressAnalyticsScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateBack() }
                )
                ScreenDestination.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onNavigate = { viewModel.navigateTo(it) }
                )
                ScreenDestination.NOTIFICATIONS -> NotificationsScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateTo(ScreenDestination.HOME) },
                    onNavigate = { viewModel.navigateTo(it) }
                )
                ScreenDestination.MOCK_TESTS -> MockTestScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateTo(ScreenDestination.HOME) }
                )
                ScreenDestination.MISTAKE_BOOK -> MistakeBookScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateTo(ScreenDestination.HOME) }
                )
                ScreenDestination.ABOUT -> AboutScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateTo(ScreenDestination.PROFILE) },
                    onNavigate = { viewModel.navigateTo(it) }
                )
                ScreenDestination.DATA_MANAGEMENT -> DataManagementScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateTo(ScreenDestination.PROFILE) }
                )
                ScreenDestination.PRIVACY -> PrivacyPolicyScreen(
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateBack() }
                )
                ScreenDestination.TERMS -> TermsScreen(
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateBack() }
                )
            }
        }
    }
}

@Composable
fun StudyHubBottomNavBar(
    currentTab: BottomTab,
    isDarkTheme: Boolean,
    onTabSelected: (BottomTab) -> Unit,
    lang: AppLanguage
) {
    val navBg = if (isDarkTheme) DarkMidnightSurface else Color.White
    val borderCol = if (isDarkTheme) DarkBorder else SlateBorder
    val selectedColor = RoyalBlue
    val unselectedColor = if (isDarkTheme) DarkTextMuted else SlateMuted

    val navTabs = listOf(
        NavTabItem(BottomTab.HOME, AppStrings.navHome(lang), Icons.Filled.Home, Icons.Outlined.Home),
        NavTabItem(BottomTab.SYLLABUS, AppStrings.navSyllabus(lang), Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        NavTabItem(BottomTab.FOCUS, AppStrings.navFocus(lang), Icons.Filled.Timer, Icons.Outlined.Timer),
        NavTabItem(BottomTab.CALENDAR, AppStrings.navCalendar(lang), Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        NavTabItem(BottomTab.PROFILE, AppStrings.navProfile(lang), Icons.Filled.Person, Icons.Outlined.Person)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar"),
        color = navBg,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(0.6.dp, borderCol)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navTabs.forEach { item ->
                val isSelected = currentTab == item.tab
                val icon = if (isSelected) item.selectedIcon else item.unselectedIcon

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(item.tab) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("nav_tab_${item.tab.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.title,
                        tint = if (isSelected) selectedColor else unselectedColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) selectedColor else unselectedColor
                    )
                }
            }
        }
    }
}

private data class NavTabItem(
    val tab: BottomTab,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
