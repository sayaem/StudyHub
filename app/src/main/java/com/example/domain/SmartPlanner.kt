package com.example.domain

import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.SubjectEntity
import com.example.util.AppLanguage
import java.util.concurrent.TimeUnit

data class SmartPlanTask(
    val id: String,
    val chapterId: Long,
    val subjectId: Long,
    val subjectNameEn: String,
    val subjectNameBn: String,
    val subjectColorHex: String,
    val chapterTitleEn: String,
    val chapterTitleBn: String,
    val actionTypeEn: String,
    val actionTypeBn: String,
    val targetMinutes: Int,
    val priorityScore: Int,
    val reasonEn: String,
    val reasonBn: String,
    val isCompleted: Boolean = false
)

object SmartPlannerEngine {

    fun generateDailyPlan(
        subjects: List<SubjectEntity>,
        chapters: List<ChapterEntity>,
        admissions: List<AdmissionTargetEntity>,
        completedTaskIds: Set<String> = emptySet(),
        targetDailyHours: Int = 4
    ): List<SmartPlanTask> {
        if (chapters.isEmpty() || subjects.isEmpty()) return emptyList()

        val subjectsMap = subjects.associateBy { it.id }
        val now = System.currentTimeMillis()

        // Find nearest upcoming exam
        val nearestExam = admissions
            .filter { it.examDate > now }
            .minByOrNull { it.examDate }

        val daysUntilNearestExam = nearestExam?.let {
            TimeUnit.MILLISECONDS.toDays(it.examDate - now).coerceAtLeast(1)
        } ?: 60L

        val urgentMode = daysUntilNearestExam <= 45

        // Score pending or in-progress chapters
        val eligibleChapters = chapters.filter { it.status != "COMPLETED" || it.isRevisionNeeded }

        val scored = eligibleChapters.map { chapter ->
            var score = 0
            var actionEn = "Study Concept"
            var actionBn = "অধ্যায় অধ্যয়ন"
            var reasonEn = "Scheduled curriculum progression"
            var reasonBn = "ধারাবাহিক সিলেবাস প্রস্তুতি"

            // 1. Revision urgency
            if (chapter.isRevisionNeeded) {
                score += 50
                actionEn = "Active Revision"
                actionBn = "সক্রিয় রিভিশন"
                reasonEn = "Flagged for retention before upcoming exams"
                reasonBn = "ভর্তি পরীক্ষার পূর্বপ্রস্তুতির জন্য জরুরি"
            }

            // 2. In-progress chapters should be finished
            if (chapter.status == "IN_PROGRESS") {
                score += 40
                if (!chapter.isRevisionNeeded) {
                    actionEn = "Complete In-Progress"
                    actionBn = "চলমান অধ্যায় সম্পন্নকরণ"
                    reasonEn = "Finish underway chapter to maintain momentum"
                    reasonBn = "চলমান অধ্যায় শেষ করে সিলেবাস এগিয়ে রাখুন"
                }
            }

            // 3. Chapter priority weight
            when (chapter.priority) {
                "HIGH" -> score += 30
                "MEDIUM" -> score += 15
                "LOW" -> score += 5
            }

            // 4. Exam proximity boost
            if (urgentMode && chapter.priority == "HIGH") {
                score += 25
                reasonEn = "High-yield topic for ${nearestExam?.universityName ?: "Admission"}"
                reasonBn = "${nearestExam?.universityName ?: "ভর্তি পরীক্ষা"} এর জন্য অতি গুরুত্বপূর্ণ"
            }

            // Target duration between 30 and 75 mins
            val allocatedMinutes = when {
                chapter.isRevisionNeeded -> 35
                chapter.estimatedMinutes in 30..90 -> chapter.estimatedMinutes
                chapter.priority == "HIGH" -> 60
                else -> 45
            }

            val subject = subjectsMap[chapter.subjectId]

            SmartPlanTask(
                id = "plan_${chapter.id}",
                chapterId = chapter.id,
                subjectId = chapter.subjectId,
                subjectNameEn = subject?.nameEn ?: "Subject",
                subjectNameBn = subject?.nameBn ?: "বিষয়",
                subjectColorHex = subject?.colorHex ?: "#0284C7",
                chapterTitleEn = chapter.titleEn,
                chapterTitleBn = chapter.titleBn,
                actionTypeEn = actionEn,
                actionTypeBn = actionBn,
                targetMinutes = allocatedMinutes,
                priorityScore = score,
                reasonEn = reasonEn,
                reasonBn = reasonBn,
                isCompleted = completedTaskIds.contains("plan_${chapter.id}")
            )
        }

        // Sort descending by score, pick top 4 distinct subjects if possible
        val sorted = scored.sortedByDescending { it.priorityScore }
        val result = mutableListOf<SmartPlanTask>()
        val seenSubjects = mutableSetOf<Long>()

        for (task in sorted) {
            if (result.size >= 4) break
            if (!seenSubjects.contains(task.subjectId) || result.size + (sorted.size - result.size) <= 4) {
                result.add(task)
                seenSubjects.add(task.subjectId)
            }
        }

        // If not enough unique subjects, fill with highest remaining
        for (task in sorted) {
            if (result.size >= 4) break
            if (!result.any { it.id == task.id }) {
                result.add(task)
            }
        }

        return result
    }
}
