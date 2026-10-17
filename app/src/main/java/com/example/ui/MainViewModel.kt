package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StudyHubDatabase
import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.PlannerTaskEntity
import com.example.data.model.RevisionItemEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.data.repository.StudyHubRepository
import com.example.util.AppLanguage
import com.example.util.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class BottomTab {
    HOME, SYLLABUS, FOCUS, CALENDAR, PROFILE
}

enum class ScreenDestination {
    HOME,
    SYLLABUS,
    SUBJECT_DETAILS,
    FOCUS,
    SMART_PLANNER,
    REVISION_TRACKER,
    CALENDAR,
    PROGRESS_ANALYTICS,
    PROFILE,
    NOTIFICATIONS,
    MOCK_TESTS,
    MISTAKE_BOOK,
    ABOUT,
    PRIVACY,
    TERMS,
    DATA_MANAGEMENT
}

enum class SyllabusFilter {
    ALL, NOT_STARTED, IN_PROGRESS, COMPLETED, REVISION_NEEDED
}

data class TimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val mode: String = "POMODORO",
    val targetSeconds: Long = 45 * 60L,
    val currentSeconds: Long = 45 * 60L,
    val currentSession: Int = 1,
    val totalSessions: Int = 4,
    val selectedSubjectId: Long? = null,
    val selectedChapterId: Long? = null,
    val sessionNotes: String = ""
)

data class StudyStats(
    val todayStudySeconds: Long = 0L,
    val totalStudySeconds: Long = 0L,
    val streakDays: Int = 0,
    val completedChapters: Int = 0,
    val totalChapters: Int = 0,
    val completionPercent: Float = 0f,
    val pendingRevisionsCount: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val database = StudyHubDatabase.getDatabase(application, viewModelScope)
    val repository = StudyHubRepository(database)

    // User Profile
    private val _userName = MutableStateFlow(prefs.userName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun updateUserName(name: String) {
        val trimmed = name.trim().ifBlank { "Arafat" }
        prefs.userName = trimmed
        _userName.value = trimmed
    }

    // Persistent Dark Theme state
    private val _isDarkTheme = MutableStateFlow(prefs.isDarkTheme)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        val newVal = !_isDarkTheme.value
        prefs.isDarkTheme = newVal
        _isDarkTheme.value = newVal
    }

    // Persistent Language
    private val _currentLanguage = MutableStateFlow(
        if (prefs.language == "bn") AppLanguage.BANGLA else AppLanguage.ENGLISH
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun toggleLanguage() {
        val next = if (_currentLanguage.value == AppLanguage.ENGLISH) AppLanguage.BANGLA else AppLanguage.ENGLISH
        prefs.language = next.code
        _currentLanguage.value = next
    }

    // Notifications state
    private val _isNotificationsEnabled = MutableStateFlow(prefs.isNotificationsEnabled)
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    fun toggleNotifications() {
        val newVal = !_isNotificationsEnabled.value
        prefs.isNotificationsEnabled = newVal
        _isNotificationsEnabled.value = newVal
    }

    private val notificationEngine = com.example.domain.notification.AdaptiveNotificationEngine(prefs)

    val isAdaptiveNotificationsEnabled = MutableStateFlow(prefs.isAdaptiveNotificationsEnabled)
    fun toggleAdaptiveNotifications() {
        val newVal = !isAdaptiveNotificationsEnabled.value
        prefs.isAdaptiveNotificationsEnabled = newVal
        isAdaptiveNotificationsEnabled.value = newVal
    }

    val quietHoursStart = MutableStateFlow(prefs.quietHoursStart)
    val quietHoursEnd = MutableStateFlow(prefs.quietHoursEnd)
    fun setQuietHours(start: Int, end: Int) {
        prefs.quietHoursStart = start
        prefs.quietHoursEnd = end
        quietHoursStart.value = start
        quietHoursEnd.value = end
    }

    val catAdmissionEnabled = MutableStateFlow(prefs.catAdmissionEnabled)
    val catRevisionEnabled = MutableStateFlow(prefs.catRevisionEnabled)
    val catPlanEnabled = MutableStateFlow(prefs.catPlanEnabled)
    val catRoutineEnabled = MutableStateFlow(prefs.catRoutineEnabled)

    fun toggleCategory(category: com.example.domain.notification.NotificationCategory) {
        when (category) {
            com.example.domain.notification.NotificationCategory.ADMISSION -> {
                val v = !catAdmissionEnabled.value
                prefs.catAdmissionEnabled = v
                catAdmissionEnabled.value = v
            }
            com.example.domain.notification.NotificationCategory.REVISION -> {
                val v = !catRevisionEnabled.value
                prefs.catRevisionEnabled = v
                catRevisionEnabled.value = v
            }
            com.example.domain.notification.NotificationCategory.PLAN_RECOVERY -> {
                val v = !catPlanEnabled.value
                prefs.catPlanEnabled = v
                catPlanEnabled.value = v
            }
            com.example.domain.notification.NotificationCategory.ROUTINE -> {
                val v = !catRoutineEnabled.value
                prefs.catRoutineEnabled = v
                catRoutineEnabled.value = v
            }
            else -> {}
        }
    }

    fun dismissNotification(id: String) {
        prefs.dismissNotification(id)
        _notificationTrigger.value = System.currentTimeMillis()
    }

    fun snoozeNotification(id: String, durationMs: Long) {
        prefs.snoozeNotification(id, System.currentTimeMillis() + durationMs)
        _notificationTrigger.value = System.currentTimeMillis()
    }

    private val _notificationTrigger = MutableStateFlow(System.currentTimeMillis())

    // Navigation
    private val _currentScreen = MutableStateFlow(ScreenDestination.HOME)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(BottomTab.HOME)
    val currentTab: StateFlow<BottomTab> = _currentTab.asStateFlow()

    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
        when (destination) {
            ScreenDestination.HOME, ScreenDestination.NOTIFICATIONS, ScreenDestination.MOCK_TESTS, ScreenDestination.MISTAKE_BOOK -> _currentTab.value = BottomTab.HOME
            ScreenDestination.SYLLABUS, ScreenDestination.SUBJECT_DETAILS, ScreenDestination.REVISION_TRACKER -> _currentTab.value = BottomTab.SYLLABUS
            ScreenDestination.FOCUS -> _currentTab.value = BottomTab.FOCUS
            ScreenDestination.CALENDAR, ScreenDestination.SMART_PLANNER -> _currentTab.value = BottomTab.CALENDAR
            ScreenDestination.PROFILE, ScreenDestination.PROGRESS_ANALYTICS, ScreenDestination.ABOUT, ScreenDestination.DATA_MANAGEMENT -> _currentTab.value = BottomTab.PROFILE
            else -> {}
        }
    }

    fun selectTab(tab: BottomTab) {
        _currentTab.value = tab
        when (tab) {
            BottomTab.HOME -> _currentScreen.value = ScreenDestination.HOME
            BottomTab.SYLLABUS -> _currentScreen.value = ScreenDestination.SYLLABUS
            BottomTab.FOCUS -> _currentScreen.value = ScreenDestination.FOCUS
            BottomTab.CALENDAR -> _currentScreen.value = ScreenDestination.CALENDAR
            BottomTab.PROFILE -> _currentScreen.value = ScreenDestination.PROFILE
        }
    }

    // Subject Details state
    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId: StateFlow<Long?> = _selectedSubjectId.asStateFlow()

    fun openSubjectDetails(subjectId: Long) {
        _selectedSubjectId.value = subjectId
        navigateTo(ScreenDestination.SUBJECT_DETAILS)
    }

    // Database Flows
    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chapters: StateFlow<List<ChapterEntity>> = repository.allChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<StudySessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val admissions: StateFlow<List<AdmissionTargetEntity>> = repository.allAdmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plannerTasks: StateFlow<List<PlannerTaskEntity>> = repository.allPlannerTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<com.example.domain.notification.AdaptiveNotification>> = combine(
        admissions,
        chapters,
        plannerTasks,
        _notificationTrigger
    ) { adm, ch, plan, _ ->
        notificationEngine.evaluateNotifications(adm, ch, plan)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val revisionItems: StateFlow<List<RevisionItemEntity>> = repository.allRevisions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mockTests: StateFlow<List<com.example.data.model.MockTestEntity>> = repository.allMockTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mistakes: StateFlow<List<com.example.data.model.MistakeEntity>> = repository.allMistakes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Syllabus Filter State
    private val _selectedSubjectFilter = MutableStateFlow<Long?>(null)
    val selectedSubjectFilter: StateFlow<Long?> = _selectedSubjectFilter.asStateFlow()

    private val _syllabusStatusFilter = MutableStateFlow(SyllabusFilter.ALL)
    val syllabusStatusFilter: StateFlow<SyllabusFilter> = _syllabusStatusFilter.asStateFlow()

    fun setSubjectFilter(subjectId: Long?) {
        _selectedSubjectFilter.value = subjectId
    }

    fun setStatusFilter(filter: SyllabusFilter) {
        _syllabusStatusFilter.value = filter
    }

    // Smart Planner State
    private val _plannerTimeRange = MutableStateFlow("TODAY")
    val plannerTimeRange: StateFlow<String> = _plannerTimeRange.asStateFlow()

    private val intelligenceEngine = com.example.domain.intelligence.StudyIntelligenceEngine()

    val syllabusRecovery: StateFlow<com.example.domain.intelligence.SyllabusRecoveryReport> = combine(
        chapters,
        admissions
    ) { ch, adm ->
        intelligenceEngine.calculateSyllabusRecovery(ch, adm)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.domain.intelligence.SyllabusRecoveryReport(0, 0, 0, null, 0f, false, "", ""))

    val studyInsights: StateFlow<List<com.example.domain.intelligence.StudyInsight>> = combine(
        chapters,
        sessions,
        admissions
    ) { ch, ses, adm ->
        intelligenceEngine.generateInsights(ch, ses, adm)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    fun setPlannerTimeRange(range: String) {
        _plannerTimeRange.value = range
    }

    fun togglePlannerTask(taskId: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            repository.updatePlannerTaskCompletion(taskId, !currentCompleted)
        }
    }

    fun addPlannerTask(timeSlot: String, subject: String, topic: String, dateCategory: String) {
        viewModelScope.launch {
            repository.insertPlannerTask(
                PlannerTaskEntity(
                    timeSlot = timeSlot.ifBlank { "09:00 - 10:30" },
                    subjectTitle = subject.ifBlank { "General" },
                    topicTitle = topic.ifBlank { "Study Session" },
                    dateCategory = dateCategory,
                    isCompleted = false,
                    colorHex = "#2563EB"
                )
            )
        }
    }

    fun deletePlannerTask(taskId: Long) {
        viewModelScope.launch {
            repository.deletePlannerTask(taskId)
        }
    }

    fun generateSmartPlanForCategory(category: String) {
        viewModelScope.launch {
            val allChaps = chapters.value
            val allSubs = subjects.value.associateBy { it.id }

            // Pick unfinished or revision chapters
            val candidates = allChaps.filter { it.status != "COMPLETED" || it.isRevisionNeeded }
                .sortedWith(
                    compareByDescending<ChapterEntity> { it.priority == "HIGH" }
                        .thenByDescending { it.isRevisionNeeded }
                        .thenByDescending { it.status == "IN_PROGRESS" }
                )

            val timeSlots = listOf("08:00 - 09:30", "10:00 - 11:30", "02:00 - 03:30", "04:30 - 06:00")
            val newTasks = candidates.take(4).mapIndexed { idx, ch ->
                val subName = allSubs[ch.subjectId]?.nameEn ?: "Subject"
                val slot = timeSlots.getOrElse(idx) { "07:00 - 08:30" }
                val color = if (ch.isRevisionNeeded) "#10B981" else "#2563EB"

                PlannerTaskEntity(
                    timeSlot = slot,
                    subjectTitle = subName,
                    topicTitle = ch.titleEn,
                    dateCategory = category,
                    isCompleted = false,
                    colorHex = color,
                    subjectId = ch.subjectId,
                    chapterId = ch.id
                )
            }

            if (newTasks.isNotEmpty()) {
                repository.insertPlannerTasks(newTasks)
            }
        }
    }

    // Revision Tracker State
    private val _revisionTab = MutableStateFlow("PENDING")
    val revisionTab: StateFlow<String> = _revisionTab.asStateFlow()

    fun setRevisionTab(tab: String) {
        _revisionTab.value = tab
    }

    fun addRevisionItem(title: String, subjectName: String, totalCount: Int) {
        viewModelScope.launch {
            repository.insertRevision(
                RevisionItemEntity(
                    title = title.ifBlank { "New Topic" },
                    subjectName = subjectName.ifBlank { "General" },
                    completedCount = 0,
                    totalCount = totalCount.coerceAtLeast(1),
                    isCompleted = false
                )
            )
        }
    }

    fun incrementRevision(id: Long, currentCompleted: Int, total: Int) {
        viewModelScope.launch {
            val nextCompleted = currentCompleted + 1
            val completed = nextCompleted >= total
            repository.updateRevisionProgress(id, nextCompleted, completed)
        }
    }

    fun deleteRevision(id: Long) {
        viewModelScope.launch {
            repository.deleteRevision(id)
        }
    }

    // Analytics Range
    private val _analyticsRange = MutableStateFlow("7_DAYS")
    val analyticsRange: StateFlow<String> = _analyticsRange.asStateFlow()

    fun setAnalyticsRange(range: String) {
        _analyticsRange.value = range
    }

    // Dynamically Calculated Study Stats from Source of Truth Tables
    val studyStats: StateFlow<StudyStats> = combine(
        chapters,
        sessions,
        revisionItems
    ) { chaps, sess, revs ->
        val midnightToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todaySeconds = sess.filter { it.timestamp >= midnightToday }
            .sumOf { it.durationSeconds }

        val totalSeconds = sess.sumOf { it.durationSeconds }

        val completedCount = chaps.count { it.status == "COMPLETED" }
        val totalCount = chaps.size
        val percent = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()) * 100f else 0f
        val revisionCount = chaps.count { it.isRevisionNeeded } + revs.count { !it.isCompleted }

        // Streak calculation from distinct days with sessions
        val dayBuckets = sess.map {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
        }.toSet()
        val calculatedStreak = dayBuckets.size

        StudyStats(
            todayStudySeconds = todaySeconds,
            totalStudySeconds = totalSeconds,
            streakDays = calculatedStreak,
            completedChapters = completedCount,
            totalChapters = totalCount,
            completionPercent = percent,
            pendingRevisionsCount = revisionCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudyStats())

    // Robust Focus Timer with Timestamp-Based Tracking & Recovery
    private val _timerState = MutableStateFlow(
        TimerState(
            targetSeconds = 45 * 60L,
            currentSeconds = 45 * 60L,
            currentSession = 1,
            totalSessions = 4
        )
    )
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private var timerStartEpochMs: Long = 0L
    private var elapsedBeforePauseSeconds: Long = 0L

    init {
        restoreTimerIfRunning()
    }

    private fun restoreTimerIfRunning() {
        if (prefs.getTimerRunning()) {
            val isPaused = prefs.getTimerPaused()
            val startMs = prefs.getTimerStartMs()
            val targetSecs = prefs.getTimerTargetSecs()
            val elapsedPaused = prefs.getTimerElapsedBeforePause()
            val subjectId = prefs.getTimerSubjectId()
            val chapterId = prefs.getTimerChapterId()

            val totalElapsed = if (isPaused) {
                elapsedPaused
            } else {
                elapsedPaused + ((System.currentTimeMillis() - startMs) / 1000L).coerceAtLeast(0L)
            }

            val remaining = (targetSecs - totalElapsed).coerceAtLeast(0L)

            _timerState.value = TimerState(
                isRunning = true,
                isPaused = isPaused,
                targetSeconds = targetSecs,
                currentSeconds = remaining,
                selectedSubjectId = subjectId,
                selectedChapterId = chapterId
            )

            if (!isPaused && remaining > 0) {
                elapsedBeforePauseSeconds = elapsedPaused
                timerStartEpochMs = startMs
                launchTimerTicker()
            } else if (remaining <= 0) {
                finishSession()
            }
        }
    }

    fun selectTimerSubject(subjectId: Long?) {
        _timerState.value = _timerState.value.copy(
            selectedSubjectId = subjectId,
            selectedChapterId = null
        )
    }

    fun selectTimerChapter(chapterId: Long?) {
        _timerState.value = _timerState.value.copy(selectedChapterId = chapterId)
    }

    fun setTimerDuration(seconds: Long) {
        if (_timerState.value.isRunning && !_timerState.value.isPaused) return
        _timerState.value = _timerState.value.copy(
            targetSeconds = seconds,
            currentSeconds = seconds
        )
    }

    fun startTimer() {
        if (_timerState.value.isRunning && !_timerState.value.isPaused) return

        val now = System.currentTimeMillis()
        timerStartEpochMs = now

        _timerState.value = _timerState.value.copy(isRunning = true, isPaused = false)

        prefs.saveTimerState(
            isRunning = true,
            isPaused = false,
            startMs = timerStartEpochMs,
            targetSecs = _timerState.value.targetSeconds,
            elapsedBeforePause = elapsedBeforePauseSeconds,
            subjectId = _timerState.value.selectedSubjectId,
            chapterId = _timerState.value.selectedChapterId
        )

        launchTimerTicker()
    }

    private fun launchTimerTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.isRunning && !_timerState.value.isPaused) {
                delay(1000L)
                val now = System.currentTimeMillis()
                val currentRunningElapsed = (now - timerStartEpochMs) / 1000L
                val totalElapsed = elapsedBeforePauseSeconds + currentRunningElapsed
                val target = _timerState.value.targetSeconds
                val remaining = (target - totalElapsed).coerceAtLeast(0L)

                _timerState.value = _timerState.value.copy(currentSeconds = remaining)

                if (remaining <= 0L) {
                    vibrateDevice()
                    finishSession()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        val now = System.currentTimeMillis()
        val currentRunningElapsed = (now - timerStartEpochMs) / 1000L
        elapsedBeforePauseSeconds += currentRunningElapsed

        _timerState.value = _timerState.value.copy(isPaused = true)

        prefs.saveTimerState(
            isRunning = true,
            isPaused = true,
            startMs = 0L,
            targetSecs = _timerState.value.targetSeconds,
            elapsedBeforePause = elapsedBeforePauseSeconds,
            subjectId = _timerState.value.selectedSubjectId,
            chapterId = _timerState.value.selectedChapterId
        )
    }

    fun resetTimer() {
        timerJob?.cancel()
        prefs.clearTimerState()
        elapsedBeforePauseSeconds = 0L
        timerStartEpochMs = 0L

        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = false,
            currentSeconds = _timerState.value.targetSeconds
        )
    }

    fun finishSession() {
        timerJob?.cancel()
        val current = _timerState.value
        val now = System.currentTimeMillis()
        val currentRunningElapsed = if (current.isRunning && !current.isPaused) {
            (now - timerStartEpochMs) / 1000L
        } else 0L

        val durationStudied = (elapsedBeforePauseSeconds + currentRunningElapsed)
            .coerceIn(0L, current.targetSeconds)

        prefs.clearTimerState()
        elapsedBeforePauseSeconds = 0L
        timerStartEpochMs = 0L

        // Record session if > 5 seconds
        if (durationStudied >= 5L) {
            viewModelScope.launch {
                val subjectName = subjects.value.find { it.id == current.selectedSubjectId }?.nameEn ?: "General Focus"
                val chapterName = chapters.value.find { it.id == current.selectedChapterId }?.titleEn ?: "Study Session"

                repository.logSession(
                    StudySessionEntity(
                        subjectId = current.selectedSubjectId,
                        chapterId = current.selectedChapterId,
                        durationSeconds = durationStudied,
                        timestamp = now - (durationStudied * 1000L),
                        endTime = now,
                        isCompleted = durationStudied >= current.targetSeconds * 0.9, // completed if 90% done
                        sessionType = "POMODORO",
                        notes = "$subjectName: $chapterName"
                    )
                )
            }
        }

        _timerState.value = current.copy(
            isRunning = false,
            isPaused = false,
            currentSeconds = current.targetSeconds,
            currentSession = (current.currentSession % current.totalSessions) + 1
        )
    }

    private fun vibrateDevice() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.vibrate(
                    CombinedVibration.createParallel(
                        VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (_: Exception) {}
    }

    // Chapter Actions
    fun cycleChapterStatus(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.cycleChapterStatus(chapter)
        }
    }

    fun toggleRevisionNeeded(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.toggleRevisionNeeded(chapter)
        }
    }

    fun markChapterRevised(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.markChapterRevised(chapter)
        }
    }

    fun addChapter(subjectId: Long, titleEn: String, titleBn: String, priority: String, estimatedMins: Int) {
        viewModelScope.launch {
            repository.insertChapter(
                ChapterEntity(
                    subjectId = subjectId,
                    titleEn = titleEn.ifBlank { "New Chapter" },
                    titleBn = titleBn.ifBlank { titleEn },
                    status = "NOT_STARTED",
                    priority = priority,
                    estimatedMinutes = estimatedMins
                )
            )
        }
    }

    fun deleteChapter(chapterId: Long) {
        viewModelScope.launch {
            repository.deleteChapter(chapterId)
        }
    }

    fun updateChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.updateChapter(chapter)
        }
    }

    fun updateChapterStatus(chapterId: Long, newStatus: String) {
        viewModelScope.launch {
            val chapter = chapters.value.find { it.id == chapterId } ?: return@launch
            repository.updateChapter(chapter.copy(status = newStatus))
        }
    }

    fun toggleChapterRevision(chapterId: Long, isNeeded: Boolean) {
        viewModelScope.launch {
            val chapter = chapters.value.find { it.id == chapterId } ?: return@launch
            repository.updateChapter(chapter.copy(isRevisionNeeded = isNeeded))
        }
    }

    // Subject Actions
    fun addSubject(nameEn: String, nameBn: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertSubject(
                SubjectEntity(
                    nameEn = nameEn.ifBlank { "New Subject" },
                    nameBn = nameBn.ifBlank { nameEn },
                    colorHex = colorHex,
                    iconName = "atom"
                )
            )
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subjectId: Long) {
        viewModelScope.launch {
            repository.deleteSubject(subjectId)
        }
    }

    // Admission Actions
    fun cycleAdmissionStatus(target: AdmissionTargetEntity) {
        viewModelScope.launch {
            repository.cycleAdmissionStatus(target)
        }
    }

    fun addAdmissionTarget(university: String, unit: String, deadlineDays: Int, examDays: Int, notes: String) {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        viewModelScope.launch {
            repository.insertAdmissionTarget(
                AdmissionTargetEntity(
                    universityName = university.ifBlank { "University Target" },
                    unit = unit.ifBlank { "General" },
                    applicationDeadline = now + (deadlineDays * dayMs),
                    examDate = now + (examDays * dayMs),
                    status = "NOT_APPLIED",
                    notes = notes
                )
            )
        }
    }

    fun deleteAdmissionTarget(id: Long) {
        viewModelScope.launch {
            repository.deleteAdmissionTarget(id)
        }
    }

    fun updateAdmissionTarget(id: Long, university: String, unit: String, deadlineDays: Int, examDays: Int, notes: String) {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        viewModelScope.launch {
            val existing = admissions.value.find { it.id == id }
            if (existing != null) {
                repository.updateAdmissionTarget(
                    existing.copy(
                        universityName = university.ifBlank { existing.universityName },
                        unit = unit.ifBlank { existing.unit },
                        applicationDeadline = now + (deadlineDays * dayMs),
                        examDate = now + (examDays * dayMs),
                        notes = notes
                    )
                )
            }
        }
    }

    // Data Management
    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            prefs.clearTimerState()
            resetTimer()
        }
    }

    fun loadSampleDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
        }
    }
}
