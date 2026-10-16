package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    BANGLA("bn", "বাংলা")
}

object AppStrings {
    fun toBengaliDigits(input: String): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatNumber(number: Number, lang: AppLanguage): String {
        val str = number.toString()
        return if (lang == AppLanguage.BANGLA) toBengaliDigits(str) else str
    }

    fun formatDuration(totalSeconds: Long, lang: AppLanguage): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            if (lang == AppLanguage.BANGLA) {
                "${toBengaliDigits(hours.toString())} ঘণ্টা ${toBengaliDigits(minutes.toString())} মি."
            } else {
                "${hours}h ${minutes}m"
            }
        } else if (minutes > 0) {
            if (lang == AppLanguage.BANGLA) {
                "${toBengaliDigits(minutes.toString())} মি. ${toBengaliDigits(seconds.toString())} সে."
            } else {
                "${minutes}m ${seconds}s"
            }
        } else {
            if (lang == AppLanguage.BANGLA) {
                "${toBengaliDigits(seconds.toString())} সেকেন্ড"
            } else {
                "${seconds}s"
            }
        }
    }

    fun formatDate(timestampMs: Long, lang: AppLanguage): String {
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
        val formatted = sdf.format(Date(timestampMs))
        return if (lang == AppLanguage.BANGLA) toBengaliDigits(formatted) else formatted
    }

    // Navigation Labels
    fun navHome(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "হোম" else "Home"
    fun navSyllabus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সিলেবাস" else "Syllabus"
    fun navFocus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ফোকাস" else "Focus"
    fun navCalendar(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ক্যালেন্ডার" else "Calendar"
    fun navProfile(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রোফাইল" else "Profile"

    // Dashboard strings
    fun dashboardGreeting(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুভ প্রস্তুতি!" else "Ready to Conquer!"
    fun dashboardSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিশ্ববিদ্যালয় ভর্তি পরীক্ষার দৈনিক ট্র্যাকার" else "University Admission Prep Tracker"
    fun todayStudyTime(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আজকের পড়াশোনা" else "Today's Study Time"
    fun dailyGoal(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "দৈনিক লক্ষ্য" else "Daily Goal"
    fun syllabusCompletion(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সিলেবাস অগ্রগতি" else "Syllabus Progress"
    fun remainingChapters(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অধ্যায় বাকি" else "Chapters Left"
    fun completedChapters(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সম্পন্ন হয়েছে" else "Completed"
    fun upcomingExams(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আসন্ন ভর্তি পরীক্ষা" else "Upcoming Admission Exams"
    fun daysLeft(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "দিন বাকি" else "days left"
    fun hoursLeft(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ঘণ্টা" else "hrs"
    fun todaySmartPlan(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আজকের স্মার্ট প্ল্যান" else "Today's Smart Plan"
    fun quickActions(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কুইক অ্যাকশন" else "Quick Actions"
    fun startFocus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পড়াশোনা শুরু" else "Start Focus"
    fun addChapter(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অধ্যায় যোগ" else "Add Chapter"
    fun addExam(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পরীক্ষা যোগ" else "Add Exam"
    fun generatePlan(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্ল্যান আপডেট" else "Update Plan"

    // Status Strings
    fun statusNotStarted(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুরু হয়নি" else "Not Started"
    fun statusInProgress(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "চলমান" else "In Progress"
    fun statusCompleted(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সম্পন্ন" else "Completed"

    fun priorityHigh(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "উচ্চ অগ্রাধিকার" else "High Priority"
    fun priorityMedium(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মাঝারি" else "Medium"
    fun priorityLow(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সাধারণ" else "Low"

    fun needsRevision(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রিভিশন প্রয়োজন" else "Needs Revision"
    fun markRevised(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রিভাইসড মার্ক করুন" else "Mark as Revised"
    fun revisionCount(count: Int, lang: AppLanguage): String {
        val num = formatNumber(count, lang)
        return if (lang == AppLanguage.BANGLA) "$num বার রিভিশন" else "$num revs"
    }

    // Focus Timer
    fun timerFocus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ফোকাস টাইমার" else "Focus Timer"
    fun timerPomodoro(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পোমোডোরো (২৫ মি.)" else "Pomodoro (25m)"
    fun timerStopwatch(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্টপওয়াচ (ফ্রি)" else "Stopwatch (Free)"
    fun selectSubject(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিষয় নির্বাচন করুন" else "Select Subject"
    fun startTimer(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুরু করুন" else "Start Session"
    fun pauseTimer(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিরতি" else "Pause"
    fun resumeTimer(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পুনরায় শুরু" else "Resume"
    fun finishSession(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সেশন শেষ করুন" else "Finish Session"
    fun resetTimer(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রিসেট" else "Reset"

    // Calendar
    fun admissionCalendar(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভর্তি ক্যালেন্ডার ও ডেডলাইন" else "Admission Calendar & Deadlines"
    fun appDeadline(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আবেদনের শেষ সময়" else "Application Deadline"
    fun examDate(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পরীক্ষার তারিখ" else "Exam Date"
    fun appStatus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আবেদনের অবস্থা" else "Application Status"
    fun statusNotApplied(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আবেদন করা হয়নি" else "Not Applied"
    fun statusApplied(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আবেদন সম্পন্ন" else "Applied"
    fun statusAdmitDownloaded(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাডমিট ডাউনলোড" else "Admit Downloaded"

    // Analytics & Profile
    fun analyticsTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্টাডি অ্যানালিটিক্স" else "Study Analytics"
    fun totalStudyHours(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সর্বমোট অধ্যয়ন" else "Total Study Time"
    fun consistencyStreak(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "টানা অধ্যয়নের ধারা" else "Study Consistency Streak"
    fun daysStreak(count: Int, lang: AppLanguage): String {
        val num = formatNumber(count, lang)
        return if (lang == AppLanguage.BANGLA) "$num দিন ধারাবাহিক 🔥" else "$num Days Streak 🔥"
    }
    fun subjectDistribution(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিষয়ভিত্তিক সময় বণ্টন" else "Subject Time Distribution"
    fun weeklyOverview(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সাপ্তাহিক চিত্র" else "Weekly Overview"
    fun switchLanguage(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভাষা পরিবর্তন (Language)" else "App Language (ভাষা)"
}
