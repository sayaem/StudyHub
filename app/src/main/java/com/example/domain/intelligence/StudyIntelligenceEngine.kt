package com.example.domain.intelligence

import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.PlannerTaskEntity
import com.example.data.model.RevisionItemEntity
import com.example.data.model.StudySessionEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class SyllabusRecoveryReport(
    val totalChapters: Int,
    val completedChapters: Int,
    val remainingChapters: Int,
    val nearestExamDays: Int?,
    val requiredDailyChapters: Float,
    val isBehind: Boolean,
    val recommendationEn: String,
    val recommendationBn: String
)

data class StudyInsight(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val type: InsightType
)

enum class InsightType {
    WARNING, SUCCESS, INFO
}

class StudyIntelligenceEngine {

    fun calculateSyllabusRecovery(
        chapters: List<ChapterEntity>,
        admissions: List<AdmissionTargetEntity>
    ): SyllabusRecoveryReport {
        val total = chapters.size
        val completed = chapters.count { it.status == "COMPLETED" }
        val remaining = total - completed

        val now = System.currentTimeMillis()
        val nearestExam = admissions
            .filter { it.examDate > now }
            .minByOrNull { it.examDate }

        val daysLeft = nearestExam?.let {
            TimeUnit.MILLISECONDS.toDays(it.examDate - now).toInt().coerceAtLeast(1)
        }

        val requiredDaily = if (daysLeft != null && daysLeft > 0) {
            remaining.toFloat() / daysLeft
        } else {
            0f
        }

        val isBehind = requiredDaily > 1.5f || (daysLeft != null && daysLeft < 10 && remaining > 15)

        val recEn = if (daysLeft != null) {
            if (isBehind) {
                "You have $remaining chapters left and $daysLeft days until ${nearestExam?.universityName}. Aim for ${String.format("%.1f", requiredDaily)} chapters daily to stay on track."
            } else {
                "You are progressing well towards ${nearestExam?.universityName}! $remaining chapters remaining across $daysLeft days."
            }
        } else {
            "Add an admission target date to get personalized daily pacing recommendations."
        }

        val recBn = if (daysLeft != null) {
            if (isBehind) {
                "আপনার ${nearestExam?.universityName} পরীক্ষার আর $daysLeft দিন বাকি এবং $remaining অধ্যায় বাকি। সময়মতো শেষ করতে প্রতিদিন প্রায় ${String.format("%.1f", requiredDaily)}টি অধ্যায় পড়ুন।"
            } else {
                "আপনার প্রস্তুতি ভালো চলছে! ${nearestExam?.universityName}-এর জন্য আরও $remaining টি অধ্যায় বাকি আছে।"
            }
        } else {
            "ব্যক্তিগত রুটিন পেতে ভর্তি পরীক্ষার তারিখ যোগ করুন।"
        }

        return SyllabusRecoveryReport(
            totalChapters = total,
            completedChapters = completed,
            remainingChapters = remaining,
            nearestExamDays = daysLeft,
            requiredDailyChapters = requiredDaily,
            isBehind = isBehind,
            recommendationEn = recEn,
            recommendationBn = recBn
        )
    }

    fun generateInsights(
        chapters: List<ChapterEntity>,
        sessions: List<StudySessionEntity>,
        admissions: List<AdmissionTargetEntity>
    ): List<StudyInsight> {
        val insights = mutableListOf<StudyInsight>()
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 86_400_000L)

        // Revision Check
        val overdueRevisions = chapters.count { it.isRevisionNeeded }
        if (overdueRevisions > 0) {
            insights.add(
                StudyInsight(
                    id = "insight_revision",
                    titleEn = "Overdue Revisions",
                    titleBn = "রিভিশন বাকি আছে",
                    descriptionEn = "You have $overdueRevisions chapters flagged for revision. Regular review boosts memory retention.",
                    descriptionBn = "আপনার $overdueRevisions টি অধ্যায় রিভিশন করা প্রয়োজন। নিয়মিত রিভিশন স্মৃতিশক্তি বাড়ায়।",
                    type = InsightType.WARNING
                )
            )
        }

        // Study Time Check
        val recentSessions = sessions.filter { it.timestamp >= weekAgo }
        val weeklyHours = recentSessions.sumOf { it.durationSeconds } / 3600f
        if (weeklyHours < 5f) {
            insights.add(
                StudyInsight(
                    id = "insight_hours",
                    titleEn = "Low Study Time This Week",
                    titleBn = "এই সপ্তাহে পড়ার সময় কম",
                    descriptionEn = "You have recorded ${String.format("%.1f", weeklyHours)} hours this week. Try to increase daily focus sessions.",
                    descriptionBn = "এই সপ্তাহে আপনি ${String.format("%.1f", weeklyHours)} ঘণ্টা পড়েছেন। প্রতিদিন ফোকাস সেশন বাড়ানোর চেষ্টা করুন।",
                    type = InsightType.WARNING
                )
            )
        } else {
            insights.add(
                StudyInsight(
                    id = "insight_hours_good",
                    titleEn = "Good Weekly Pace",
                    titleBn = "ভালো পড়ার গতি",
                    descriptionEn = "Great job! You have logged ${String.format("%.1f", weeklyHours)} study hours over the past 7 days.",
                    descriptionBn = "দারুণ! গত ৭ দিনে আপনি ${String.format("%.1f", weeklyHours)} ঘণ্টা পড়াশোনা করেছেন।",
                    type = InsightType.SUCCESS
                )
            )
        }

        // Admission Exam Urgency
        val urgentExam = admissions.filter { it.examDate > now }.minByOrNull { it.examDate }
        if (urgentExam != null) {
            val days = TimeUnit.MILLISECONDS.toDays(urgentExam.examDate - now).toInt()
            if (days <= 14) {
                insights.add(
                    StudyInsight(
                        id = "insight_exam_${urgentExam.id}",
                        titleEn = "Exam Approaching Soon!",
                        titleBn = "খুব শীঘ্রই পরীক্ষা!",
                        descriptionEn = "${urgentExam.universityName} is in $days days. Focus on mock tests and revision.",
                        descriptionBn = "${urgentExam.universityName} পরীক্ষা আর মাত্র $days দিন পরে। এখন মক টেস্ট ও রিভিশনে জোর দিন।",
                        type = InsightType.WARNING
                    )
                )
            }
        }

        return insights
    }

    fun calculateNextRevisionDate(completedCount: Int): Long {
        val days = when (completedCount) {
            0 -> 1L
            1 -> 3L
            2 -> 7L
            else -> 14L
        }
        return System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days)
    }

    fun generatePlan(
        pendingChapters: List<ChapterEntity>,
        availableTimeHours: Float
    ): List<PlannerTaskEntity> {
        val tasks = mutableListOf<PlannerTaskEntity>()
        var remainingTime = availableTimeHours

        for (ch in pendingChapters) {
            val estimatedHours = ch.estimatedMinutes.toFloat() / 60f
            if (remainingTime >= estimatedHours) {
                tasks.add(
                    PlannerTaskEntity(
                        timeSlot = "09:00 - ${9 + estimatedHours.toInt()}:00",
                        subjectTitle = "Study Plan",
                        topicTitle = ch.titleEn,
                        dateCategory = "TODAY",
                        chapterId = ch.id
                    )
                )
                remainingTime -= estimatedHours
            }
        }
        return tasks
    }
}

