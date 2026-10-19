package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.StudyHubNotificationHelper
import com.example.ui.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StudyHubNotificationHelper.initNotificationChannels(applicationContext)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
            val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                when {
                    showSplash -> {
                        SplashScreen(isDarkTheme = isDarkTheme) {
                            viewModel.dismissSplash()
                        }
                    }
                    !isOnboardingCompleted -> {
                        OnboardingFlowScreen(
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme,
                            onComplete = { goal, focus, breakMins ->
                                viewModel.completeOnboarding(goal, focus, breakMins)
                            }
                        )
                    }
                    else -> {
                        StudyHubApp(viewModel = viewModel, isDarkTheme = isDarkTheme)
                    }
                }
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
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsStateWithLifecycle()

    BackHandler(enabled = currentScreen != ScreenDestination.HOME) {
        when (currentScreen) {
            ScreenDestination.SUBJECT_DETAILS -> viewModel.navigateTo(ScreenDestination.SYLLABUS)
            ScreenDestination.SMART_PLANNER, ScreenDestination.CALENDAR -> viewModel.navigateTo(ScreenDestination.HOME)
            ScreenDestination.REVISION_TRACKER -> viewModel.navigateTo(ScreenDestination.SYLLABUS)
            ScreenDestination.PROGRESS_ANALYTICS -> viewModel.navigateBack()
            ScreenDestination.NOTIFICATIONS -> viewModel.navigateTo(ScreenDestination.HOME)
            ScreenDestination.PRIVACY, ScreenDestination.TERMS, ScreenDestination.ABOUT -> viewModel.navigateTo(ScreenDestination.PROFILE)
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
                    timerState = null,
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
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateTo(ScreenDestination.PROFILE) }
                )
                ScreenDestination.TERMS -> TermsScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateTo(ScreenDestination.PROFILE) }
                )
            }
        }
    }
}

@Composable
fun StudyHubBottomNavBar(
    currentTab: BottomNavTab,
    isDarkTheme: Boolean,
    onTabSelected: (BottomNavTab) -> Unit,
    lang: com.example.util.AppLanguage
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
            label = { Text(if (lang == com.example.util.AppLanguage.BANGLA) "হোম" else "Home") },
            selected = currentTab == BottomNavTab.HOME,
            onClick = { onTabSelected(BottomNavTab.HOME) }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.MenuBook, contentDescription = "Syllabus") },
            label = { Text(if (lang == com.example.util.AppLanguage.BANGLA) "সিলেবাস" else "Syllabus") },
            selected = currentTab == BottomNavTab.SYLLABUS,
            onClick = { onTabSelected(BottomNavTab.SYLLABUS) }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Timer, contentDescription = "Focus") },
            label = { Text(if (lang == com.example.util.AppLanguage.BANGLA) "ফোকাস" else "Focus") },
            selected = currentTab == BottomNavTab.FOCUS,
            onClick = { onTabSelected(BottomNavTab.FOCUS) }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar") },
            label = { Text(if (lang == com.example.util.AppLanguage.BANGLA) "ক্যালেন্ডার" else "Calendar") },
            selected = currentTab == BottomNavTab.CALENDAR,
            onClick = { onTabSelected(BottomNavTab.CALENDAR) }
        )
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
            label = { Text(if (lang == com.example.util.AppLanguage.BANGLA) "প্রোফাইল" else "Profile") },
            selected = currentTab == BottomNavTab.PROFILE,
            onClick = { onTabSelected(BottomNavTab.PROFILE) }
        )
    }
}
