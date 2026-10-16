package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BottomTab
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressAnalyticsScreen
import com.example.ui.screens.RevisionTrackerScreen
import com.example.ui.screens.SmartPlannerScreen
import com.example.ui.screens.SubjectDetailsScreen
import com.example.ui.screens.SyllabusScreen
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

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                StudyHubApp(viewModel = viewModel, isDarkTheme = isDarkTheme)
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
            ScreenDestination.SMART_PLANNER -> viewModel.navigateTo(ScreenDestination.HOME)
            ScreenDestination.REVISION_TRACKER -> viewModel.navigateTo(ScreenDestination.SYLLABUS)
            ScreenDestination.PROGRESS_ANALYTICS -> viewModel.navigateTo(ScreenDestination.PROFILE)
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
                    lang = lang
                )
                ScreenDestination.SMART_PLANNER -> SmartPlannerScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang
                )
                ScreenDestination.REVISION_TRACKER -> RevisionTrackerScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateTo(ScreenDestination.SYLLABUS) }
                )
                ScreenDestination.CALENDAR -> CalendarScreen(
                    viewModel = viewModel,
                    admissions = admissions,
                    isDarkTheme = isDarkTheme,
                    lang = lang
                )
                ScreenDestination.PROGRESS_ANALYTICS -> ProgressAnalyticsScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onBack = { viewModel.navigateTo(ScreenDestination.PROFILE) }
                )
                ScreenDestination.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    lang = lang,
                    onNavigate = { viewModel.navigateTo(it) }
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
    val navBorder = if (isDarkTheme) DarkBorder else SlateBorder

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = navBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, navBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                label = AppStrings.navHome(lang),
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                isSelected = currentTab == BottomTab.HOME,
                isDarkTheme = isDarkTheme,
                testTag = "nav_home",
                onClick = { onTabSelected(BottomTab.HOME) }
            )

            NavBarItem(
                label = AppStrings.navSyllabus(lang),
                selectedIcon = Icons.Filled.MenuBook,
                unselectedIcon = Icons.Outlined.MenuBook,
                isSelected = currentTab == BottomTab.SYLLABUS,
                isDarkTheme = isDarkTheme,
                testTag = "nav_syllabus",
                onClick = { onTabSelected(BottomTab.SYLLABUS) }
            )

            NavBarItem(
                label = AppStrings.navFocus(lang),
                selectedIcon = Icons.Filled.Timer,
                unselectedIcon = Icons.Outlined.Timer,
                isSelected = currentTab == BottomTab.FOCUS,
                isDarkTheme = isDarkTheme,
                testTag = "nav_focus",
                onClick = { onTabSelected(BottomTab.FOCUS) }
            )

            NavBarItem(
                label = AppStrings.navCalendar(lang),
                selectedIcon = Icons.Filled.CalendarMonth,
                unselectedIcon = Icons.Outlined.CalendarMonth,
                isSelected = currentTab == BottomTab.CALENDAR,
                isDarkTheme = isDarkTheme,
                testTag = "nav_calendar",
                onClick = { onTabSelected(BottomTab.CALENDAR) }
            )

            NavBarItem(
                label = AppStrings.navProfile(lang),
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                isSelected = currentTab == BottomTab.PROFILE,
                isDarkTheme = isDarkTheme,
                testTag = "nav_profile",
                onClick = { onTabSelected(BottomTab.PROFILE) }
            )
        }
    }
}

@Composable
fun NavBarItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val activeColor = RoyalBlue
    val inactiveColor = if (isDarkTheme) DarkTextMuted else SlateMuted

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}
