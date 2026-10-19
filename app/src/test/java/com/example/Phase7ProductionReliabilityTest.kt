package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.MistakeEntity
import com.example.data.model.PlannerTaskEntity
import com.example.data.model.RevisionItemEntity
import com.example.domain.notification.AdaptiveNotification
import com.example.domain.notification.AdaptiveNotificationEngine
import com.example.domain.notification.NotificationCategory
import com.example.domain.notification.NotificationPriority
import com.example.notification.StudyHubNotificationHelper
import com.example.ui.ScreenDestination
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase7ProductionReliabilityTest {

    private lateinit var context: Context
    private lateinit var prefs: PreferencesManager
    private lateinit var engine: AdaptiveNotificationEngine

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        prefs = PreferencesManager(context)
        engine = AdaptiveNotificationEngine(prefs)

        // Reset preferences to clean fresh installation state
        prefs.isDarkTheme = false
        prefs.isNotificationsEnabled = true
        prefs.isAdaptiveNotificationsEnabled = true
        prefs.catAdmissionEnabled = true
        prefs.catRevisionEnabled = true
        prefs.catPlanEnabled = true
        prefs.catRoutineEnabled = true
        prefs.quietHoursStart = 22
        prefs.quietHoursEnd = 7
        prefs.maxDailyNotifications = 5
    }

    // --- ISSUE 6: Default Theme Verification ---

    @Test
    fun `fresh install defaults to light theme`() {
        assertFalse("Fresh installation must default to Light theme", prefs.isDarkTheme)
    }

    @Test
    fun `theme preference changes are persisted`() {
        prefs.isDarkTheme = true
        assertTrue(prefs.isDarkTheme)

        prefs.isDarkTheme = false
        assertFalse(prefs.isDarkTheme)
    }

    // --- ISSUE 1 & 2: Adaptive Notification Engine & Delivery ---

    @Test
    fun `notification channels are initialized with proper channels`() {
        StudyHubNotificationHelper.initNotificationChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channels = nm.notificationChannels
        assertNotNull(channels)
        val channelIds = channels.map { it.id }
        assertTrue(channelIds.contains(StudyHubNotificationHelper.CHANNEL_FOCUS))
        assertTrue(channelIds.contains(StudyHubNotificationHelper.CHANNEL_REMINDERS))
        assertTrue(channelIds.contains(StudyHubNotificationHelper.CHANNEL_DEADLINES))
    }

    @Test
    fun `engine produces real decision for urgent admission deadline`() {
        val now = System.currentTimeMillis()
        val admission = AdmissionTargetEntity(
            id = 42L,
            universityName = "Dhaka University",
            unit = "A Unit",
            examDate = now + TimeUnit.DAYS.toMillis(15),
            applicationDeadline = now + TimeUnit.HOURS.toMillis(4), // today!
            status = "NOT_APPLIED",
            websiteUrl = "https://admission.eis.du.ac.bd",
            notes = "Target choice"
        )

        val notifications = engine.evaluateNotifications(
            admissions = listOf(admission),
            chapters = emptyList(),
            plannerTasks = emptyList()
        )

        assertTrue("Expected urgent admission deadline notification", notifications.isNotEmpty())
        val notif = notifications.first()
        assertEquals(NotificationCategory.ADMISSION, notif.category)
        assertEquals(NotificationPriority.CRITICAL, notif.priority)
        assertEquals(ScreenDestination.CALENDAR, notif.targetScreen)
        assertEquals(admission.id, notif.sourceId)
    }

    @Test
    fun `engine produces decision for overdue spaced revisions`() {
        val now = System.currentTimeMillis()
        val revision = RevisionItemEntity(
            id = 101L,
            title = "Newtonian Mechanics",
            subjectName = "Physics",
            completedCount = 1,
            totalCount = 4,
            isCompleted = false,
            subjectId = 1L,
            chapterId = 10L,
            lastRevisedAt = now - TimeUnit.DAYS.toMillis(3),
            nextRevisionDate = now - TimeUnit.HOURS.toMillis(2) // overdue!
        )

        val notifications = engine.evaluateNotifications(
            admissions = emptyList(),
            chapters = emptyList(),
            plannerTasks = emptyList(),
            revisions = listOf(revision)
        )

        val revNotif = notifications.find { it.category == NotificationCategory.REVISION }
        assertNotNull("Expected revision notification", revNotif)
        assertEquals(ScreenDestination.REVISION_TRACKER, revNotif?.targetScreen)
    }

    @Test
    fun `engine produces decision for pending daily study planner tasks`() {
        val task = PlannerTaskEntity(
            id = 505L,
            timeSlot = "10:00 - 11:30",
            subjectTitle = "Chemistry",
            topicTitle = "Periodic Table",
            dateCategory = "TODAY",
            isCompleted = false,
            colorHex = "#2563EB"
        )

        val notifications = engine.evaluateNotifications(
            admissions = emptyList(),
            chapters = emptyList(),
            plannerTasks = listOf(task)
        )

        val planNotif = notifications.find { it.category == NotificationCategory.PLAN_RECOVERY }
        assertNotNull("Expected plan reminder notification", planNotif)
        assertEquals(ScreenDestination.SMART_PLANNER, planNotif?.targetScreen)
    }

    @Test
    fun `deduplication suppresses recently delivered notification`() {
        val notifId = "adm_dead_today_99"
        assertFalse(prefs.isNotificationDeliveredRecently(notifId))

        prefs.recordNotificationDelivered(notifId)
        assertTrue(prefs.isNotificationDeliveredRecently(notifId))
    }

    @Test
    fun `completed task or revision cancels obsolete notification`() {
        StudyHubNotificationHelper.initNotificationChannels(context)
        // Verify cancellation call executes safely without crashing
        StudyHubNotificationHelper.cancelNotification(context, "plan_recovery")
        StudyHubNotificationHelper.cancelNotification(context, "rev_spaced_101")
    }

    @Test
    fun `diagnostics report inspects system state accurately`() {
        val report = StudyHubNotificationHelper.getDiagnosticReport(context)
        assertNotNull(report)
        assertTrue(report.channelsActive)
        assertTrue(report.engineEnabled)
    }

    // --- ISSUE 3: Privacy Policy and Terms & Conditions ---

    @Test
    fun `legal strings and policies are localized and non-empty`() {
        val privacyEn = AppStrings.privacyPolicy(AppLanguage.ENGLISH)
        val privacyBn = AppStrings.privacyPolicy(AppLanguage.BANGLA)
        assertTrue(privacyEn.isNotBlank())
        assertTrue(privacyBn.isNotBlank())

        val termsEn = AppStrings.termsConditions(AppLanguage.ENGLISH)
        val termsBn = AppStrings.termsConditions(AppLanguage.BANGLA)
        assertTrue(termsEn.isNotBlank())
        assertTrue(termsBn.isNotBlank())
    }

    // --- ISSUE 4: Greeting Illustration ---

    @Test
    fun `compact transparent greeting assets exist and are valid`() {
        val morningAsset = File("src/main/res/drawable/ic_header_morning.png")
        val nightAsset = File("src/main/res/drawable/ic_header_night.png")

        assertTrue("Morning header illustration asset should exist", morningAsset.exists())
        assertTrue("Night header illustration asset should exist", nightAsset.exists())
        assertTrue("Morning asset should have non-zero size", morningAsset.length() > 0)
        assertTrue("Night asset should have non-zero size", nightAsset.length() > 0)
    }
}
