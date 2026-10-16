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

    fun formatDurationShort(totalSeconds: Long, lang: AppLanguage): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return if (hours > 0) {
            if (lang == AppLanguage.BANGLA) {
                "${toBengaliDigits(hours.toString())}ঘ ${toBengaliDigits(minutes.toString())}মি"
            } else {
                "${hours}h ${minutes}m"
            }
        } else {
            if (lang == AppLanguage.BANGLA) {
                "${toBengaliDigits(minutes.toString())} মিনিট"
            } else {
                "${minutes}m"
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
    fun viewAll(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সব দেখুন" else "View All"
    fun continueLearning(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পড়াশোনা চালিয়ে যান" else "Continue Learning"
    fun nextExamCountdown(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পরবর্তী পরীক্ষা কাউন্টডাউন" else "Next Exam Countdown"
    fun targetDays(days: Int, lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "${toBengaliDigits(days.toString())} দিন বাকি" else "$days days left"

    // Syllabus
    fun syllabusTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সিলেবাস" else "Syllabus"
    fun allSubjects(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সকল বিষয়" else "All Subjects"
    fun bySubject(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিষয়ভিত্তিক" else "By Subject"
    fun byStatus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অবস্থা অনুযায়ী" else "By Status"
    fun allPill(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সব" else "All"
    fun allStatusesPill(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সকল অবস্থা" else "All Statuses"
    fun chaptersCount(completed: Int, total: Int, lang: AppLanguage): String {
        val c = formatNumber(completed, lang)
        val t = formatNumber(total, lang)
        return if (lang == AppLanguage.BANGLA) "$c / $t অধ্যায়" else "$c / $t chapters"
    }
    fun emptySubjectsTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো বিষয় যোগ করা হয়নি" else "No Subjects Added Yet"
    fun emptySubjectsDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভর্তি পরীক্ষার বিষয় যোগ করে অধ্যায় এবং প্রস্তুতি ট্র্যাক করুন।" else "Add your admission subjects to track chapters, topics, and study progress."
    fun addSubjectAction(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিষয় যোগ করুন" else "Add Subject"
    fun createSubjectTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিষয় যোগ করুন" else "Add Subject"
    fun createSubjectSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নতুন বিষয়ের নাম ও রঙ নির্বাচন করুন" else "Enter subject name and choose color theme"
    fun subjectNameLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিষয়ের নাম (যেমনঃ পদার্থবিজ্ঞান)" else "Subject Name (e.g., Physics, Math)"
    fun subjectNameBnLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাংলা নাম (ঐচ্ছিক)" else "Bangla Name (Optional)"
    fun subjectColorLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিষয়ের রঙ" else "Subject Color"
    fun subjectIconLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আইকন" else "Icon"
    fun previewLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রিভিউ" else "Card Preview"
    fun createSubjectButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিষয় তৈরি করুন" else "Create Subject"
    fun saveChanges(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সংরক্ষণ করুন" else "Save Changes"
    fun cancelAction(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাতিল" else "Cancel"
    fun deleteAction(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মুছুন" else "Delete"
    fun editAction(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সম্পাদনা" else "Edit"

    // Subject Details
    fun chaptersTab(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অধ্যায়সমূহ" else "Chapters"
    fun overviewTab(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সারসংক্ষেপ" else "Overview"
    fun emptyChaptersTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো অধ্যায় নেই" else "No Chapters Added Yet"
    fun emptyChaptersDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এই বিষয়ের জন্য প্রথম অধ্যায় যোগ করুন।" else "Add the first chapter for this subject to start tracking progress."
    fun deleteSubjectPrompt(name: String, lang: AppLanguage) =
        if (lang == AppLanguage.BANGLA) "\"$name\" বিষয়টি এবং এর সকল অধ্যায় মুছে ফেলতে চান?" else "Are you sure you want to delete \"$name\" and all its chapters?"
    fun deleteChapterPrompt(title: String, lang: AppLanguage) =
        if (lang == AppLanguage.BANGLA) "\"$title\" অধ্যায়টি মুছে ফেলতে চান?" else "Are you sure you want to delete chapter \"$title\"?"

    // Chapter Creation & Edit
    fun createChapterTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অধ্যায় যোগ করুন" else "Add Chapter"
    fun editChapterTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অধ্যায় সম্পাদনা" else "Edit Chapter"
    fun chapterTitleLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অধ্যায়ের শিরোনাম (যেমনঃ ভেক্টর)" else "Chapter Title (e.g., Vector)"
    fun chapterTitleBnLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বাংলা শিরোনাম (ঐচ্ছিক)" else "Bangla Title (Optional)"
    fun targetHoursLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আনুমানিক অধ্যয়ন সময় (ঘণ্টা)" else "Target Study Duration (hours)"
    fun priorityLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অগ্রাধিকার" else "Priority"
    fun selectSubjectLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিষয় নির্বাচন করুন" else "Select Subject"
    fun createChapterButton(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অধ্যায় তৈরি করুন" else "Create Chapter"

    // Validation Errors
    fun errEmptySubjectName(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিষয়ের নাম খালি রাখা যাবে না" else "Subject name cannot be empty"
    fun errDuplicateSubject(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এই বিষয়ের নামটি ইতিমধ্যে রয়েছে" else "A subject with this name already exists"
    fun errEmptyChapterTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অধ্যায়ের নাম আবশ্যক" else "Chapter title is required"
    fun errSelectSubject(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "একটি বিষয় নির্বাচন করুন" else "Please select a subject"
    fun errEmptyUniversity(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিশ্ববিদ্যালয়ের নাম আবশ্যক" else "University name is required"

    // Status Strings
    fun statusNotStarted(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুরু হয়নি" else "Not Started"
    fun statusInProgress(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "চলমান" else "In Progress"
    fun statusCompleted(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সম্পন্ন" else "Completed"

    fun priorityHigh(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "উচ্চ অগ্রাধিকার" else "High Priority"
    fun priorityMedium(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মাঝারি" else "Medium"
    fun priorityLow(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সাধারণ" else "Low"

    // Smart Planner
    fun plannerTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্মার্ট প্ল্যানার" else "Smart Planner"
    fun tabToday(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আজ" else "Today"
    fun tabTomorrow(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আগামীকাল" else "Tomorrow"
    fun tabThisWeek(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এই সপ্তাহ" else "This Week"
    fun emptyPlannerTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো প্ল্যান নির্ধারিত নেই" else "No Study Tasks Scheduled"
    fun emptyPlannerDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আজকের জন্য নতুন পড়ার বিষয় যোগ করুন অথবা স্বয়ংক্রিয় প্ল্যান তৈরি করুন।" else "Add a study task or generate an automated smart plan based on your syllabus."
    fun addTask(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "টাস্ক যোগ করুন" else "Add Task"
    fun smartSuggestionsTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্মার্ট পরামর্শ" else "Smart Suggestions"
    fun motivationalPrompt(completed: Int, total: Int, lang: AppLanguage): String {
        return if (total == 0) {
            if (lang == AppLanguage.BANGLA) "আপনার প্রথম স্টাডি টাস্ক যোগ করে দিনটি শুরু করুন!" else "Start your day by scheduling your first study session!"
        } else if (completed == total) {
            if (lang == AppLanguage.BANGLA) "অসাধারণ! আজকের সব লক্ষ্য সম্পন্ন হয়েছে 🎉" else "Fantastic work! All today's targets completed 🎉"
        } else {
            val c = formatNumber(completed, lang)
            val t = formatNumber(total, lang)
            if (lang == AppLanguage.BANGLA) "আজকের $t টি টাস্কের মধ্যে $c টি সম্পন্ন! এগিয়ে চলুন 💪" else "$c of $t tasks completed today! Keep going 💪"
        }
    }

    // Revision Tracker
    fun revisionTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রিভিশন ট্র্যাকার" else "Revision Tracker"
    fun tabPending(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রিভিশন বাকি" else "Pending"
    fun tabCompletedRevs(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সম্পন্ন" else "Completed"
    fun addRevisionTopic(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রিভিশন যোগ করুন" else "Add Revision"
    fun emptyRevisionTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো রিভিশন বাকি নেই" else "No Topics Need Revision"
    fun emptyRevisionDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "যে টপিকগুলো নিয়মিত ঝালাই করা প্রয়োজন তা এখানে যোগ করুন।" else "Add topics here that you need to revise periodically before admission exams."
    fun revisionCount(count: Int, lang: AppLanguage): String {
        val num = formatNumber(count, lang)
        return if (lang == AppLanguage.BANGLA) "$num বার রিভিশন" else "$num revs"
    }
    fun markRevised(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রিভাইসড মার্ক করুন" else "Mark Revised"
    fun needsRevision(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রিভিশন প্রয়োজন" else "Needs Revision"

    // Focus Timer
    fun timerFocus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ফোকাস টাইমার" else "Study Focus Timer"
    fun timerDurationPreset(mins: Int, lang: AppLanguage): String {
        val m = formatNumber(mins, lang)
        return if (lang == AppLanguage.BANGLA) "$m মিনিট" else "${mins}m"
    }
    fun selectSubject(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিষয় নির্বাচন করুন" else "Select Subject"
    fun selectChapter(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অধ্যায় নির্বাচন করুন" else "Select Chapter (Optional)"
    fun startTimer(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শুরু করুন" else "Start Session"
    fun pauseTimer(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিরতি" else "Pause"
    fun resumeTimer(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পুনরায় শুরু" else "Resume"
    fun restartTimer(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রিস্টার্ট" else "Restart"
    fun stopTimer(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "শেষ করুন" else "Finish"
    fun sessionProgress(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সেশনের অগ্রগতি" else "Session Progress"
    fun todayTotalStudy(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আজকের মোট অধ্যয়ন" else "Today's Total Study"
    fun sessionSavedBanner(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্টাডি সেশন সফলভাবে সংরক্ষিত হয়েছে!" else "Study session successfully recorded!"

    // Admission Calendar
    fun admissionCalendar(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভর্তি ক্যালেন্ডার ও ডেডলাইন" else "Admission Calendar & Deadlines"
    fun addTargetUni(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিশ্ববিদ্যালয় যোগ করুন" else "Add University"
    fun editTargetUni(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিশ্ববিদ্যালয় সম্পাদনা" else "Edit University"
    fun emptyCalendarTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোনো বিশ্ববিদ্যালয় যোগ করা হয়নি" else "No Universities Added Yet"
    fun emptyCalendarDesc(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনার লক্ষ্য বিশ্ববিদ্যালয় ও পরীক্ষার তারিখ যোগ করে দিন গণনা ট্র্যাক করুন।" else "Add your target universities and exam dates to keep track of critical deadlines."
    fun appDeadline(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আবেদনের শেষ সময়" else "Application Deadline"
    fun examDate(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পরীক্ষার তারিখ" else "Exam Date"
    fun appStatus(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আবেদনের অবস্থা" else "Application Status"
    fun statusNotApplied(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আবেদন বাকি" else "Not Applied"
    fun statusApplied(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আবেদন সম্পন্ন" else "Applied"
    fun statusAdmitDownloaded(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাডমিট সংগ্রহ" else "Admit Downloaded"
    fun daysRemaining(days: Int, lang: AppLanguage): String {
        return if (days < 0) {
            val d = formatNumber(-days, lang)
            if (lang == AppLanguage.BANGLA) "$d দিন পূর্বে সমাপ্ত" else "$d days ago"
        } else if (days == 0) {
            if (lang == AppLanguage.BANGLA) "আজ পরীক্ষা!" else "Today!"
        } else {
            val d = formatNumber(days, lang)
            if (lang == AppLanguage.BANGLA) "$d দিন বাকি" else "$d days left"
        }
    }

    // Analytics
    fun analyticsTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্টাডি অ্যানালিটিক্স" else "Progress Analytics"
    fun filter7Days(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "৭ দিন" else "7 Days"
    fun filter30Days(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "৩০ দিন" else "30 Days"
    fun filter3Months(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "৩ মাস" else "3 Months"
    fun totalStudyHours(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সর্বমোট অধ্যয়ন" else "Total Study Time"
    fun dailyAverage(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "দৈনিক গড়" else "Daily Average"
    fun weeklyOverview(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্টাডি সময় ও তুলনা" else "Study Time & Trend"
    fun syllabusCompletionRate(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সিলেবাস সম্পন্ন হার" else "Syllabus Completion"
    fun emptyAnalytics(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এখনও কোনো স্টাডি সেশন রেকর্ড করা হয়নি। ফোকাস টাইমার ব্যবহার করে পড়াশোনা শুরু করুন।" else "No study sessions recorded yet. Start studying with the Focus Timer to see your analytics."

    // Profile & Settings
    fun profileTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রোফাইল ও সেটিংস" else "Profile & Settings"
    fun candidateSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিশ্ববিদ্যালয় ভর্তি পরীক্ষার্থী ২০২৬" else "University Admission Candidate 2026"
    fun editNameTitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নাম পরিবর্তন" else "Edit Profile Name"
    fun enterNamePrompt(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনার নাম লিখুন" else "Enter your name"
    fun sectionGeneral(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সাধারণ পছন্দসমূহ" else "General Preferences"
    fun switchLanguage(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভাষা (Language)" else "App Language (ভাষা)"
    fun selectLanguage(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভাষা নির্বাচন করুন" else "Select Language"
    fun darkModeSetting(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডার্ক মোড (Dark Theme)" else "Dark Theme (ডার্ক মোড)"
    fun lightModeLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "লাইট মোড" else "Light Mode"
    fun darkModeLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডার্ক মোড" else "Dark Mode"
    fun notificationsSetting(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "স্টাডি নোটিফিকেশন" else "Study Reminders"
    fun notifEnabled(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "চালু" else "Enabled"
    fun notifDisabled(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বন্ধ" else "Disabled"
    fun sectionData(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ডাটা ব্যবস্থাপনা" else "Data Management"
    fun loadDemoData(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নমুনা ডাটা লোড করুন (Demo Data)" else "Load Sample Demo Data"
    fun demoDataSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নমুনা বিষয়, অধ্যায় ও ভর্তি লক্ষ্য লোড হবে" else "Populate realistic sample subjects and calendar"
    fun clearData(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সকল তথ্য মুছুন (Reset All Data)" else "Reset All Data"
    fun clearDataSubtitle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সকল সংরক্ষিত তথ্য মুছে খালি করে ফেলবে" else "Erase all subjects, study logs and progress"
    fun sectionAbout(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাপ সম্পর্কিত" else "About StudyHub"
    fun appVersionLabel(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাপ ভার্সন" else "App Version"
    fun helpAbout(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সহায়তা ও নির্দেশিকা" else "Help & User Guide"
    fun helpContent(lang: AppLanguage) = if (lang == AppLanguage.BANGLA)
        "StudyHub হলো বিশ্ববিদ্যালয় ভর্তি পরীক্ষার্থীদের জন্য একটি পূর্ণাঙ্গ প্রস্তুতি ও ট্র্যাকিং অ্যাপ。\n\n• সিলেবাস: বিষয় ও অধ্যায়ের প্রস্তুতি ট্র্যাক করুন।\n• ফোকাস টাইমার: স্টাডি সেশন রেকর্ড করুন।\n• স্মার্ট প্ল্যানার: অগ্রাধিকারভিত্তিক পড়ার তালিকা তৈরি করুন।\n• ভর্তি ক্যালেন্ডার: আবেদন ও পরীক্ষার তারিখ সংরক্ষণ করুন।"
    else
        "StudyHub is a comprehensive prep companion for university admission candidates.\n\n• Syllabus: Track subjects, chapters & completion status.\n• Focus Timer: Record real study sessions with pause/resume.\n• Smart Planner: Prioritize tasks and schedule revisions.\n• Admission Calendar: Track application deadlines & exam dates."
}
