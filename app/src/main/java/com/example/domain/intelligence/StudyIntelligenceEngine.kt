package com.example.domain.intelligence

import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.MistakeEntity
import com.example.data.model.MockTestEntity
import com.example.data.model.PlannerTaskEntity
import com.example.data.model.RevisionItemEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class RecoveryStatus {
    ON_TRACK,
    MODERATE_BEHIND,
    CRITICALLY_BEHIND,
    NO_EXAM_SET,
    COMPLETED
}

data class RecoveryChapterRecommendation(
    val chapterId: Long,
    val subjectId: Long,
    val titleEn: String,
    val titleBn: String,
    val subjectNameEn: String,
    val subjectNameBn: String,
    val priority: String,
    val estimatedMinutes: Int,
    val reasonEn: String,
    val reasonBn: String
)

data class SyllabusRecoveryReport(
    val totalChapters: Int,
    val completedChapters: Int,
    val remainingChapters: Int,
    val totalRemainingMinutes: Int,
    val nearestExamDays: Int?,
    val examName: String?,
    val dailyAvailableHours: Int,
    val requiredDailyChapters: Float,
    val requiredDailyHours: Float,
    val recoveryStatus: RecoveryStatus,
    val isBehind: Boolean,
    val isAchievable: Boolean,
    val headlineEn: String,
    val headlineBn: String,
    val recommendationEn: String,
    val recommendationBn: String,
    val prioritizedChapters: List<RecoveryChapterRecommendation>
)

data class DailyPlanSuggestion(
    val id: String,
    val timeSlot: String,
    val subjectTitle: String,
    val topicTitle: String,
    val dateCategory: String,
    val colorHex: String,
    val subjectId: Long?,
    val chapterId: Long?,
    val isRevision: Boolean,
    val durationMinutes: Int,
    val reasonEn: String,
    val reasonBn: String
)

data class SubjectWeeklyProgress(
    val subjectId: Long,
    val subjectNameEn: String,
    val subjectNameBn: String,
    val colorHex: String,
    val studySeconds: Long,
    val percentOfTotal: Float
)

data class WeeklyReviewReport(
    val weekStartDateFormatted: String,
    val weekEndDateFormatted: String,
    val actualStudySeconds: Long,
    val targetStudySeconds: Long,
    val studyHoursGoal: Int,
    val completedChaptersThisWeek: Int,
    val chaptersGoal: Int,
    val previousWeekStudySeconds: Long,
    val percentageChangeFromLastWeek: Float?,
    val subjectBreakdown: List<SubjectWeeklyProgress>,
    val overdueRevisionsCount: Int,
    val pendingPlannerTasksCount: Int,
    val observationsEn: List<String>,
    val observationsBn: List<String>
)

data class StudyInsight(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val type: InsightType,
    val actionableDestination: com.example.ui.ScreenDestination? = null
)

enum class InsightType {
    WARNING, SUCCESS, INFO
}

data class SubjectTestPerformance(
    val subjectId: Long?,
    val subjectNameEn: String,
    val subjectNameBn: String,
    val colorHex: String,
    val testCount: Int,
    val averageScore: Double,
    val averagePercentage: Double
)

data class MistakeCategoryBreakdown(
    val category: String,
    val count: Int,
    val percentageOfTotal: Float
)

data class AdmissionAnalyticsReport(
    val totalTestsTaken: Int,
    val overallAverageScore: Double,
    val overallAveragePercentage: Double,
    val highestScore: Double,
    val highestPercentage: Double,
    val latestTest: MockTestEntity?,
    val latestTestPercentage: Double?,
    val previousTestPercentage: Double?,
    val deltaPercentageFromPrevious: Double?,
    val targetScorePercentage: Double,
    val targetGapPercentage: Double?,
    val subjectPerformances: List<SubjectTestPerformance>,
    val weakestSubject: SubjectTestPerformance?,
    val strongestSubject: SubjectTestPerformance?,
    val weakestChapters: List<ChapterEntity>,
    val mistakeCategoryBreakdown: List<MistakeCategoryBreakdown>,
    val mostFrequentMistakeCategory: String?,
    val totalMistakesCount: Int,
    val unresolvedMistakesCount: Int,
    val resolvedMistakesCount: Int,
    val isSmallSample: Boolean
)

class StudyIntelligenceEngine {

    companion object {
        val DEFAULT_REVISION_INTERVALS = listOf(1, 3, 7, 14)
    }

    // -------------------------------------------------------------
    // 1. SMART SYLLABUS RECOVERY ENGINE
    // -------------------------------------------------------------
    fun calculateSyllabusRecovery(
        chapters: List<ChapterEntity>,
        subjects: List<SubjectEntity>,
        admissions: List<AdmissionTargetEntity>,
        dailyAvailableHours: Int = 4
    ): SyllabusRecoveryReport {
        val total = chapters.size
        val completed = chapters.count { it.status == "COMPLETED" }
        val remaining = (total - completed).coerceAtLeast(0)
        val remainingMinutes = chapters
            .filter { it.status != "COMPLETED" }
            .sumOf { if (it.estimatedMinutes > 0) it.estimatedMinutes else 60 }

        val subjectsMap = subjects.associateBy { it.id }
        val now = System.currentTimeMillis()
        val nearestExam = admissions
            .filter { it.examDate > now }
            .minByOrNull { it.examDate }

        if (total == 0) {
            return SyllabusRecoveryReport(
                totalChapters = 0,
                completedChapters = 0,
                remainingChapters = 0,
                totalRemainingMinutes = 0,
                nearestExamDays = null,
                examName = null,
                dailyAvailableHours = dailyAvailableHours,
                requiredDailyChapters = 0f,
                requiredDailyHours = 0f,
                recoveryStatus = RecoveryStatus.NO_EXAM_SET,
                isBehind = false,
                isAchievable = true,
                headlineEn = "No Chapters Added Yet",
                headlineBn = "কোনো অধ্যায় যুক্ত করা হয়নি",
                recommendationEn = "Add your academic subjects and chapters in the Syllabus tab to calculate workload and progress.",
                recommendationBn = "কাজের চাপ এবং অগ্রগতি জানতে সিলেবাস ট্যাবে বিষয় ও অধ্যায় যোগ করুন।",
                prioritizedChapters = emptyList()
            )
        }

        if (remaining == 0) {
            return SyllabusRecoveryReport(
                totalChapters = total,
                completedChapters = completed,
                remainingChapters = 0,
                totalRemainingMinutes = 0,
                nearestExamDays = nearestExam?.let { TimeUnit.MILLISECONDS.toDays(it.examDate - now).toInt().coerceAtLeast(1) },
                examName = nearestExam?.universityName,
                dailyAvailableHours = dailyAvailableHours,
                requiredDailyChapters = 0f,
                requiredDailyHours = 0f,
                recoveryStatus = RecoveryStatus.COMPLETED,
                isBehind = false,
                isAchievable = true,
                headlineEn = "Syllabus 100% Completed!",
                headlineBn = "সিলেবাস ১০০% সম্পন্ন!",
                recommendationEn = "All chapters are completed. Focus on mock exams and active revision intervals.",
                recommendationBn = "সব অধ্যায় শেষ হয়েছে। এখন মক টেস্ট ও রিভিশনে গুরুত্ব দিন।",
                prioritizedChapters = emptyList()
            )
        }

        val daysLeft = nearestExam?.let {
            TimeUnit.MILLISECONDS.toDays(it.examDate - now).toInt().coerceAtLeast(1)
        }

        if (daysLeft == null) {
            // Missing target date input
            return SyllabusRecoveryReport(
                totalChapters = total,
                completedChapters = completed,
                remainingChapters = remaining,
                totalRemainingMinutes = remainingMinutes,
                nearestExamDays = null,
                examName = null,
                dailyAvailableHours = dailyAvailableHours,
                requiredDailyChapters = 0f,
                requiredDailyHours = 0f,
                recoveryStatus = RecoveryStatus.NO_EXAM_SET,
                isBehind = false,
                isAchievable = true,
                headlineEn = "Admission Target Date Needed",
                headlineBn = "পরীক্ষার তারিখ নির্ধারণ করা প্রয়োজন",
                recommendationEn = "Add an upcoming admission test date in the Calendar tab to evaluate your pace and calculate required daily study.",
                recommendationBn = "প্রয়োজনীয় দৈনিক পড়ার সময় ও গতি নির্ধারণ করতে ক্যালেন্ডার ট্যাবে পরীক্ষার তারিখ যোগ করুন।",
                prioritizedChapters = emptyList()
            )
        }

        val requiredDailyChapters = remaining.toFloat() / daysLeft
        val requiredDailyHours = (remainingMinutes.toFloat() / 60f) / daysLeft
        val userTargetHours = dailyAvailableHours.toFloat()

        val recoveryStatus = when {
            requiredDailyHours <= userTargetHours -> RecoveryStatus.ON_TRACK
            requiredDailyHours <= userTargetHours * 1.5f && requiredDailyHours <= 8f -> RecoveryStatus.MODERATE_BEHIND
            else -> RecoveryStatus.CRITICALLY_BEHIND
        }

        val isBehind = recoveryStatus != RecoveryStatus.ON_TRACK
        val isAchievable = requiredDailyHours <= 9.0f

        // Prioritized Recovery Recommendations
        val incompleteChapters = chapters.filter { it.status != "COMPLETED" }
        val prioritizedList = incompleteChapters
            .sortedWith(
                compareByDescending<ChapterEntity> { it.status == "IN_PROGRESS" }
                    .thenByDescending { it.priority == "HIGH" }
                    .thenByDescending { it.isRevisionNeeded }
                    .thenByDescending { it.priority == "MEDIUM" }
            )
            .take(5)
            .map { ch ->
                val sub = subjectsMap[ch.subjectId]
                val reasonEn = when {
                    ch.status == "IN_PROGRESS" -> "In-progress chapter: complete underway work to maintain momentum"
                    ch.priority == "HIGH" -> "High priority topic: frequently tested in ${nearestExam.universityName}"
                    ch.isRevisionNeeded -> "Flagged for retention review"
                    else -> "Core syllabus progression"
                }
                val reasonBn = when {
                    ch.status == "IN_PROGRESS" -> "চলমান অধ্যায়: অসমাপ্ত কাজ আগে শেষ করে সিলেবাস এগিয়ে নিন"
                    ch.priority == "HIGH" -> "অতি গুরুত্বপূর্ণ অধ্যায়: ${nearestExam.universityName}-এ বারবার প্রশ্ন আসে"
                    ch.isRevisionNeeded -> "মনে রাখার জন্য সক্রিয় রিভিশন দরকার"
                    else -> "ধারাবাহিক সিলেবাস প্রস্তুতি"
                }
                RecoveryChapterRecommendation(
                    chapterId = ch.id,
                    subjectId = ch.subjectId,
                    titleEn = ch.titleEn,
                    titleBn = ch.titleBn,
                    subjectNameEn = sub?.nameEn ?: "Subject",
                    subjectNameBn = sub?.nameBn ?: "বিষয়",
                    priority = ch.priority,
                    estimatedMinutes = if (ch.estimatedMinutes > 0) ch.estimatedMinutes else 60,
                    reasonEn = reasonEn,
                    reasonBn = reasonBn
                )
            }

        val headlineEn = when (recoveryStatus) {
            RecoveryStatus.ON_TRACK -> "Syllabus on Schedule"
            RecoveryStatus.MODERATE_BEHIND -> "Slightly Behind Schedule"
            RecoveryStatus.CRITICALLY_BEHIND -> "Urgent: Behind Schedule"
            else -> "Syllabus Status"
        }

        val headlineBn = when (recoveryStatus) {
            RecoveryStatus.ON_TRACK -> "সিলেবাস সময়মতো এগোচ্ছে"
            RecoveryStatus.MODERATE_BEHIND -> "সিলেবাস কিছুটা পিছিয়ে আছে"
            RecoveryStatus.CRITICALLY_BEHIND -> "জরুরি: সিলেবাস বেশ পিছিয়ে আছে"
            else -> "সিলেবাসের অবস্থা"
        }

        val recEn = when (recoveryStatus) {
            RecoveryStatus.ON_TRACK ->
                "You have $remaining chapters (${remainingMinutes / 60}h) left for ${nearestExam.universityName} ($daysLeft days). Studying ${String.format("%.1f", requiredDailyHours)}h daily keeps you fully on track."
            RecoveryStatus.MODERATE_BEHIND ->
                "You need ${String.format("%.1f", requiredDailyHours)}h daily across $daysLeft days to complete ${nearestExam.universityName} syllabus. Increase study time by ${(requiredDailyHours - userTargetHours).coerceAtLeast(0.5f).toInt()}h or prioritize high-yield chapters below."
            RecoveryStatus.CRITICALLY_BEHIND ->
                "Current workload requires ${String.format("%.1f", requiredDailyHours)}h daily for $remaining chapters in $daysLeft days. Prioritize high-priority and in-progress chapters immediately."
            else -> ""
        }

        val recBn = when (recoveryStatus) {
            RecoveryStatus.ON_TRACK ->
                "${nearestExam.universityName} পরীক্ষার আর $daysLeft দিন বাকি এবং $remaining টি অধ্যায় বাকি। প্রতিদিন গড়ে ${String.format("%.1f", requiredDailyHours)} ঘণ্টা পড়লে সিলেবাস সময়মতো শেষ হবে।"
            RecoveryStatus.MODERATE_BEHIND ->
                "সিলেবাস শেষ করতে প্রতিদিন প্রায় ${String.format("%.1f", requiredDailyHours)} ঘণ্টা পড়া প্রয়োজন। দৈনিক পড়ার সময় কিছুটা বাড়ান অথবা নিচের গুরুত্বপূর্ণ অধ্যায়গুলোতে আগে নজর দিন।"
            RecoveryStatus.CRITICALLY_BEHIND ->
                "বাকি $remaining টি অধ্যায়ের জন্য দৈনিক ${String.format("%.1f", requiredDailyHours)} ঘণ্টা পড়া দরকার। সময় বাঁচাতে উচ্চ অগ্রাধিকারের অধ্যায়গুলো প্রথমে শেষ করুন।"
            else -> ""
        }

        return SyllabusRecoveryReport(
            totalChapters = total,
            completedChapters = completed,
            remainingChapters = remaining,
            totalRemainingMinutes = remainingMinutes,
            nearestExamDays = daysLeft,
            examName = nearestExam.universityName,
            dailyAvailableHours = dailyAvailableHours,
            requiredDailyChapters = requiredDailyChapters,
            requiredDailyHours = requiredDailyHours,
            recoveryStatus = recoveryStatus,
            isBehind = isBehind,
            isAchievable = isAchievable,
            headlineEn = headlineEn,
            headlineBn = headlineBn,
            recommendationEn = recEn,
            recommendationBn = recBn,
            prioritizedChapters = prioritizedList
        )
    }

    // -------------------------------------------------------------
    // 2. AUTOMATIC DAILY STUDY PLANNER (Deterministic)
    // -------------------------------------------------------------
    fun generateDeterministicDailyPlan(
        subjects: List<SubjectEntity>,
        chapters: List<ChapterEntity>,
        revisions: List<RevisionItemEntity>,
        admissions: List<AdmissionTargetEntity>,
        existingTasks: List<PlannerTaskEntity>,
        mistakes: List<MistakeEntity> = emptyList(),
        mockTests: List<MockTestEntity> = emptyList(),
        dateCategory: String = "TODAY",
        dailyAvailableHours: Int = 4,
        preferredStudyPeriod: String = "ALL"
    ): List<DailyPlanSuggestion> {
        if (chapters.isEmpty() && revisions.isEmpty()) return emptyList()

        val subjectsMap = subjects.associateBy { it.id }
        val now = System.currentTimeMillis()

        // Upcoming exam check
        val nearestExam = admissions
            .filter { it.examDate > now }
            .minByOrNull { it.examDate }

        // Time slots definition (non-overlapping, guaranteed 15-30m break)
        val allSlots = when (preferredStudyPeriod.uppercase()) {
            "MORNING" -> listOf(
                "07:30 - 09:00",
                "09:30 - 11:00",
                "11:15 - 12:30"
            )
            "AFTERNOON" -> listOf(
                "13:30 - 15:00",
                "15:30 - 17:00",
                "17:15 - 18:30"
            )
            "EVENING" -> listOf(
                "18:30 - 20:00",
                "20:30 - 22:00",
                "22:15 - 23:30"
            )
            else -> listOf(
                "08:30 - 10:00",
                "10:30 - 12:00",
                "14:30 - 16:00",
                "16:30 - 18:00",
                "19:30 - 21:00"
            )
        }

        // Limit number of slots according to daily available hours
        val slotCount = (dailyAvailableHours * 60 / 90).coerceIn(1, allSlots.size)

        // Filter out slots that are already occupied by existing tasks for this date
        val occupiedSlots = existingTasks.map { it.timeSlot.trim() }.toSet()
        val existingChapterIds = existingTasks.mapNotNull { it.chapterId }.toSet()

        val freeSlots = allSlots.filter { !occupiedSlots.contains(it) }
        val availableSlots = if (freeSlots.isNotEmpty()) freeSlots.take(slotCount) else allSlots.take(slotCount)

        val suggestions = mutableListOf<DailyPlanSuggestion>()

        // 1. Check for due / overdue spaced revision item (allocate at most 1 slot for revision balance)
        val dueRevision = revisions
            .filter { !it.isCompleted && it.nextRevisionDate <= now + 86400000L }
            .minByOrNull { it.nextRevisionDate }

        if (dueRevision != null && availableSlots.isNotEmpty()) {
            val slot = availableSlots[0]
            val sub = subjects.find { it.nameEn.equals(dueRevision.subjectName, ignoreCase = true) }
            val col = sub?.colorHex ?: "#10B981"
            suggestions.add(
                DailyPlanSuggestion(
                    id = "sugg_rev_${dueRevision.id}",
                    timeSlot = slot,
                    subjectTitle = dueRevision.subjectName,
                    topicTitle = dueRevision.title,
                    dateCategory = dateCategory,
                    colorHex = col,
                    subjectId = dueRevision.subjectId ?: sub?.id,
                    chapterId = dueRevision.chapterId,
                    isRevision = true,
                    durationMinutes = 60,
                    reasonEn = "Scheduled Spaced Revision (Cycle ${dueRevision.completedCount + 1}/${dueRevision.totalCount})",
                    reasonBn = "ধারাবাহিক রিভিশন (ধাপ ${dueRevision.completedCount + 1}/${dueRevision.totalCount})"
                )
            )
        }

        // 2. Select eligible pending chapters (in-progress, weak topics with mistakes, and weak mock test subjects first)
        val unresolvedMistakeChapterIds = mistakes.filter { it.status != "RESOLVED" }.mapNotNull { it.chapterId }.toSet()

        val weakSubjectIds = if (mockTests.isNotEmpty()) {
            mockTests.groupBy { it.subjectId }
                .mapNotNull { (subId, tests) ->
                    if (subId == null) null
                    else {
                        val avg = tests.map { (it.obtainedMarks - it.negativeMarks).coerceAtLeast(0.0) / it.totalMarks.coerceAtLeast(1.0) }.average()
                        subId to avg
                    }
                }
                .filter { it.second < 0.65 } // lower than 65% average
                .map { it.first }
                .toSet()
        } else emptySet()

        val candidateChapters = if (chapters.any { it.status != "COMPLETED" }) {
            chapters
                .filter { it.status != "COMPLETED" && !existingChapterIds.contains(it.id) }
                .ifEmpty { chapters.filter { it.status != "COMPLETED" } }
                .sortedWith(
                    compareByDescending<ChapterEntity> { unresolvedMistakeChapterIds.contains(it.id) }
                        .thenByDescending { weakSubjectIds.contains(it.subjectId) }
                        .thenByDescending { it.status == "IN_PROGRESS" }
                        .thenByDescending { it.priority == "HIGH" }
                        .thenByDescending { it.isRevisionNeeded }
                        .thenByDescending { it.priority == "MEDIUM" }
                )
        } else {
            // If all chapters are completed, schedule reinforcement revision on high-yield and mistake-prone chapters
            chapters
                .filter { !existingChapterIds.contains(it.id) }
                .ifEmpty { chapters }
                .sortedWith(
                    compareByDescending<ChapterEntity> { unresolvedMistakeChapterIds.contains(it.id) }
                        .thenByDescending { it.priority == "HIGH" }
                        .thenByDescending { it.isRevisionNeeded }
                )
        }

        val usedSubjectIds = mutableSetOf<Long>()
        if (dueRevision?.subjectId != null) {
            usedSubjectIds.add(dueRevision.subjectId)
        }

        val remainingSlots = if (suggestions.isNotEmpty()) availableSlots.drop(1) else availableSlots

        // Distribute remaining slots across distinct subjects
        for (slot in remainingSlots) {
            // Find best candidate from unseen subject first
            val chapter = candidateChapters.firstOrNull {
                !usedSubjectIds.contains(it.subjectId) && !suggestions.any { s -> s.chapterId == it.id }
            } ?: candidateChapters.firstOrNull {
                !suggestions.any { s -> s.chapterId == it.id }
            }

            if (chapter != null) {
                usedSubjectIds.add(chapter.subjectId)
                val sub = subjectsMap[chapter.subjectId]
                val subName = sub?.nameEn ?: "Subject"
                val subNameBn = sub?.nameBn ?: subName
                val col = sub?.colorHex ?: "#2563EB"

                val isWeakSub = weakSubjectIds.contains(chapter.subjectId)
                val hasUnresolvedMistakes = unresolvedMistakeChapterIds.contains(chapter.id)
                val isCompletedReinforce = chapter.status == "COMPLETED"

                val reasonEn = when {
                    hasUnresolvedMistakes -> "Weak area: Contains unresolved mistakes from recent tests"
                    isWeakSub -> "Priority practice: Lower scoring subject ($subName) in mock tests"
                    chapter.status == "IN_PROGRESS" -> "Underway topic: finish first to clear chapter backlog"
                    nearestExam != null && chapter.priority == "HIGH" -> "High-yield topic for ${nearestExam.universityName}"
                    isCompletedReinforce -> "Reinforcement revision: Consolidating key topic before exams"
                    chapter.priority == "HIGH" -> "High priority chapter in core syllabus"
                    else -> "Regular syllabus progression"
                }

                val reasonBn = when {
                    hasUnresolvedMistakes -> "টার্গেটেড দুর্বল টপিক: মক টেস্টে ভুল হওয়া অধ্যায় আগে সংশোধন করুন"
                    isWeakSub -> "অগ্রাধিকার অনুশীলন: মক টেস্টে ${subNameBn}-এ গড় নম্বর কম"
                    chapter.status == "IN_PROGRESS" -> "চলমান অধ্যায়: অসমাপ্ত কাজ আগে শেষ করে সিলেবাস এগিয়ে রাখুন"
                    nearestExam != null && chapter.priority == "HIGH" -> "${nearestExam.universityName}-এর অতি গুরুত্বপূর্ণ টপিক"
                    isCompletedReinforce -> "পুনরাবৃত্তি অনুশীলন: পরীক্ষার পূর্বে গুরুত্বপূর্ণ বিষয় দৃঢ়করণ"
                    chapter.priority == "HIGH" -> "সিলেবাসের শীর্ষ অগ্রাধিকারের অধ্যায়"
                    else -> "ধারাবাহিক সিলেবাস অগ্রগতি"
                }

                suggestions.add(
                    DailyPlanSuggestion(
                        id = "sugg_chap_${chapter.id}",
                        timeSlot = slot,
                        subjectTitle = subName,
                        topicTitle = chapter.titleEn,
                        dateCategory = dateCategory,
                        colorHex = col,
                        subjectId = chapter.subjectId,
                        chapterId = chapter.id,
                        isRevision = false,
                        durationMinutes = if (chapter.estimatedMinutes in 30..120) chapter.estimatedMinutes else 90,
                        reasonEn = reasonEn,
                        reasonBn = reasonBn
                    )
                )
            }
        }

        return suggestions
    }

    // -------------------------------------------------------------
    // 3. SPACED REVISION ENGINE
    // -------------------------------------------------------------
    fun calculateNextRevisionDate(
        completedCount: Int,
        baseTimestamp: Long = System.currentTimeMillis()
    ): Long {
        val days = when (completedCount) {
            0 -> 1L   // 1 day later
            1 -> 3L   // 3 days after 1st revision
            2 -> 7L   // 7 days after 2nd revision
            else -> 14L // 14 days after 3rd revision
        }
        return baseTimestamp + TimeUnit.DAYS.toMillis(days)
    }

    fun isRevisionOverdue(nextRevisionDate: Long, now: Long = System.currentTimeMillis()): Boolean {
        return nextRevisionDate < now - 3_600_000L // 1 hour grace
    }

    fun isRevisionDueToday(nextRevisionDate: Long, now: Long = System.currentTimeMillis()): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + 86_400_000L
        return nextRevisionDate in startOfDay..endOfDay
    }

    // -------------------------------------------------------------
    // 4. WEEKLY GOALS & REVIEW
    // -------------------------------------------------------------
    fun calculateWeeklyReview(
        sessions: List<StudySessionEntity>,
        chapters: List<ChapterEntity>,
        subjects: List<SubjectEntity>,
        revisions: List<RevisionItemEntity>,
        plannerTasks: List<PlannerTaskEntity>,
        weeklyStudyHoursGoal: Int = 20,
        weeklyChaptersGoal: Int = 5,
        now: Long = System.currentTimeMillis()
    ): WeeklyReviewReport {
        // Start of current week (Monday 00:00:00)
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfThisWeek = cal.timeInMillis
        val startOfLastWeek = startOfThisWeek - (7 * 86_400_000L)
        val endOfThisWeek = startOfThisWeek + (7 * 86_400_000L)

        val currentWeekSessions = sessions.filter { it.timestamp in startOfThisWeek..endOfThisWeek }
        val lastWeekSessions = sessions.filter { it.timestamp in startOfLastWeek until startOfThisWeek }

        val actualSecondsThisWeek = currentWeekSessions.sumOf { it.durationSeconds }
        val targetSecondsThisWeek = weeklyStudyHoursGoal * 3600L

        val lastWeekSeconds = lastWeekSessions.sumOf { it.durationSeconds }
        val pctChange = if (lastWeekSeconds > 0) {
            ((actualSecondsThisWeek - lastWeekSeconds).toFloat() / lastWeekSeconds.toFloat()) * 100f
        } else null

        // Completed chapters this week
        val completedChaptersThisWeek = chapters.count {
            it.status == "COMPLETED" && (it.lastRevisedAt ?: 0L) >= startOfThisWeek
        }

        // Subject Breakdown
        val subjectsMap = subjects.associateBy { it.id }
        val subjectTimeMap = mutableMapOf<Long, Long>()
        for (ses in currentWeekSessions) {
            val subId = ses.subjectId ?: -1L
            subjectTimeMap[subId] = (subjectTimeMap[subId] ?: 0L) + ses.durationSeconds
        }

        val subjectBreakdown = subjectTimeMap.map { (subId, secs) ->
            val sub = subjectsMap[subId]
            val pct = if (actualSecondsThisWeek > 0) (secs.toFloat() / actualSecondsThisWeek.toFloat()) * 100f else 0f
            SubjectWeeklyProgress(
                subjectId = subId,
                subjectNameEn = sub?.nameEn ?: "General Study",
                subjectNameBn = sub?.nameBn ?: "সাধারণ পড়াশোনা",
                colorHex = sub?.colorHex ?: "#2563EB",
                studySeconds = secs,
                percentOfTotal = pct
            )
        }.sortedByDescending { it.studySeconds }

        val overdueRevisions = revisions.count { !it.isCompleted && it.nextRevisionDate < now }
        val pendingPlanner = plannerTasks.count { !it.isCompleted }

        // Observations derived strictly from real persisted records
        val obsEn = mutableListOf<String>()
        val obsBn = mutableListOf<String>()

        val hoursCompleted = actualSecondsThisWeek / 3600f
        if (hoursCompleted >= weeklyStudyHoursGoal) {
            obsEn.add("Goal Achieved: You reached your weekly target of ${weeklyStudyHoursGoal}h.")
            obsBn.add("লক্ষ্যমাত্রা অর্জিত: আপনি সাপ্তাহিক ${weeklyStudyHoursGoal} ঘণ্টার লক্ষ্য পূরণ করেছেন।")
        } else {
            val remain = (weeklyStudyHoursGoal - hoursCompleted).coerceAtLeast(0.5f)
            obsEn.add("${String.format("%.1f", hoursCompleted)}h logged of ${weeklyStudyHoursGoal}h target (${String.format("%.1f", remain)}h remaining).")
            obsBn.add("${weeklyStudyHoursGoal} ঘণ্টার মধ্যে ${String.format("%.1f", hoursCompleted)} ঘণ্টা পড়া হয়েছে (বাকি ${String.format("%.1f", remain)} ঘণ্টা)।")
        }

        if (pctChange != null) {
            val changeSign = if (pctChange >= 0) "+${String.format("%.0f", pctChange)}%" else "${String.format("%.0f", pctChange)}%"
            obsEn.add("Study duration changed by $changeSign compared to last week.")
            obsBn.add("গত সপ্তাহের তুলনায় পড়াশোনার সময় $changeSign পরিবর্তিত হয়েছে।")
        }

        if (overdueRevisions > 0) {
            obsEn.add("$overdueRevisions revision topic${if (overdueRevisions > 1) "s are" else " is"} overdue. Prioritize review to ensure retention.")
            obsBn.add("$overdueRevisions টি রিভিশন বাকি আছে। পড়া মনে রাখতে দ্রুত রিভিশন সম্পন্ন করুন।")
        }

        val topSubject = subjectBreakdown.firstOrNull()
        if (topSubject != null && topSubject.percentOfTotal >= 60f && subjectBreakdown.size > 1) {
            obsEn.add("${topSubject.subjectNameEn} received ${String.format("%.0f", topSubject.percentOfTotal)}% of this week's study time. Consider balancing with other subjects.")
            obsBn.add("${topSubject.subjectNameBn}-এ এই সপ্তাহের ${String.format("%.0f", topSubject.percentOfTotal)}% সময় ব্যয় হয়েছে। অন্যান্য বিষয়ের প্রতিও নজর দিন।")
        }

        val dateFormat = java.text.SimpleDateFormat("dd MMM", java.util.Locale.US)
        val startFormatted = dateFormat.format(java.util.Date(startOfThisWeek))
        val endFormatted = dateFormat.format(java.util.Date(endOfThisWeek - 1000L))

        return WeeklyReviewReport(
            weekStartDateFormatted = startFormatted,
            weekEndDateFormatted = endFormatted,
            actualStudySeconds = actualSecondsThisWeek,
            targetStudySeconds = targetSecondsThisWeek,
            studyHoursGoal = weeklyStudyHoursGoal,
            completedChaptersThisWeek = completedChaptersThisWeek,
            chaptersGoal = weeklyChaptersGoal,
            previousWeekStudySeconds = lastWeekSeconds,
            percentageChangeFromLastWeek = pctChange,
            subjectBreakdown = subjectBreakdown,
            overdueRevisionsCount = overdueRevisions,
            pendingPlannerTasksCount = pendingPlanner,
            observationsEn = obsEn,
            observationsBn = obsBn
        )
    }

    // -------------------------------------------------------------
    // 5. PRACTICAL STUDY INSIGHTS
    // -------------------------------------------------------------
    fun generateInsights(
        chapters: List<ChapterEntity>,
        sessions: List<StudySessionEntity>,
        admissions: List<AdmissionTargetEntity>,
        revisions: List<RevisionItemEntity>,
        weeklyHoursGoal: Int = 20
    ): List<StudyInsight> {
        val insights = mutableListOf<StudyInsight>()
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 86_400_000L)

        // 1. Overdue Spaced Revisions
        val overdueRevisions = revisions.count { !it.isCompleted && it.nextRevisionDate < now }
        if (overdueRevisions > 0) {
            insights.add(
                StudyInsight(
                    id = "insight_overdue_revision",
                    titleEn = "Overdue Revisions ($overdueRevisions)",
                    titleBn = "রিভিশন বাকি আছে ($overdueRevisions)",
                    descriptionEn = "You have $overdueRevisions revision topic${if (overdueRevisions > 1) "s" else ""} scheduled that ${if (overdueRevisions > 1) "have" else "has"} passed review time. Regular review prevents retention loss.",
                    descriptionBn = "আপনার $overdueRevisions টি বিষয়ের রিভিশন সময় পার হয়ে গেছে। পড়া মনে রাখতে দ্রুত রিভিশন করুন।",
                    type = InsightType.WARNING,
                    actionableDestination = com.example.ui.ScreenDestination.REVISION_TRACKER
                )
            )
        }

        // 2. Study Pace vs Weekly Target
        val weeklySessions = sessions.filter { it.timestamp >= weekAgo }
        val weeklyHours = weeklySessions.sumOf { it.durationSeconds } / 3600f
        if (weeklyHours < (weeklyHoursGoal * 0.4f)) {
            insights.add(
                StudyInsight(
                    id = "insight_weekly_pace_low",
                    titleEn = "Below Weekly Target",
                    titleBn = "সাপ্তাহিক লক্ষ্যের চেয়ে কম",
                    descriptionEn = "Logged ${String.format("%.1f", weeklyHours)}h of your ${weeklyHoursGoal}h weekly target over past 7 days. Plan 1-2 focused sessions today.",
                    descriptionBn = "গত ৭ দিনে ${weeklyHoursGoal} ঘণ্টার মধ্যে ${String.format("%.1f", weeklyHours)} ঘণ্টা পড়া হয়েছে। আজ বাড়তি ফোকাস সেশনের পরিকল্পনা করুন।",
                    type = InsightType.WARNING,
                    actionableDestination = com.example.ui.ScreenDestination.SMART_PLANNER
                )
            )
        } else if (weeklyHours >= weeklyHoursGoal) {
            insights.add(
                StudyInsight(
                    id = "insight_weekly_pace_strong",
                    titleEn = "Weekly Goal Completed",
                    titleBn = "সাপ্তাহিক লক্ষ্যমাত্রা পূর্ণ",
                    descriptionEn = "Excellent consistency! You logged ${String.format("%.1f", weeklyHours)}h, meeting your ${weeklyHoursGoal}h weekly goal.",
                    descriptionBn = "দারুণ ধারাবাহিকতা! আপনি নির্ধারিত ${weeklyHoursGoal} ঘণ্টার লক্ষ্য অতিক্রম করে ${String.format("%.1f", weeklyHours)} ঘণ্টা পড়েছেন।",
                    type = InsightType.SUCCESS,
                    actionableDestination = com.example.ui.ScreenDestination.PROGRESS_ANALYTICS
                )
            )
        }

        // 3. Exam Urgency
        val urgentExam = admissions.filter { it.examDate > now }.minByOrNull { it.examDate }
        if (urgentExam != null) {
            val days = TimeUnit.MILLISECONDS.toDays(urgentExam.examDate - now).toInt()
            if (days <= 21) {
                insights.add(
                    StudyInsight(
                        id = "insight_exam_${urgentExam.id}",
                        titleEn = "${urgentExam.universityName} in $days Days",
                        titleBn = "${urgentExam.universityName} আর $days দিন পরে",
                        descriptionEn = "Final preparation window active. Allocate high focus to mock exams and error analysis.",
                        descriptionBn = "চূড়ান্ত প্রস্তুতির সময় চলছে। মক টেস্ট ও ভুল সংশোধনে সর্বোচ্চ গুরুত্ব দিন।",
                        type = if (days <= 7) InsightType.WARNING else InsightType.INFO,
                        actionableDestination = com.example.ui.ScreenDestination.CALENDAR
                    )
                )
            }
        }

        // 4. Stalled In-Progress Chapters
        val inProgressChapters = chapters.filter { it.status == "IN_PROGRESS" }
        if (inProgressChapters.size >= 4) {
            insights.add(
                StudyInsight(
                    id = "insight_stalled_chapters",
                    titleEn = "Multiple Incomplete Chapters",
                    titleBn = "একাধিক অধ্যায় চলমান",
                    descriptionEn = "You have ${inProgressChapters.size} chapters in progress simultaneously. Finishing 1-2 will reduce cognitive load.",
                    descriptionBn = "একসাথে ${inProgressChapters.size}টি অধ্যায় চলমান রয়েছে। যেকোনো ১-২টি শেষ করলে মানসিক চাপ কমবে।",
                    type = InsightType.INFO,
                    actionableDestination = com.example.ui.ScreenDestination.SYLLABUS
                )
            )
        }

        return insights
    }

    // -------------------------------------------------------------
    // 6. ADMISSION PERFORMANCE ANALYTICS
    // -------------------------------------------------------------
    fun calculateAdmissionAnalytics(
        mockTests: List<MockTestEntity>,
        mistakes: List<MistakeEntity>,
        subjects: List<SubjectEntity>,
        chapters: List<ChapterEntity>,
        targetScorePct: Double = 80.0
    ): AdmissionAnalyticsReport {
        val totalTests = mockTests.size
        val subjectsMap = subjects.associateBy { it.id }
        val chaptersMap = chapters.associateBy { it.id }

        val totalMistakes = mistakes.size
        val unresolvedMistakes = mistakes.count { it.status != "RESOLVED" }
        val resolvedMistakes = mistakes.count { it.status == "RESOLVED" }

        // Category distribution
        val categoryGroups = mistakes.groupBy { it.mistakeCategory }
        val categoryBreakdown = categoryGroups.map { (cat, list) ->
            val pct = if (totalMistakes > 0) (list.size.toFloat() / totalMistakes.toFloat()) * 100f else 0f
            MistakeCategoryBreakdown(cat, list.size, pct)
        }.sortedByDescending { it.count }

        val mostFrequentCategory = categoryBreakdown.firstOrNull()?.category

        // Weakest chapters based on unresolved mistakes
        val unresolvedByChapter = mistakes
            .filter { it.status != "RESOLVED" && it.chapterId != null }
            .groupBy { it.chapterId!! }
            .mapNotNull { (chapId, list) -> chaptersMap[chapId]?.let { it to list.size } }
            .sortedByDescending { it.second }
            .map { it.first }

        if (totalTests == 0) {
            return AdmissionAnalyticsReport(
                totalTestsTaken = 0,
                overallAverageScore = 0.0,
                overallAveragePercentage = 0.0,
                highestScore = 0.0,
                highestPercentage = 0.0,
                latestTest = null,
                latestTestPercentage = null,
                previousTestPercentage = null,
                deltaPercentageFromPrevious = null,
                targetScorePercentage = targetScorePct,
                targetGapPercentage = null,
                subjectPerformances = emptyList(),
                weakestSubject = null,
                strongestSubject = null,
                weakestChapters = unresolvedByChapter,
                mistakeCategoryBreakdown = categoryBreakdown,
                mostFrequentMistakeCategory = mostFrequentCategory,
                totalMistakesCount = totalMistakes,
                unresolvedMistakesCount = unresolvedMistakes,
                resolvedMistakesCount = resolvedMistakes,
                isSmallSample = false
            )
        }

        // Sorted by test date descending
        val sortedTests = mockTests.sortedByDescending { it.testDate }
        val latest = sortedTests.firstOrNull()
        val previous = sortedTests.getOrNull(1)

        fun calcNet(test: MockTestEntity): Double = (test.obtainedMarks - test.negativeMarks).coerceAtLeast(0.0)
        fun calcPct(test: MockTestEntity): Double =
            if (test.totalMarks > 0) (calcNet(test) / test.totalMarks) * 100.0 else 0.0

        val percentages = sortedTests.map { calcPct(it) }
        val netScores = sortedTests.map { calcNet(it) }

        val avgScore = netScores.average()
        val avgPercentage = percentages.average()
        val highScore = netScores.maxOrNull() ?: 0.0
        val highPct = percentages.maxOrNull() ?: 0.0

        val latestPct = latest?.let { calcPct(it) }
        val prevPct = previous?.let { calcPct(it) }
        val delta = if (latestPct != null && prevPct != null) latestPct - prevPct else null
        val targetGap = targetScorePct - avgPercentage

        // Subject Performances
        val subjectTestsMap = sortedTests.groupBy { it.subjectId }
        val subjectPerformances = subjectTestsMap.map { (subId, tests) ->
            val sub = subId?.let { subjectsMap[it] }
            val subNameEn = sub?.nameEn ?: if (subId == null) "Comprehensive Test" else "Subject"
            val subNameBn = sub?.nameBn ?: subNameEn
            val col = sub?.colorHex ?: "#2563EB"
            val subPcts = tests.map { calcPct(it) }
            val subNet = tests.map { calcNet(it) }
            SubjectTestPerformance(
                subjectId = subId,
                subjectNameEn = subNameEn,
                subjectNameBn = subNameBn,
                colorHex = col,
                testCount = tests.size,
                averageScore = subNet.average(),
                averagePercentage = subPcts.average()
            )
        }.sortedBy { it.averagePercentage }

        val weakestSub = subjectPerformances.firstOrNull()
        val strongestSub = subjectPerformances.lastOrNull()

        return AdmissionAnalyticsReport(
            totalTestsTaken = totalTests,
            overallAverageScore = avgScore,
            overallAveragePercentage = avgPercentage,
            highestScore = highScore,
            highestPercentage = highPct,
            latestTest = latest,
            latestTestPercentage = latestPct,
            previousTestPercentage = prevPct,
            deltaPercentageFromPrevious = delta,
            targetScorePercentage = targetScorePct,
            targetGapPercentage = targetGap,
            subjectPerformances = subjectPerformances,
            weakestSubject = weakestSub,
            strongestSubject = strongestSub,
            weakestChapters = unresolvedByChapter,
            mistakeCategoryBreakdown = categoryBreakdown,
            mostFrequentMistakeCategory = mostFrequentCategory,
            totalMistakesCount = totalMistakes,
            unresolvedMistakesCount = unresolvedMistakes,
            resolvedMistakesCount = resolvedMistakes,
            isSmallSample = totalTests in 1..2
        )
    }
}
