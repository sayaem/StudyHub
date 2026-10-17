package com.example.domain.notification

import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.MistakeEntity
import com.example.data.model.PlannerTaskEntity
import com.example.data.model.RevisionItemEntity
import com.example.util.AppLanguage
import com.example.util.PreferencesManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class NotificationCategory {
    ADMISSION,
    REVISION,
    PLAN_RECOVERY,
    ROUTINE,
    SESSION
}

enum class NotificationPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

data class AdaptiveNotification(
    val id: String,
    val category: NotificationCategory,
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val priority: NotificationPriority,
    val timestamp: Long,
    val targetScreen: com.example.ui.ScreenDestination,
    val sourceId: Long,
    var isRead: Boolean = false,
    var isDismissed: Boolean = false,
    var snoozedUntil: Long = 0L
) {
    fun getTitle(lang: AppLanguage): String = if (lang == AppLanguage.BANGLA) titleBn else titleEn
    fun getDescription(lang: AppLanguage): String = if (lang == AppLanguage.BANGLA) descriptionBn else descriptionEn
}

class AdaptiveNotificationEngine(private val prefs: PreferencesManager) {

    fun evaluateNotifications(
        admissions: List<AdmissionTargetEntity>,
        chapters: List<ChapterEntity>,
        plannerTasks: List<PlannerTaskEntity>,
        revisions: List<RevisionItemEntity> = emptyList(),
        mistakes: List<MistakeEntity> = emptyList(),
        isSyllabusBehind: Boolean = false,
        syllabusBehindReasonEn: String = "",
        syllabusBehindReasonBn: String = ""
    ): List<AdaptiveNotification> {
        if (!prefs.isNotificationsEnabled || !prefs.isAdaptiveNotificationsEnabled) {
            return emptyList()
        }

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = now }
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val quietStart = prefs.quietHoursStart
        val quietEnd = prefs.quietHoursEnd
        val isQuietHours = if (quietStart > quietEnd) {
            currentHour >= quietStart || currentHour < quietEnd
        } else {
            currentHour in quietStart until quietEnd
        }

        val candidates = mutableListOf<AdaptiveNotification>()

        // 1. Admission Deadlines & Exam Dates
        if (prefs.catAdmissionEnabled) {
            for (admission in admissions) {
                // Application Deadline Reminders (7d, 3d, 1d, 0d) for pending applications
                if (admission.status == "NOT_APPLIED") {
                    val daysUntilDeadline = TimeUnit.MILLISECONDS.toDays(admission.applicationDeadline - now).toInt()
                    when {
                        daysUntilDeadline == 0 -> {
                            candidates.add(
                                AdaptiveNotification(
                                    id = "adm_dead_today_${admission.id}",
                                    category = NotificationCategory.ADMISSION,
                                    titleEn = "Application Deadline Today!",
                                    titleBn = "আবেদনের শেষ সময় আজ!",
                                    descriptionEn = "Today is the final day to submit your application for ${admission.universityName} (${admission.unit}). Don't miss it!",
                                    descriptionBn = "আজ ${admission.universityName} (${admission.unit})-এর আবেদনের শেষ দিন! দ্রুত আবেদন সম্পন্ন করুন।",
                                    priority = NotificationPriority.CRITICAL,
                                    timestamp = now,
                                    targetScreen = com.example.ui.ScreenDestination.CALENDAR,
                                    sourceId = admission.id
                                )
                            )
                        }
                        daysUntilDeadline == 1 -> {
                            candidates.add(
                                AdaptiveNotification(
                                    id = "adm_dead_1d_${admission.id}",
                                    category = NotificationCategory.ADMISSION,
                                    titleEn = "1 Day Left to Apply",
                                    titleBn = "আবেদনের শেষ সময় কাল!",
                                    descriptionEn = "Application for ${admission.universityName} closes tomorrow. Complete fee payment and submission.",
                                    descriptionBn = "${admission.universityName}-এ আবেদনের সময় আগামীকাল শেষ হবে। ফি পরিশোধ ও আবেদন নিশ্চিত করুন।",
                                    priority = NotificationPriority.CRITICAL,
                                    timestamp = now,
                                    targetScreen = com.example.ui.ScreenDestination.CALENDAR,
                                    sourceId = admission.id
                                )
                            )
                        }
                        daysUntilDeadline in 2..3 -> {
                            candidates.add(
                                AdaptiveNotification(
                                    id = "adm_dead_${daysUntilDeadline}d_${admission.id}",
                                    category = NotificationCategory.ADMISSION,
                                    titleEn = "Application Deadline in $daysUntilDeadline Days",
                                    titleBn = "আবেদনের বাকি আর $daysUntilDeadline দিন",
                                    descriptionEn = "Deadline approaching for ${admission.universityName} in $daysUntilDeadline days. Ensure documents and details are ready.",
                                    descriptionBn = "${admission.universityName}-এ আবেদনের আর মাত্র $daysUntilDeadline দিন বাকি। প্রয়োজনীয় কাগজপত্র প্রস্তুত রাখুন।",
                                    priority = NotificationPriority.HIGH,
                                    timestamp = now,
                                    targetScreen = com.example.ui.ScreenDestination.CALENDAR,
                                    sourceId = admission.id
                                )
                            )
                        }
                        daysUntilDeadline in 4..7 -> {
                            candidates.add(
                                AdaptiveNotification(
                                    id = "adm_dead_${daysUntilDeadline}d_${admission.id}",
                                    category = NotificationCategory.ADMISSION,
                                    titleEn = "Upcoming Deadline: ${admission.universityName}",
                                    titleBn = "আসন্ন আবেদন শেষ সময়: ${admission.universityName}",
                                    descriptionEn = "Application window closes in $daysUntilDeadline days (${admission.unit}).",
                                    descriptionBn = "${admission.universityName}-এ আবেদনের সময় আর $daysUntilDeadline দিন পর শেষ হবে।",
                                    priority = NotificationPriority.MEDIUM,
                                    timestamp = now,
                                    targetScreen = com.example.ui.ScreenDestination.CALENDAR,
                                    sourceId = admission.id
                                )
                            )
                        }
                    }
                }

                // Exam Date Countdown
                if (admission.status != "COMPLETED") {
                    val daysUntilExam = TimeUnit.MILLISECONDS.toDays(admission.examDate - now).toInt()
                    if (daysUntilExam in 0..30) {
                        val priority = when {
                            daysUntilExam <= 1 -> NotificationPriority.CRITICAL
                            daysUntilExam <= 7 -> NotificationPriority.HIGH
                            else -> NotificationPriority.MEDIUM
                        }
                        candidates.add(
                            AdaptiveNotification(
                                id = "adm_exam_${admission.id}",
                                category = NotificationCategory.ADMISSION,
                                titleEn = "Admission Test in $daysUntilExam Days",
                                titleBn = "ভর্তি পরীক্ষা আর $daysUntilExam দিন",
                                descriptionEn = "Your exam for ${admission.universityName} (${admission.unit}) is in $daysUntilExam days. Focus on mock tests and revision.",
                                descriptionBn = "${admission.universityName} (${admission.unit})-এর ভর্তি পরীক্ষা আর মাত্র $daysUntilExam দিন বাকি। প্রস্তুতি জোরদার করুন!",
                                priority = priority,
                                timestamp = now,
                                targetScreen = com.example.ui.ScreenDestination.CALENDAR,
                                sourceId = admission.id
                            )
                        )
                    }
                }
            }
        }

        // Mistake Book Review Reminder
        val unresolvedMistakes = mistakes.filter { it.status != "RESOLVED" }
        if (unresolvedMistakes.isNotEmpty()) {
            candidates.add(
                AdaptiveNotification(
                    id = "mistakes_unresolved_alert",
                    category = NotificationCategory.REVISION,
                    titleEn = "Unresolved Mistakes (${unresolvedMistakes.size})",
                    titleBn = "ভুলসমূহ পর্যালোচনা (${unresolvedMistakes.size})",
                    descriptionEn = "You have ${unresolvedMistakes.size} questions in your Mistake Book needing review to avoid repeating errors in mock tests.",
                    descriptionBn = "আপনার ভুল তালিকায় ${unresolvedMistakes.size}টি প্রশ্ন পর্যালোচনার অপেক্ষায় আছে। মক টেস্টের আগে দেখে নিন।",
                    priority = NotificationPriority.MEDIUM,
                    timestamp = now,
                    targetScreen = com.example.ui.ScreenDestination.MISTAKE_BOOK,
                    sourceId = 0L
                )
            )
        }

        // 2. Revision Reminders (Spaced Revisions & Chapter flags)
        if (prefs.catRevisionEnabled) {
            val overdueSpacedRevisions = revisions.filter { !it.isCompleted && it.nextRevisionDate < now }
            for (rev in overdueSpacedRevisions.take(2)) {
                candidates.add(
                    AdaptiveNotification(
                        id = "rev_spaced_${rev.id}",
                        category = NotificationCategory.REVISION,
                        titleEn = "Spaced Revision Due",
                        titleBn = "রিভিশনের সময় হয়েছে",
                        descriptionEn = "'${rev.title}' (${rev.subjectName}) is due for revision review (Step ${rev.completedCount + 1}/${rev.totalCount}).",
                        descriptionBn = "'${rev.title}' (${rev.subjectName}) রিভিশন করার সময় হয়েছে (ধাপ ${rev.completedCount + 1}/${rev.totalCount})।",
                        priority = NotificationPriority.HIGH,
                        timestamp = now,
                        targetScreen = com.example.ui.ScreenDestination.REVISION_TRACKER,
                        sourceId = rev.id
                    )
                )
            }

            val revisionChapters = chapters.filter { it.isRevisionNeeded }
            for (ch in revisionChapters.take(2)) {
                candidates.add(
                    AdaptiveNotification(
                        id = "rev_${ch.id}",
                        category = NotificationCategory.REVISION,
                        titleEn = "Revision Notice",
                        titleBn = "রিভিশন অ্যালার্ট",
                        descriptionEn = "Chapter '${ch.titleEn}' is marked for revision. Regular revision improves memory retention.",
                        descriptionBn = "'${ch.titleEn}' অধ্যায়টি রিভিশন করা প্রয়োজন। নিয়মিত রিভিশন স্মৃতিশক্তি বাড়ায়।",
                        priority = NotificationPriority.MEDIUM,
                        timestamp = now,
                        targetScreen = com.example.ui.ScreenDestination.SYLLABUS,
                        sourceId = ch.id
                    )
                )
            }
        }

        // 3. Study Plan & Syllabus Recovery Alert
        if (prefs.catPlanEnabled) {
            if (isSyllabusBehind && syllabusBehindReasonEn.isNotBlank()) {
                candidates.add(
                    AdaptiveNotification(
                        id = "plan_syllabus_behind",
                        category = NotificationCategory.PLAN_RECOVERY,
                        titleEn = "Syllabus Recovery Needed",
                        titleBn = "সিলেবাস রিকভারি প্রয়োজন",
                        descriptionEn = syllabusBehindReasonEn,
                        descriptionBn = syllabusBehindReasonBn,
                        priority = NotificationPriority.HIGH,
                        timestamp = now,
                        targetScreen = com.example.ui.ScreenDestination.SMART_PLANNER,
                        sourceId = 0L
                    )
                )
            }

            val pendingTasks = plannerTasks.filter { !it.isCompleted }
            if (pendingTasks.isNotEmpty()) {
                candidates.add(
                    AdaptiveNotification(
                        id = "plan_recovery",
                        category = NotificationCategory.PLAN_RECOVERY,
                        titleEn = "Daily Study Planner",
                        titleBn = "দৈনিক স্টাডি প্ল্যান",
                        descriptionEn = "You have ${pendingTasks.size} pending study task${if (pendingTasks.size > 1) "s" else ""} in your planner. Complete them to maintain your study streak.",
                        descriptionBn = "আপনার প্ল্যানারে ${pendingTasks.size}টি পড়া বাকি আছে। সিলেবাস এগিয়ে রাখতে সম্পন্ন করুন।",
                        priority = NotificationPriority.MEDIUM,
                        timestamp = now,
                        targetScreen = com.example.ui.ScreenDestination.SMART_PLANNER,
                        sourceId = 0L
                    )
                )
            }
        }

        // 4. Study Routine Suggestions
        if (prefs.catRoutineEnabled) {
            if (currentHour in 9..11 || currentHour in 15..17) {
                candidates.add(
                    AdaptiveNotification(
                        id = "routine_sugg_${currentHour}",
                        category = NotificationCategory.ROUTINE,
                        titleEn = "Optimal Study Time",
                        titleBn = "পড়ার উপযুক্ত সময়",
                        descriptionEn = "Studies show consistency during these hours yields higher focus. Jump into a focus session!",
                        descriptionBn = "গবেষণায় দেখা গেছে এই সময়ে মনোযোগ বেশি থাকে। ফোকাস সেশন শুরু করুন!",
                        priority = NotificationPriority.LOW,
                        timestamp = now,
                        targetScreen = com.example.ui.ScreenDestination.FOCUS,
                        sourceId = -1L
                    )
                )
            }
        }

        // Filter out dismissed, snoozed, and quiet hours (unless critical)
        val filtered = candidates.filter { candidate ->
            if (prefs.isNotificationDismissed(candidate.id)) return@filter false
            if (prefs.isNotificationSnoozed(candidate.id, now)) return@filter false
            if (isQuietHours && candidate.priority != NotificationPriority.CRITICAL) return@filter false
            true
        }

        // Enforce max daily proactive notifications (excluding critical)
        val criticalList = filtered.filter { it.priority == NotificationPriority.CRITICAL }
        val proactiveList = filtered.filter { it.priority != NotificationPriority.CRITICAL }
            .take(prefs.maxDailyNotifications)

        return (criticalList + proactiveList).sortedByDescending { it.timestamp }
    }
}
