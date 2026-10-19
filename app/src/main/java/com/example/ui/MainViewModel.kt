package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ScreenDestination {
    HOME, SYLLABUS, SUBJECT_DETAILS, FOCUS, SMART_PLANNER, CALENDAR,
    REVISION_TRACKER, PROGRESS_ANALYTICS, PROFILE, NOTIFICATIONS,
    MOCK_TESTS, MISTAKE_BOOK, ABOUT, DATA_MANAGEMENT, PRIVACY, TERMS
}

enum class BottomNavTab {
    HOME, SYLLABUS, FOCUS, CALENDAR, PROFILE
}

data class StudyStats(
    val todayStudySeconds: Long = 0L,
    val streakDays: Int = 1
)

data class SubjectItem(
    val id: Long,
    val name: String,
    val progress: Float = 0f
)

data class ChapterItem(
    val id: Long,
    val subjectId: Long,
    val title: String,
    val isCompleted: Boolean = false
)

data class AdmissionItem(
    val id: Long,
    val universityName: String,
    val examDate: String
)

data class NotificationMessage(
    val id: Long,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Fresh install defaults to Light theme (false)
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // Splash screen visible on cold launch
    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    private val _currentScreen = MutableStateFlow(ScreenDestination.HOME)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(BottomNavTab.HOME)
    val currentTab: StateFlow<BottomNavTab> = _currentTab.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _studyStats = MutableStateFlow(StudyStats(todayStudySeconds = 0L, streakDays = 1))
    val studyStats: StateFlow<StudyStats> = _studyStats.asStateFlow()

    private val _subjects = MutableStateFlow(listOf(
        SubjectItem(1L, "Physics", 0.2f),
        SubjectItem(2L, "Chemistry", 0.4f),
        SubjectItem(3L, "Mathematics", 0.1f)
    ))
    val subjects: StateFlow<List<SubjectItem>> = _subjects.asStateFlow()

    private val _chapters = MutableStateFlow(listOf(
        ChapterItem(101L, 1L, "Vector Analysis", true),
        ChapterItem(102L, 1L, "Newtonian Mechanics", false),
        ChapterItem(103L, 2L, "Atomic Structure", true)
    ))
    val chapters: StateFlow<List<ChapterItem>> = _chapters.asStateFlow()

    private val _admissions = MutableStateFlow(listOf(
        AdmissionItem(1L, "University Admission Test", "Nov 15, 2026")
    ))
    val admissions: StateFlow<List<AdmissionItem>> = _admissions.asStateFlow()

    private val _notifications = MutableStateFlow(listOf(
        NotificationMessage(1L, "Welcome to StudyHub", "Your intelligent study assistant is ready.")
    ))
    val notifications: StateFlow<List<NotificationMessage>> = _notifications.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId: StateFlow<Long?> = _selectedSubjectId.asStateFlow()

    fun dismissSplash() {
        _showSplash.value = false
    }

    fun completeOnboarding(goalHours: Int, focusMins: Int, breakMins: Int) {
        _isOnboardingCompleted.value = true
        _showSplash.value = false
    }

    fun setDarkTheme(dark: Boolean) {
        _isDarkTheme.value = dark
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    val screenBackStack = mutableListOf<ScreenDestination>()

    fun navigateTo(destination: ScreenDestination) {
        if (_currentScreen.value != destination) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = destination
            syncTabWithScreen(destination)
        }
    }

    fun navigateBack() {
        if (screenBackStack.isNotEmpty()) {
            val prev = screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = prev
            syncTabWithScreen(prev)
        } else {
            _currentScreen.value = ScreenDestination.HOME
            _currentTab.value = BottomNavTab.HOME
        }
    }

    fun selectTab(tab: BottomNavTab) {
        _currentTab.value = tab
        val dest = when (tab) {
            BottomNavTab.HOME -> ScreenDestination.HOME
            BottomNavTab.SYLLABUS -> ScreenDestination.SYLLABUS
            BottomNavTab.FOCUS -> ScreenDestination.FOCUS
            BottomNavTab.CALENDAR -> ScreenDestination.CALENDAR
            BottomNavTab.PROFILE -> ScreenDestination.PROFILE
        }
        _currentScreen.value = dest
        screenBackStack.clear()
    }

    private fun syncTabWithScreen(dest: ScreenDestination) {
        when (dest) {
            ScreenDestination.HOME, ScreenDestination.NOTIFICATIONS -> _currentTab.value = BottomNavTab.HOME
            ScreenDestination.SYLLABUS, ScreenDestination.SUBJECT_DETAILS, ScreenDestination.REVISION_TRACKER -> _currentTab.value = BottomNavTab.SYLLABUS
            ScreenDestination.FOCUS -> _currentTab.value = BottomNavTab.FOCUS
            ScreenDestination.CALENDAR, ScreenDestination.SMART_PLANNER -> _currentTab.value = BottomNavTab.CALENDAR
            ScreenDestination.PROFILE, ScreenDestination.ABOUT, ScreenDestination.DATA_MANAGEMENT, ScreenDestination.PRIVACY, ScreenDestination.TERMS, ScreenDestination.MOCK_TESTS, ScreenDestination.MISTAKE_BOOK, ScreenDestination.PROGRESS_ANALYTICS -> _currentTab.value = BottomNavTab.PROFILE
        }
    }

    fun openSubjectDetails(subjectId: Long) {
        _selectedSubjectId.value = subjectId
        navigateTo(ScreenDestination.SUBJECT_DETAILS)
    }

    fun addStudySeconds(secs: Long) {
        val curr = _studyStats.value
        _studyStats.value = curr.copy(todayStudySeconds = curr.todayStudySeconds + secs)
    }
}
