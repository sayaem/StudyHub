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
}
