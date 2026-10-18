package com.example

import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.PlannerTaskEntity
import com.example.data.model.RevisionItemEntity
import com.example.data.model.SubjectEntity
import com.example.domain.intelligence.StudyIntelligenceEngine
import com.example.ui.CalendarHubTab
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UnifiedCalendarAndAutoPlanTest {

    private lateinit var engine: StudyIntelligenceEngine

    @Before
    fun setUp() {
        engine = StudyIntelligenceEngine()
    }

    @Test
    fun `auto planner respects existing occupied slots and avoids collisions`() {
        val subjects = listOf(
            SubjectEntity(id = 1L, nameEn = "Physics", nameBn = "পদার্থবিজ্ঞান", colorHex = "#6366F1", iconName = "physics"),
            SubjectEntity(id = 2L, nameEn = "Chemistry", nameBn = "রসায়ন", colorHex = "#10B981", iconName = "chemistry")
        )

        val chapters = listOf(
            ChapterEntity(id = 10L, subjectId = 1L, titleEn = "Mechanics", titleBn = "বলবিদ্যা", status = "NOT_STARTED", priority = "HIGH", estimatedMinutes = 90),
            ChapterEntity(id = 20L, subjectId = 2L, titleEn = "Organic Chemistry", titleBn = "জৈব রসায়ন", status = "NOT_STARTED", priority = "HIGH", estimatedMinutes = 90)
        )

        // Existing task already occupies "08:30 - 10:00"
        val existingTasks = listOf(
            PlannerTaskEntity(
                id = 1L,
                timeSlot = "08:30 - 10:00",
                subjectTitle = "Custom Study",
                topicTitle = "Self Practice",
                dateCategory = "TODAY",
                isCompleted = false,
                colorHex = "#2563EB"
            )
        )

        val suggestions = engine.generateDeterministicDailyPlan(
            subjects = subjects,
            chapters = chapters,
            revisions = emptyList(),
            admissions = emptyList(),
            existingTasks = existingTasks,
            dailyAvailableHours = 4,
            preferredStudyPeriod = "ALL"
        )

        assertTrue(suggestions.isNotEmpty())
        // None of the newly suggested time slots should collide with the existing "08:30 - 10:00" slot
        assertFalse(suggestions.any { it.timeSlot == "08:30 - 10:00" })
    }

    @Test
    fun `auto planner provides reinforcement revision when all chapters are completed`() {
        val subjects = listOf(
            SubjectEntity(id = 1L, nameEn = "Physics", nameBn = "পদার্থবিজ্ঞান", colorHex = "#6366F1", iconName = "physics")
        )

        val completedChapters = listOf(
            ChapterEntity(id = 10L, subjectId = 1L, titleEn = "Optics", titleBn = "আলোকবিজ্ঞান", status = "COMPLETED", priority = "HIGH", estimatedMinutes = 90),
            ChapterEntity(id = 20L, subjectId = 1L, titleEn = "Waves", titleBn = "তরঙ্গ", status = "COMPLETED", priority = "HIGH", estimatedMinutes = 90)
        )

        val suggestions = engine.generateDeterministicDailyPlan(
            subjects = subjects,
            chapters = completedChapters,
            revisions = emptyList(),
            admissions = emptyList(),
            existingTasks = emptyList(),
            dailyAvailableHours = 4,
            preferredStudyPeriod = "ALL"
        )

        assertTrue("Planner should schedule reinforcement revision even when all chapters are completed", suggestions.isNotEmpty())
        assertTrue(suggestions.any { it.reasonEn.contains("Reinforcement revision") || it.reasonBn.contains("পুনরাবৃত্তি") })
    }

    @Test
    fun `calendar hub tab enum contains study planner and admission calendar`() {
        val tabs = CalendarHubTab.values()
        assertEquals(2, tabs.size)
        assertTrue(tabs.contains(CalendarHubTab.STUDY_PLANNER))
        assertTrue(tabs.contains(CalendarHubTab.ADMISSION_CALENDAR))
    }
}
