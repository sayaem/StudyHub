package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.FocusScheduleEntity
import com.example.data.model.StudySessionEntity
import com.example.util.PreferencesManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FocusModeIntelligenceTest {

    private lateinit var prefsManager: PreferencesManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefsManager = PreferencesManager(context)
    }

    @Test
    fun `timer state persistence saves and restores correctly`() {
        prefsManager.saveTimerState(
            isRunning = true,
            isPaused = false,
            startMs = 1000L,
            targetSecs = 2700L,
            elapsedBeforePause = 300L,
            subjectId = 1L,
            chapterId = 101L
        )

        assertTrue(prefsManager.getTimerRunning())
        assertFalse(prefsManager.getTimerPaused())
        assertEquals(1000L, prefsManager.getTimerStartMs())
        assertEquals(2700L, prefsManager.getTimerTargetSecs())
        assertEquals(300L, prefsManager.getTimerElapsedBeforePause())
        assertEquals(1L, prefsManager.getTimerSubjectId())
        assertEquals(101L, prefsManager.getTimerChapterId())

        prefsManager.clearTimerState()
        assertFalse(prefsManager.getTimerRunning())
    }

    @Test
    fun `focus schedule entity holds parameters correctly`() {
        val schedule = FocusScheduleEntity(
            id = 1L,
            daysOfWeek = listOf(1, 2, 3, 4, 5),
            startTime = 540, // 09:00
            endTime = 720,   // 12:00
            restrictedApps = listOf("com.facebook.katana", "com.instagram.android"),
            isEnabled = true
        )

        assertEquals(5, schedule.daysOfWeek.size)
        assertEquals(540, schedule.startTime)
        assertEquals(720, schedule.endTime)
        assertEquals(2, schedule.restrictedApps.size)
        assertTrue(schedule.isEnabled)
    }

    @Test
    fun `study session entity calculates duration and completion correctly`() {
        val session = StudySessionEntity(
            id = 1L,
            subjectId = 1L,
            chapterId = 101L,
            durationSeconds = 2700L,
            timestamp = System.currentTimeMillis() - 2700000L,
            endTime = System.currentTimeMillis(),
            isCompleted = true,
            interruptionCount = 0,
            sessionType = "POMODORO",
            notes = "Physics: Mechanics"
        )

        assertEquals(2700L, session.durationSeconds)
        assertTrue(session.isCompleted)
        assertEquals(0, session.interruptionCount)
        assertEquals("POMODORO", session.sessionType)
    }

    @Test
    fun `blocked apps persistence and interruption counter work correctly`() {
        val testApps = setOf("com.facebook.katana", "com.instagram.android", "com.google.android.youtube")
        prefsManager.blockedApps = testApps
        prefsManager.isDistractionBlockerEnabled = true

        assertEquals(3, prefsManager.blockedApps.size)
        assertTrue(prefsManager.blockedApps.contains("com.facebook.katana"))
        assertTrue(prefsManager.isDistractionBlockerEnabled)

        // Record interruption
        prefsManager.resetInterruptionCount()
        assertEquals(0, prefsManager.interruptionCount)

        prefsManager.recordInterruption("com.facebook.katana")
        assertEquals(1, prefsManager.interruptionCount)
        assertEquals("com.facebook.katana", prefsManager.lastBlockedPackage)

        prefsManager.recordInterruption("com.instagram.android")
        assertEquals(2, prefsManager.interruptionCount)
        assertEquals("com.instagram.android", prefsManager.lastBlockedPackage)

        prefsManager.resetInterruptionCount()
        assertEquals(0, prefsManager.interruptionCount)
    }

    @Test
    fun `custom timer duration clamps between 1 and 240 minutes`() {
        prefsManager.customTimerMinutes = 50
        assertEquals(50, prefsManager.customTimerMinutes)

        prefsManager.customTimerMinutes = 0
        assertEquals(1, prefsManager.customTimerMinutes)

        prefsManager.customTimerMinutes = 500
        assertEquals(240, prefsManager.customTimerMinutes)
    }

    @Test
    fun `accessibility whitelist includes critical system packages`() {
        val whitelist = com.example.service.StudyHubAccessibilityService.SYSTEM_WHITELIST
        assertTrue(whitelist.contains("com.android.settings"))
        assertTrue(whitelist.contains("com.android.systemui"))
        assertTrue(whitelist.contains("com.android.phone"))
        assertTrue(whitelist.contains("com.google.android.dialer"))
    }

    @Test
    fun `notification channels initialize without error`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.notification.StudyHubNotificationHelper.initNotificationChannels(context)
        // Verify channel creation succeeds
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val focusChannel = notificationManager.getNotificationChannel(com.example.notification.StudyHubNotificationHelper.CHANNEL_FOCUS)
        assertNotNull(focusChannel)
        assertEquals("Focus Sessions", focusChannel.name)
    }
}
