package com.example

import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.PlannerTaskEntity
import com.example.data.model.RevisionItemEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.domain.intelligence.RecoveryStatus
import com.example.domain.intelligence.StudyIntelligenceEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class StudyIntelligenceTest {

    private lateinit var engine: StudyIntelligenceEngine

    @Before
    fun setUp() {
        engine = StudyIntelligenceEngine()
    }

    @Test
    fun `recovery engine handles missing admission exam without assumptions`() {
        val chapters = listOf(
            ChapterEntity(subjectId = 1L, titleEn = "Mechanics", titleBn = "বলবিদ্যা", status = "NOT_STARTED", estimatedMinutes = 90),
            ChapterEntity(subjectId = 1L, titleEn = "Thermodynamics", titleBn = "তাপগতিবিদ্যা", status = "IN_PROGRESS", estimatedMinutes = 60)
        )

        val report = engine.calculateSyllabusRecovery(chapters, emptyList(), emptyList(), dailyAvailableHours = 4)

        assertEquals(2, report.totalChapters)
        assertEquals(0, report.completedChapters)
        assertEquals(2, report.remainingChapters)
        assertEquals(150, report.totalRemainingMinutes)
        assertNull("Nearest exam days must be null when no exam is set", report.nearestExamDays)
        assertEquals(RecoveryStatus.NO_EXAM_SET, report.recoveryStatus)
        assertFalse(report.isBehind)
    }

    @Test
    fun `recovery engine calculates correct pacing and behind status when exam exists`() {
        val now = System.currentTimeMillis()
        val examDate = now + (10 * 86_400_000L) // 10 days away

        val chapters = listOf(
            ChapterEntity(subjectId = 1L, titleEn = "Mechanics", titleBn = "বলবিদ্যা", status = "COMPLETED", estimatedMinutes = 90),
            ChapterEntity(subjectId = 1L, titleEn = "Optics", titleBn = "আলোকবিজ্ঞান", status = "NOT_STARTED", priority = "HIGH", estimatedMinutes = 120),
            ChapterEntity(subjectId = 1L, titleEn = "Electrostatics", titleBn = "স্থির তড়িৎ", status = "IN_PROGRESS", priority = "HIGH", estimatedMinutes = 180),
            ChapterEntity(subjectId = 1L, titleEn = "Modern Physics", titleBn = "আধুনিক পদার্থবিজ্ঞান", status = "NOT_STARTED", priority = "MEDIUM", estimatedMinutes = 150)
        )

        val admissions = listOf(
            AdmissionTargetEntity(
                universityName = "BUET",
                unit = "Engineering",
                applicationDeadline = examDate - 86400000L,
                examDate = examDate,
                status = "APPLIED"
            )
        )

        val report = engine.calculateSyllabusRecovery(chapters, emptyList(), admissions, dailyAvailableHours = 2)

        assertEquals(4, report.totalChapters)
        assertEquals(1, report.completedChapters)
        assertEquals(3, report.remainingChapters)
        assertEquals(450, report.totalRemainingMinutes) // 120 + 180 + 150 = 450 mins = 7.5h
        assertEquals(10, report.nearestExamDays)
        assertEquals("BUET", report.examName)

        // Required daily workload: 450 mins across 10 days = 45 mins/day (0.75h/day)
        assertEquals(0.75f, report.requiredDailyHours, 0.05f)
        assertEquals(0.3f, report.requiredDailyChapters, 0.05f)
        // 0.75h <= 2h daily available -> ON_TRACK
        assertEquals(RecoveryStatus.ON_TRACK, report.recoveryStatus)
        assertFalse(report.isBehind)
        assertTrue(report.isAchievable)
    }

    @Test
    fun `recovery engine flags behind status when required daily hours exceed available hours`() {
        val now = System.currentTimeMillis()
        val examDate = now + (2 * 86_400_000L) // only 2 days away!

        val chapters = (1..6).map { i ->
            ChapterEntity(
                id = i.toLong(),
                subjectId = 1L,
                titleEn = "Chapter $i",
                titleBn = "অধ্যায় $i",
                status = "NOT_STARTED",
                priority = "HIGH",
                estimatedMinutes = 120 // 2h each = 12 hours total
            )
        }

        val admissions = listOf(
            AdmissionTargetEntity(
                universityName = "DU A Unit",
                unit = "Science",
                applicationDeadline = examDate,
                examDate = examDate,
                status = "APPLIED"
            )
        )

        // 12 hours over 2 days = 6 hours/day. User availability is only 2 hours/day -> Behind!
        val report = engine.calculateSyllabusRecovery(chapters, emptyList(), admissions, dailyAvailableHours = 2)

        assertTrue(report.isBehind)
        assertTrue(report.requiredDailyHours >= 5.5f)
        assertEquals(RecoveryStatus.CRITICALLY_BEHIND, report.recoveryStatus)
        assertEquals(5, report.prioritizedChapters.size)
    }

    @Test
    fun `deterministic daily planner balances revision and new learning with non-overlapping slots`() {
        val subjects = listOf(
            SubjectEntity(id = 1L, nameEn = "Physics", nameBn = "পদার্থবিজ্ঞান", colorHex = "#6366F1", iconName = "physics"),
            SubjectEntity(id = 2L, nameEn = "Chemistry", nameBn = "রসায়ন", colorHex = "#10B981", iconName = "chemistry")
        )

        val chapters = listOf(
            ChapterEntity(id = 10L, subjectId = 1L, titleEn = "Mechanics", titleBn = "বলবিদ্যা", status = "IN_PROGRESS", priority = "HIGH", estimatedMinutes = 90),
            ChapterEntity(id = 20L, subjectId = 2L, titleEn = "Organic", titleBn = "জৈব রসায়ন", status = "NOT_STARTED", priority = "HIGH", estimatedMinutes = 90)
        )

        val revisions = listOf(
            RevisionItemEntity(
                id = 100L,
                title = "Thermodynamics",
                subjectName = "Physics",
                subjectId = 1L,
                chapterId = 5L,
                completedCount = 0,
                totalCount = 4,
                nextRevisionDate = System.currentTimeMillis() // Due right now
            )
        )

        val suggestions = engine.generateDeterministicDailyPlan(
            subjects = subjects,
            chapters = chapters,
            revisions = revisions,
            admissions = emptyList(),
            existingTasks = emptyList(),
            dailyAvailableHours = 4,
            preferredStudyPeriod = "ALL"
        )

        assertTrue("Planner should generate study suggestions", suggestions.isNotEmpty())
        // 1 slot should be dedicated to Spaced Revision
        val revisionTask = suggestions.find { it.isRevision }
        assertNotNull("Must include due spaced revision item", revisionTask)
        assertEquals("Thermodynamics", revisionTask?.topicTitle)

        // Verify non-overlapping time slots
        val timeSlots = suggestions.map { it.timeSlot }
        assertEquals("All time slots must be unique and non-overlapping", timeSlots.toSet().size, timeSlots.size)
    }

    @Test
    fun `spaced revision intervals advance correctly`() {
        val now = System.currentTimeMillis()
        val next1 = engine.calculateNextRevisionDate(0, now)
        assertEquals(now + TimeUnit.DAYS.toMillis(1), next1)

        val next2 = engine.calculateNextRevisionDate(1, now)
        assertEquals(now + TimeUnit.DAYS.toMillis(3), next2)

        val next3 = engine.calculateNextRevisionDate(2, now)
        assertEquals(now + TimeUnit.DAYS.toMillis(7), next3)

        val next4 = engine.calculateNextRevisionDate(3, now)
        assertEquals(now + TimeUnit.DAYS.toMillis(14), next4)

        // Overdue check
        val pastDate = now - (2 * 86_400_000L)
        assertTrue(engine.isRevisionOverdue(pastDate, now))
        assertFalse(engine.isRevisionOverdue(now + 86400000L, now))
    }

    @Test
    fun `weekly review aggregates sessions and compares against target`() {
        val now = System.currentTimeMillis()
        val subjects = listOf(
            SubjectEntity(id = 1L, nameEn = "Physics", nameBn = "পদার্থবিজ্ঞান", colorHex = "#6366F1", iconName = "physics")
        )

        val sessions = listOf(
            StudySessionEntity(subjectId = 1L, durationSeconds = 3600L, timestamp = now), // 1 hour this week
            StudySessionEntity(subjectId = 1L, durationSeconds = 5400L, timestamp = now)  // 1.5 hours this week
        )

        val review = engine.calculateWeeklyReview(
            sessions = sessions,
            chapters = emptyList(),
            subjects = subjects,
            revisions = emptyList(),
            plannerTasks = emptyList(),
            weeklyStudyHoursGoal = 10,
            weeklyChaptersGoal = 4,
            now = now
        )

        assertEquals(9000L, review.actualStudySeconds) // 2.5 hours
        assertEquals(36000L, review.targetStudySeconds)
        assertEquals(10, review.studyHoursGoal)
        assertEquals(1, review.subjectBreakdown.size)
        assertEquals("Physics", review.subjectBreakdown[0].subjectNameEn)
        assertTrue(review.observationsEn.isNotEmpty())
    }
}
