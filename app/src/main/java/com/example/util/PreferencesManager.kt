package com.example.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("studyhub_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_THEME = "key_dark_theme"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_HAS_SEEDED_INITIAL = "key_has_seeded_initial"
        private const val KEY_NOTIFICATIONS_ENABLED = "key_notifications_enabled"

        // Timer background recovery
        private const val KEY_TIMER_RUNNING = "key_timer_running"
        private const val KEY_TIMER_START_MS = "key_timer_start_ms"
        private const val KEY_TIMER_TARGET_SECS = "key_timer_target_secs"
        private const val KEY_TIMER_ELAPSED_BEFORE_PAUSE = "key_timer_elapsed_before_pause"
        private const val KEY_TIMER_IS_PAUSED = "key_timer_is_paused"
        private const val KEY_TIMER_SUBJECT_ID = "key_timer_subject_id"
        private const val KEY_TIMER_CHAPTER_ID = "key_timer_chapter_id"

        // App Blocker & Distraction Control
        private const val KEY_BLOCKED_APPS = "key_blocked_apps"
        private const val KEY_DISTRACTION_BLOCKER_ENABLED = "key_distraction_blocker_enabled"
        private const val KEY_CUSTOM_TIMER_MINUTES = "key_custom_timer_minutes"
        private const val KEY_INTERRUPTION_COUNT = "key_interruption_count"
        private const val KEY_LAST_BLOCKED_APP = "key_last_blocked_app"
    }

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Arafat") ?: "Arafat"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var hasSeededInitial: Boolean
        get() = prefs.getBoolean(KEY_HAS_SEEDED_INITIAL, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_SEEDED_INITIAL, value).apply()

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean("key_is_onboarding_completed", false)
        set(value) = prefs.edit().putBoolean("key_is_onboarding_completed", value).apply()

    var onboardingStudyGoalHours: Int
        get() = prefs.getInt("key_onboarding_study_goal_hours", 4)
        set(value) = prefs.edit().putInt("key_onboarding_study_goal_hours", value).apply()

    var onboardingFocusMinutes: Int
        get() = prefs.getInt("key_onboarding_focus_minutes", 45)
        set(value) = prefs.edit().putInt("key_onboarding_focus_minutes", value).apply()

    var onboardingBreakMinutes: Int
        get() = prefs.getInt("key_onboarding_break_minutes", 10)
        set(value) = prefs.edit().putInt("key_onboarding_break_minutes", value).apply()

    var isAdaptiveNotificationsEnabled: Boolean
        get() = prefs.getBoolean("key_adaptive_notifications", true)
        set(value) = prefs.edit().putBoolean("key_adaptive_notifications", value).apply()

    var quietHoursStart: Int
        get() = prefs.getInt("key_quiet_hours_start", 22)
        set(value) = prefs.edit().putInt("key_quiet_hours_start", value).apply()

    var quietHoursEnd: Int
        get() = prefs.getInt("key_quiet_hours_end", 7)
        set(value) = prefs.edit().putInt("key_quiet_hours_end", value).apply()

    var maxDailyNotifications: Int
        get() = prefs.getInt("key_max_daily_notifs", 3)
        set(value) = prefs.edit().putInt("key_max_daily_notifs", value).apply()

    var catAdmissionEnabled: Boolean
        get() = prefs.getBoolean("key_cat_admission", true)
        set(value) = prefs.edit().putBoolean("key_cat_admission", value).apply()

    var catRevisionEnabled: Boolean
        get() = prefs.getBoolean("key_cat_revision", true)
        set(value) = prefs.edit().putBoolean("key_cat_revision", value).apply()

    var catPlanEnabled: Boolean
        get() = prefs.getBoolean("key_cat_plan", true)
        set(value) = prefs.edit().putBoolean("key_cat_plan", value).apply()

    var catRoutineEnabled: Boolean
        get() = prefs.getBoolean("key_cat_routine", true)
        set(value) = prefs.edit().putBoolean("key_cat_routine", value).apply()

    fun dismissNotification(id: String) {
        prefs.edit().putBoolean("dismissed_$id", true).apply()
    }

    fun isNotificationDismissed(id: String): Boolean {
        return prefs.getBoolean("dismissed_$id", false)
    }

    fun snoozeNotification(id: String, untilMs: Long) {
        prefs.edit().putLong("snooze_$id", untilMs).apply()
    }

    fun isNotificationSnoozed(id: String, now: Long): Boolean {
        val until = prefs.getLong("snooze_$id", 0L)
        return now < until
    }

    var hasAskedNotificationPermission: Boolean
        get() = prefs.getBoolean("key_has_asked_notif_perm", false)
        set(value) = prefs.edit().putBoolean("key_has_asked_notif_perm", value).apply()

    fun recordNotificationDelivered(id: String, timestamp: Long = System.currentTimeMillis()) {
        prefs.edit().putLong("delivered_$id", timestamp).apply()
    }

    fun getLastDeliveredEpoch(id: String): Long {
        return prefs.getLong("delivered_$id", 0L)
    }

    fun isNotificationDeliveredRecently(id: String, cooldownMs: Long = 12 * 60 * 60 * 1000L): Boolean {
        val last = getLastDeliveredEpoch(id)
        if (last == 0L) return false
        return (System.currentTimeMillis() - last) < cooldownMs
    }

    var lastEngineEvaluationTimestamp: Long
        get() = prefs.getLong("key_last_engine_eval_time", 0L)
        set(value) = prefs.edit().putLong("key_last_engine_eval_time", value).apply()

    // Study Planning and Goals
    var dailyAvailableStudyHours: Int
        get() = prefs.getInt("key_daily_available_hours", 4)
        set(value) = prefs.edit().putInt("key_daily_available_hours", value.coerceIn(1, 14)).apply()

    var preferredStudyPeriod: String
        get() = prefs.getString("key_preferred_study_period", "ALL") ?: "ALL"
        set(value) = prefs.edit().putString("key_preferred_study_period", value).apply()

    var weeklyStudyHoursGoal: Int
        get() = prefs.getInt("key_weekly_study_hours_goal", 20)
        set(value) = prefs.edit().putInt("key_weekly_study_hours_goal", value.coerceIn(1, 80)).apply()

    var weeklyChaptersGoal: Int
        get() = prefs.getInt("key_weekly_chapters_goal", 5)
        set(value) = prefs.edit().putInt("key_weekly_chapters_goal", value.coerceIn(1, 50)).apply()

    var mockTestTargetScorePercentage: Float
        get() = prefs.getFloat("key_mock_test_target_pct", 80f)
        set(value) = prefs.edit().putFloat("key_mock_test_target_pct", value.coerceIn(10f, 100f)).apply()

    fun dismissInsight(id: String) {
        prefs.edit().putBoolean("insight_dismissed_$id", true).apply()
    }

    fun isInsightDismissed(id: String): Boolean {
        return prefs.getBoolean("insight_dismissed_$id", false)
    }

    // Timer persistence
    fun saveTimerState(
        isRunning: Boolean,
        isPaused: Boolean,
        startMs: Long,
        targetSecs: Long,
        elapsedBeforePause: Long,
        subjectId: Long?,
        chapterId: Long?
    ) {
        prefs.edit()
            .putBoolean(KEY_TIMER_RUNNING, isRunning)
            .putBoolean(KEY_TIMER_IS_PAUSED, isPaused)
            .putLong(KEY_TIMER_START_MS, startMs)
            .putLong(KEY_TIMER_TARGET_SECS, targetSecs)
            .putLong(KEY_TIMER_ELAPSED_BEFORE_PAUSE, elapsedBeforePause)
            .putLong(KEY_TIMER_SUBJECT_ID, subjectId ?: -1L)
            .putLong(KEY_TIMER_CHAPTER_ID, chapterId ?: -1L)
            .apply()
    }

    fun getTimerRunning(): Boolean = prefs.getBoolean(KEY_TIMER_RUNNING, false)
    fun getTimerPaused(): Boolean = prefs.getBoolean(KEY_TIMER_IS_PAUSED, false)
    fun getTimerStartMs(): Long = prefs.getLong(KEY_TIMER_START_MS, 0L)
    fun getTimerTargetSecs(): Long = prefs.getLong(KEY_TIMER_TARGET_SECS, 45 * 60L)
    fun getTimerElapsedBeforePause(): Long = prefs.getLong(KEY_TIMER_ELAPSED_BEFORE_PAUSE, 0L)
    fun getTimerSubjectId(): Long? {
        val id = prefs.getLong(KEY_TIMER_SUBJECT_ID, -1L)
        return if (id == -1L) null else id
    }
    fun getTimerChapterId(): Long? {
        val id = prefs.getLong(KEY_TIMER_CHAPTER_ID, -1L)
        return if (id == -1L) null else id
    }

    fun clearTimerState() {
        prefs.edit()
            .putBoolean(KEY_TIMER_RUNNING, false)
            .putBoolean(KEY_TIMER_IS_PAUSED, false)
            .putLong(KEY_TIMER_START_MS, 0L)
            .putLong(KEY_TIMER_ELAPSED_BEFORE_PAUSE, 0L)
            .apply()
    }

    // App Blocker & Distraction Control
    var blockedApps: Set<String>
        get() = prefs.getStringSet(KEY_BLOCKED_APPS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_BLOCKED_APPS, value).apply()

    var isDistractionBlockerEnabled: Boolean
        get() = prefs.getBoolean(KEY_DISTRACTION_BLOCKER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DISTRACTION_BLOCKER_ENABLED, value).apply()

    var customTimerMinutes: Int
        get() = prefs.getInt(KEY_CUSTOM_TIMER_MINUTES, 45)
        set(value) = prefs.edit().putInt(KEY_CUSTOM_TIMER_MINUTES, value.coerceIn(1, 240)).apply()

    var interruptionCount: Int
        get() = prefs.getInt(KEY_INTERRUPTION_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_INTERRUPTION_COUNT, value).apply()

    var lastBlockedPackage: String
        get() = prefs.getString(KEY_LAST_BLOCKED_APP, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_BLOCKED_APP, value).apply()

    fun recordInterruption(packageName: String) {
        interruptionCount += 1
        lastBlockedPackage = packageName
    }

    fun resetInterruptionCount() {
        interruptionCount = 0
        lastBlockedPackage = ""
    }

    // Auto-Plan task tracking
    var autoGeneratedTaskIds: Set<String>
        get() = prefs.getStringSet("key_auto_generated_task_ids", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("key_auto_generated_task_ids", value).apply()

    fun markTasksAsAutoGenerated(taskIds: List<Long>) {
        val current = autoGeneratedTaskIds.toMutableSet()
        taskIds.forEach { current.add(it.toString()) }
        autoGeneratedTaskIds = current
    }

    fun isTaskAutoGenerated(taskId: Long): Boolean {
        return autoGeneratedTaskIds.contains(taskId.toString())
    }
}
