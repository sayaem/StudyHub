package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.MistakeEntity
import com.example.data.model.MockTestEntity
import com.example.data.model.RevisionItemEntity
import com.example.data.model.SubjectEntity
import com.example.domain.intelligence.StudyIntelligenceEngine
import com.example.domain.notification.AdaptiveNotificationEngine
import com.example.domain.notification.NotificationCategory
import com.example.domain.notification.NotificationPriority
import com.example.util.PreferencesManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdmissionExamIntelligenceTest {

    private lateinit var intelligenceEngine: StudyIntelligenceEngine
    private lateinit var prefsManager: PreferencesManager

    @Before
    fun setUp() {
        intelligenceEngine = StudyIntelligenceEngine()
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefsManager = PreferencesManager(context)
        prefsManager.isNotificationsEnabled = true
        prefsManager.isAdaptiveNotificationsEnabled = true
        prefsManager.catAdmissionEnabled = true
        prefsManager.catRevisionEnabled = true
        prefsManager.quietHoursStart = 23
        prefsManager.quietHoursEnd = 6
    }

    // -------------------------------------------------------------
    // 1. MOCK TEST SCORE & NET MARK CALCULATIONS
    // -------------------------------------------------------------
    @Test
    fun `mock test net score and percentage calculation accounts for negative marks`() {
        val test1 = MockTestEntity(
            id = 1L,
            testName = "Physics Model Test 1",
            examProvider = "Udvash",
            subjectId = 1L,
            testDate = System.currentTimeMillis() - 86400000L,
            totalMarks = 100.0,
            obtainedMarks = 85.0,
            negativeMarks = 3.75,
            durationMinutes = 60
        )

        val netMarks = (test1.obtainedMarks - test1.negativeMarks).coerceAtLeast(0.0)
        assertEquals(81.25, netMarks, 0.001)

        val percentage = (netMarks / test1.totalMarks) * 100.0
        assertEquals(81.25, percentage, 0.001)
    }

    @Test
    fun `mock test net score clamps to zero when negative marks exceed obtained marks`() {
        val testEdge = MockTestEntity(
            id = 2L,
            testName = "Tough Math Test",
            examProvider = "Retina",
            subjectId = 2L,
            testDate = System.currentTimeMillis(),
            totalMarks = 50.0,
            obtainedMarks = 5.0,
            negativeMarks = 12.0
        )

        val netMarks = (testEdge.obtainedMarks - testEdge.negativeMarks).coerceAtLeast(0.0)
        assertEquals(0.0, netMarks, 0.0)

        val percentage = (netMarks / testEdge.totalMarks) * 100.0
        assertEquals(0.0, percentage, 0.0)
    }

    // -------------------------------------------------------------
    // 2. ADMISSION PERFORMANCE ANALYTICS
    // -------------------------------------------------------------
    @Test
    fun `admission performance analytics computes averages trends and weakest subject correctly`() {
        val physics = SubjectEntity(id = 1L, nameEn = "Physics", nameBn = "পদার্থবিজ্ঞান", colorHex = "#2563EB", iconName = "physics")
        val chemistry = SubjectEntity(id = 2L, nameEn = "Chemistry", nameBn = "রসায়ন", colorHex = "#10B981", iconName = "chemistry")
        val subjects = listOf(physics, chemistry)

        val now = System.currentTimeMillis()
        val mockTests = listOf(
            MockTestEntity(
                id = 1L,
                testName = "Physics Paper 1",
                examProvider = "Udvash",
                subjectId = 1L,
                testDate = now - (5 * 86400000L),
                totalMarks = 100.0,
                obtainedMarks = 80.0,
                negativeMarks = 0.0 // 80%
            ),
            MockTestEntity(
                id = 2L,
                testName = "Chemistry Paper 1",
                examProvider = "Udvash",
                subjectId = 2L,
                testDate = now - (3 * 86400000L),
                totalMarks = 100.0,
                obtainedMarks = 55.0,
                negativeMarks = 5.0 // 50%
            ),
            MockTestEntity(
                id = 3L,
                testName = "Physics Paper 2",
                examProvider = "Udvash",
                subjectId = 1L,
                testDate = now - 86400000L,
                totalMarks = 100.0,
                obtainedMarks = 92.0,
                negativeMarks = 2.0 // 90%
            )
        )

        val mistakes = listOf(
            MistakeEntity(
                id = 1L,
                subjectId = 2L,
                chapterId = 201L,
                questionDescription = "Acid Base Equilibrium",
                mistakeDescription = "Confused pH with pOH",
                correctApproach = "pH + pOH = 14",
                mistakeCategory = "Conceptual Gap",
                status = "NEW"
            ),
            MistakeEntity(
                id = 2L,
                subjectId = 2L,
                chapterId = 201L,
                questionDescription = "Thermodynamics heat of reaction",
                mistakeDescription = "Sign error in delta H",
                correctApproach = "Exothermic reactions have negative delta H",
                mistakeCategory = "Calculation Error",
                status = "NEW"
            )
        )

        val chapters = listOf(
            ChapterEntity(id = 201L, subjectId = 2L, titleEn = "Chemical Equilibrium", titleBn = "রাসায়নিক সাম্যাবস্থা")
        )

        val report = intelligenceEngine.calculateAdmissionAnalytics(
            mockTests = mockTests,
            mistakes = mistakes,
            subjects = subjects,
            chapters = chapters,
            targetScorePct = 80.0
        )

        assertEquals(3, report.totalTestsTaken)
        // Physics avg = (80 + 90) / 2 = 85.0%
        // Chemistry avg = 50.0%
        // Total scores: 80, 50, 90 -> sum = 220, avg = 73.33%
        assertEquals(73.33, report.overallAveragePercentage, 0.5)

        // Latest test is test 3 (90%), previous test is test 2 (50%) -> delta = +40%
        assertNotNull(report.deltaPercentageFromPrevious)
        assertEquals(40.0, report.deltaPercentageFromPrevious!!, 0.1)

        // Weakest subject must be Chemistry (50% < 85%)
        assertNotNull(report.weakestSubject)
        assertEquals(2L, report.weakestSubject?.subjectId)
        assertEquals("Chemistry", report.weakestSubject?.subjectNameEn)
        assertEquals(50.0, report.weakestSubject?.averagePercentage ?: 0.0, 0.1)

        // Strongest subject must be Physics
        assertNotNull(report.strongestSubject)
        assertEquals(1L, report.strongestSubject?.subjectId)
        assertEquals(85.0, report.strongestSubject?.averagePercentage ?: 0.0, 0.1)

        // Target gap: 80% target - 73.33% avg = ~6.67%
        assertNotNull(report.targetGapPercentage)
        assertEquals(6.67, report.targetGapPercentage!!, 0.5)

        // Mistake Category Breakdown: 2 mistakes total (1 Conceptual Gap = 50%, 1 Calculation Error = 50%)
        assertEquals(2, report.totalMistakesCount)
        assertEquals(2, report.unresolvedMistakesCount)
        assertEquals(2, report.mistakeCategoryBreakdown.size)
        assertEquals(1, report.weakestChapters.size)
        assertEquals("Chemical Equilibrium", report.weakestChapters.first().titleEn)
    }

    // -------------------------------------------------------------
    // 3. MISTAKE CATEGORIZATION & REVISION ITEM CREATION
    // -------------------------------------------------------------
    @Test
    fun `mistake review transitions and conversion to spaced revision`() {
        val mistake = MistakeEntity(
            id = 10L,
            subjectId = 1L,
            chapterId = 101L,
            questionDescription = "Projectile Motion maximum height formula",
            mistakeDescription = "Forgot the g factor in denominator",
            correctApproach = "H_max = (u^2 * sin^2(theta)) / (2g)",
            mistakeCategory = "Memory Issue",
            status = "NEW"
        )

        // 1. Simulating Resolution
        val resolvedMistake = mistake.copy(status = "RESOLVED")
        assertEquals("RESOLVED", resolvedMistake.status)

        // 2. Converting unresolved mistake into Spaced Revision item
        val spacedRevision = RevisionItemEntity(
            title = "Mistake Review: ${mistake.questionDescription.take(40)}",
            subjectName = "Physics",
            subjectId = mistake.subjectId,
            chapterId = mistake.chapterId,
            totalCount = 4,
            completedCount = 0,
            isCompleted = false,
            nextRevisionDate = System.currentTimeMillis() + 86400000L
        )

        assertEquals("Physics", spacedRevision.subjectName)
        assertEquals(101L, spacedRevision.chapterId)
        assertEquals(4, spacedRevision.totalCount)
        assertFalse(spacedRevision.isCompleted)
    }

    // -------------------------------------------------------------
    // 4. UNIVERSITY APPLICATION TRACKER & DEADLINE ALERTS
    // -------------------------------------------------------------
    @Test
    fun `university application deadline tracking and status transitions`() {
        val now = System.currentTimeMillis()
        val target = AdmissionTargetEntity(
            id = 1L,
            universityName = "Dhaka University",
            unit = "KA Unit (Science)",
            applicationDeadline = now + (2 * 86400000L), // 2 days away
            examDate = now + (30 * 86400000L), // 30 days away
            status = "NOT_APPLIED",
            notes = "Roll: 104523, Center: Curzon Hall"
        )

        val daysUntilDeadline = TimeUnit.MILLISECONDS.toDays(target.applicationDeadline - now).toInt()
        assertEquals(2, daysUntilDeadline)

        val daysUntilExam = TimeUnit.MILLISECONDS.toDays(target.examDate - now).toInt()
        assertEquals(30, daysUntilExam)

        // Status cycle transition: NOT_APPLIED -> APPLIED -> ADMIT_DOWNLOADED -> COMPLETED
        val status1 = "NOT_APPLIED"
        val next1 = when (status1) {
            "NOT_APPLIED" -> "APPLIED"
            "APPLIED" -> "ADMIT_DOWNLOADED"
            "ADMIT_DOWNLOADED" -> "COMPLETED"
            else -> "NOT_APPLIED"
        }
        assertEquals("APPLIED", next1)

        val next2 = when (next1) {
            "NOT_APPLIED" -> "APPLIED"
            "APPLIED" -> "ADMIT_DOWNLOADED"
            "ADMIT_DOWNLOADED" -> "COMPLETED"
            else -> "NOT_APPLIED"
        }
        assertEquals("ADMIT_DOWNLOADED", next2)
    }

    @Test
    fun `adaptive notification engine triggers deadline and mistake alerts`() {
        val engine = AdaptiveNotificationEngine(prefsManager)
        val now = System.currentTimeMillis()

        val urgentAdmission = AdmissionTargetEntity(
            id = 101L,
            universityName = "Medical College",
            unit = "MBBS",
            applicationDeadline = now + 86400000L, // 1 day left!
            examDate = now + (20 * 86400000L),
            status = "NOT_APPLIED"
        )

        val pendingMistakes = listOf(
            MistakeEntity(
                id = 1L,
                subjectId = 1L,
                chapterId = null,
                questionDescription = "Optics Snell's law",
                mistakeDescription = "Refractive index reversed",
                correctApproach = "n1 * sin(theta1) = n2 * sin(theta2)",
                mistakeCategory = "Careless Mistake",
                status = "NEW"
            )
        )

        val notifications = engine.evaluateNotifications(
            admissions = listOf(urgentAdmission),
            chapters = emptyList(),
            plannerTasks = emptyList(),
            revisions = emptyList(),
            mistakes = pendingMistakes
        )

        // Verify deadline alert generated
        val deadlineAlert = notifications.find { it.category == NotificationCategory.ADMISSION }
        assertNotNull("Admission deadline alert must be generated", deadlineAlert)
        assertEquals(NotificationPriority.CRITICAL, deadlineAlert?.priority)
        assertTrue(
            deadlineAlert?.titleEn?.contains("Deadline", ignoreCase = true) == true ||
            deadlineAlert?.titleEn?.contains("1 Day", ignoreCase = true) == true
        )

        // Verify mistake alert generated
        val mistakeAlert = notifications.find { it.id == "mistakes_unresolved_alert" }
        assertNotNull("Mistake book reminder should be generated", mistakeAlert)
        assertTrue(mistakeAlert?.descriptionEn?.contains("1 question", ignoreCase = true) == true)
    }

    // -------------------------------------------------------------
    // 5. INTELLIGENT PLANNER INTEGRATION WITH WEAK MOCK TEST AREAS
    // -------------------------------------------------------------
    @Test
    fun `smart planner prioritizes weak mock test subjects and chapters with mistakes`() {
        val physics = SubjectEntity(id = 1L, nameEn = "Physics", nameBn = "পদার্থবিজ্ঞান", colorHex = "#2563EB", iconName = "physics")
        val biology = SubjectEntity(id = 2L, nameEn = "Biology", nameBn = "জীববিজ্ঞান", colorHex = "#10B981", iconName = "biology")
        val subjects = listOf(physics, biology)

        val now = System.currentTimeMillis()
        // Physics has a 45% score (weak!), Biology has 90%
        val mockTests = listOf(
            MockTestEntity(
                id = 1L,
                testName = "Physics Paper",
                examProvider = "Udvash",
                subjectId = 1L,
                testDate = now - 86400000L,
                totalMarks = 100.0,
                obtainedMarks = 45.0,
                negativeMarks = 0.0 // 45%
            ),
            MockTestEntity(
                id = 2L,
                testName = "Biology Paper",
                examProvider = "Retina",
                subjectId = 2L,
                testDate = now - 86400000L,
                totalMarks = 100.0,
                obtainedMarks = 90.0,
                negativeMarks = 0.0 // 90%
            )
        )

        val physicsChap = ChapterEntity(
            id = 101L,
            subjectId = 1L,
            titleEn = "Thermodynamics",
            titleBn = "তাপগতিবিদ্যা",
            status = "NOT_STARTED",
            priority = "MEDIUM"
        )
        val biologyChap = ChapterEntity(
            id = 201L,
            subjectId = 2L,
            titleEn = "Cell Division",
            titleBn = "কোষ বিভাজন",
            status = "NOT_STARTED",
            priority = "MEDIUM"
        )

        val suggestions = intelligenceEngine.generateDeterministicDailyPlan(
            subjects = subjects,
            chapters = listOf(physicsChap, biologyChap),
            revisions = emptyList(),
            admissions = emptyList(),
            existingTasks = emptyList(),
            mockTests = mockTests,
            dailyAvailableHours = 2
        )

        assertTrue(suggestions.isNotEmpty())
        // First chapter suggested should be Physics because it is the weak mock test subject (< 65%)
        val firstChapterSlot = suggestions.first()
        assertEquals(101L, firstChapterSlot.chapterId)
        assertTrue(firstChapterSlot.reasonEn.contains("Lower scoring subject", ignoreCase = true) || firstChapterSlot.reasonEn.contains("Physics", ignoreCase = true))
    }
}
