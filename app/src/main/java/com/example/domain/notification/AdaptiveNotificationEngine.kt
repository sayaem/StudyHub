package com.example.domain.notification

import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.PlannerTaskEntity
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
        plannerTasks: List<PlannerTaskEntity>
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

        // 1. Admission Deadlines
        if (prefs.catAdmissionEnabled) {
            for (admission in admissions) {
                if (admission.status == "APPLIED" || admission.status == "COMPLETED") continue
                val daysLeft = TimeUnit.MILLISECONDS.toDays(admission.examDate - now).toInt()
                if (daysLeft in 0..30) {
                    val priority = if (daysLeft <= 3) NotificationPriority.CRITICAL else NotificationPriority.HIGH
                    candidates.add(
                        AdaptiveNotification(
                            id = "adm_${admission.id}",
                            category = NotificationCategory.ADMISSION,
                            titleEn = "Admission Deadline Alert",
                            titleBn = "ভর্তি পরীক্ষার রিমাইন্ডার",
                            descriptionEn = "Your admission exam for ${admission.universityName} is approaching in $daysLeft days. Keep up your preparation!",
                            descriptionBn = "${admission.universityName}-এর ভর্তি পরীক্ষা আর মাত্র $daysLeft দিন বাকি। প্রস্তুতি জোরদার করুন!",
                            priority = priority,
                            timestamp = now,
                            targetScreen = com.example.ui.ScreenDestination.CALENDAR,
                            sourceId = admission.id
                        )
                    )
                }
            }
        }

        // 2. Revision Reminders
        if (prefs.catRevisionEnabled) {
            val revisionChapters = chapters.filter { it.isRevisionNeeded }
            for (ch in revisionChapters) {
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

        // 3. Study Plan Recovery (Overdue or Pending Planner Tasks)
        if (prefs.catPlanEnabled) {
            val pendingTasks = plannerTasks.filter { !it.isCompleted }
            if (pendingTasks.isNotEmpty()) {
                candidates.add(
                    AdaptiveNotification(
                        id = "plan_recovery",
                        category = NotificationCategory.PLAN_RECOVERY,
                        titleEn = "Study Plan Recovery",
                        titleBn = "স্টাডি প্ল্যান আপডেট",
                        descriptionEn = "You have ${pendingTasks.size} pending study tasks in your planner. Complete them to stay on track.",
                        descriptionBn = "আপনার প্ল্যানারে ${pendingTasks.size}টি কাজ বাকি আছে। সময়মতো সম্পন্ন করুন।",
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
